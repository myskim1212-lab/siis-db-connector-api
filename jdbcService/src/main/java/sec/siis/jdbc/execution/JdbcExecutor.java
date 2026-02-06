package sec.siis.jdbc.execution;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sec.siis.jdbc.config.ActionType;
import sec.siis.jdbc.config.EffectiveOperationConfig;
import sec.siis.jdbc.config.FieldConfig;
import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.config.OperationConfig;
import sec.siis.jdbc.exception.ApiException;
import sec.siis.jdbc.exception.DbException;
import sec.siis.jdbc.exception.ExceptionMessageUtils;
import sec.siis.jdbc.exception.SystemException;
import sec.siis.jdbc.logging.LogMessageManager;
import sec.siis.jdbc.strategy.DbStrategy;
import sec.siis.jdbc.strategy.StrategyFactory;
import sec.siis.jdbc.util.CommonJsonUtil;

public class JdbcExecutor {

	private static final Logger log = LoggerFactory.getLogger(JdbcExecutor.class);
	private static final int DEFAULT_PROCEDURE_AFFECTED_ROWS = 1;

	private final DataSource dataSource;
	private final DbStrategy strategy;
	private String msgID;
	
	public JdbcExecutor(DataSource dataSource, String msgID) throws Exception {
		this.dataSource = dataSource;
		this.strategy = StrategyFactory.create(dataSource,msgID);
		this.msgID = msgID;
	}

	/*
	 * ========================================================= 
	 * Entry 
	 * =========================================================
	 */
	public JdbcExecutionResult executeJdbc(JdbcConfig config, String inputData, JdbcExecutionResult result)
			throws Exception {

		try {
			ObjectMapper mapper = ObjectMapperHolder.INSTANCE.mapper;
			JsonNode root = mapper.readTree(inputData);
			Map<String, JsonNode> pathCache = new HashMap<>();

			List<CollectOperation> txUnit = new ArrayList<>();
			boolean hasError = false;

			
			int order = 0;
			for (OperationConfig op : config.getOperations()) {
				order++;
				EffectiveOperationConfig eop = new EffectiveOperationConfig(config, op);

				boolean existNode = CommonJsonUtil.existRecordNode(root, eop.getData_record_path(),
						eop.getOperation_name(), eop.getData_record(), pathCache);

				List<Map<String, Object>> rows = CommonJsonUtil.extractRecord(root, eop.getData_record_path(),
						eop.getOperation_name(), eop.getData_record(), pathCache);

				CollectOperation collectOp = new CollectOperation(order, eop, rows, existNode);

				// DML이면서 ROW 스코프인 경우만 전환 트리거 발생
			    boolean isRowDml = (eop.getCommit_scope() == CommitScope.ROW 
			                        && eop.getAction_type() != ActionType.SELECT 
			                        && eop.getAction_type() != ActionType.PROCEDURE);
			   
			    if (isRowDml) {
			        // 1. 이전 ALL scope 유닛 처리
			        if (!txUnit.isEmpty()) {
			            hasError = executeTxUnit(txUnit, config, result);
			            if (hasError && config.getStop_on_operation_error()) break;
			        }

			        // 2. 현재 ROW 단위 개별 처리
			        hasError = (op.hasDetailOperations()) 
			                   ? executeMasterDetailRowCommit(collectOp, config, result)
			                   : executeRowCommit(collectOp, config, result);

			        if (hasError && config.getStop_on_operation_error()) break;
			        continue;
			    }

			    // SELECT, PROCEDURE, ALL-DML은 여기에 누적
			    txUnit.add(collectOp);

			    // 명시적 커밋 지점
			    if (eop.getCommit_Action() == CommitAction.COMMIT) {
			        hasError = executeTxUnit(txUnit, config, result);
			        if (hasError && config.getStop_on_operation_error()) break;
			    }
			}
			// 잔여 작업 처리 (hasError 체크가 핵심)
			if (!txUnit.isEmpty()) {
			    executeTxUnit(txUnit, config, result);
			}

		} catch (Exception e) {
			LogMessageManager.errorExecuteJdbcFailed(log, msgID, e);
			throw new SystemException(e);
		} finally {
			result.finish();
		}
		return result;
	}

	private boolean executeTxUnit(List<CollectOperation> txUnit, JdbcConfig config, JdbcExecutionResult result) {
		if (txUnit.isEmpty()) {
			return false;
		}

		boolean hasError = executeInTx(txUnit, config, result);
		txUnit.clear();
		return hasError;
	}

	private boolean executeInTx(List<CollectOperation> collectOps, JdbcConfig config, JdbcExecutionResult result) {
		if (collectOps.isEmpty()) {
			return false;
		}

		// 1. TxUnit 시작 로깅 및 시간 측정 시작
		String ops = LogMessageManager.formatOperations(collectOps, op -> op.getEop().getOperation_name());
		LogMessageManager.infoTxUnitOperations(log, msgID, config.getApi_name(), ops);
		long txStartTime = System.currentTimeMillis();

		List<JdbcExecutionOperationResult> opResults = new ArrayList<>();
		Connection conn = null;
		boolean rollback = false;

		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			for (CollectOperation op : collectOps) {
				//String opName = op.getEop().getOperation_name();
				//ActionType actionType = op.getEop().getAction_type();

				if (op.getEop().isMasterDetail()) {
					List<JdbcExecutionOperationResult> md = executeMasterDetailInTx(op, config, conn);
					opResults.addAll(md);
					if (md.stream().anyMatch(JdbcExecutionOperationResult::isFail))
						rollback = true;
				} else {
					JdbcExecutionOperationResult r = executeGeneralInTx(op, config, conn);
					opResults.add(r);
					if (r.isFail())
						rollback = true;
				}

				if (rollback)
					break;
			}

			if (rollback) {
				conn.rollback();
			} else {
				conn.commit();
			}

		} catch (Exception e) {
			rollback = true;
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					LogMessageManager.errorExecuteJdbcFailed(log, msgID, ex);
				}
			}
			throw new SystemException(e);
		} finally {
			if (conn != null) {
				try {
					if (!conn.isClosed()) {
		                conn.setAutoCommit(true); // 커넥션 반납 전 상태 복구
		                conn.close();
		            }
				} catch (SQLException e) {
					LogMessageManager.errorExecuteJdbcFailed(log, msgID, e);
				}
			}

			// 4. TxUnit 최종 결과 및 전체 소요시간 로깅
			long txDuration = System.currentTimeMillis() - txStartTime;
			if (!rollback) {
				LogMessageManager.infoTxUnitCommitted(log, msgID, config.getApi_name(), ops);
			} else {
				LogMessageManager.infoTxUnitRolledBack(log, msgID, config.getApi_name(), ops);
			}
		}

		boolean isCommit = !rollback;
		opResults.forEach(r -> r.setCommitted(isCommit));
		if (!isCommit)
			opResults.forEach(r -> r.setAffectedRows(0));
		result.addAll(opResults, isCommit);

		return rollback;
	}

	private JdbcExecutionOperationResult executeGeneralInTx(CollectOperation collectOp, JdbcConfig config,
			Connection conn) {

		JdbcExecutionOperationResult r = JdbcExecutionOperationResult.init(collectOp.getOrder(), collectOp.getEop());
		List<Map<String, Object>> rows = collectOp.getRows();
		r.start();
		int rowSize = (rows == null ? 0 : rows.size());
		r.setRequestRecordCount(rowSize);

		if (!collectOp.isExistNode()) {
			String skipReason = String.format("Record node not present: path=%s, record=%s",
					collectOp.getEop().getData_record_path(), collectOp.getEop().getData_record());
			r.skip(skipReason);
			LogMessageManager.debugOperationSkipped(log, msgID, collectOp.getEop().getApi_name(),
					collectOp.getEop().getOperation_name(), skipReason);
			r.end();
			return r;
		}

		try {
			
			LogMessageManager.debugOperationStart(log, msgID, collectOp.getEop().getApi_name(), 
					collectOp.getEop().getOperation_name(), r.getActionType());
			
			OperationOutput out = runOperation(config, collectOp.getEop(), rows, conn);
			
			if (collectOp.getEop().getAction_type()==ActionType.SELECT) {
				if (out.getData() != null) {
					r.setResponseRecordCount(out.getData().size());
					r.setResultData(out.getData());
				}
			}
			r.setSuccessCount(out.getSuccessCount());
			r.setAffectedRows(out.getAffectedCount());
			r.setSuccess(true);
		} catch (ApiException e) {
			r.setFailCount(rowSize);
			r.setAffectedRows(0);
			r.fail(e);
			LogMessageManager.errorOperationExecutionFailed(log, msgID, collectOp.getEop().getApi_name(),
					collectOp.getEop().getOperation_name(), e);
		} catch (Exception e) {
			r.setFailCount(rowSize);
			r.setAffectedRows(0);
			r.fail(new SystemException(e));
			LogMessageManager.errorOperationExecutionFailed(log, msgID, collectOp.getEop().getApi_name(),
					collectOp.getEop().getOperation_name(), e);
		} finally {
			r.end();
			LogMessageManager.debugOperationComplete(log, msgID, collectOp.getEop().getApi_name(), 
					collectOp.getEop().getOperation_name(), r.getElapsedMs());
		}
		//r.end();
		return r;
	}

	/*
	 * ========================================================= 
	 * Master Detail ALL
	 * =========================================================
	 */
	private List<JdbcExecutionOperationResult> executeMasterDetailInTx(CollectOperation collectOp, JdbcConfig config,
			Connection conn) {

		List<JdbcExecutionOperationResult> results = new ArrayList<>();

		OperationConfig masterOp = collectOp.getOriginalConfig();
		EffectiveOperationConfig masterEop = collectOp.getEop();
		List<Map<String, Object>> masterRows = collectOp.getRows();

		JdbcExecutionOperationResult master = JdbcExecutionOperationResult.init(collectOp.getOrder(), masterEop);
		master.start();
		int masterRowSize = (masterRows == null ? 0 : masterRows.size());
		master.setRequestRecordCount(masterRowSize);

		LogMessageManager.debugExecutingMasterDetail(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name());

		if (!collectOp.isExistNode()) {
			String skipReason = String.format("Record node not present: path=%s, record=%s",
					masterEop.getData_record_path(), masterEop.getData_record());
			master.skip(skipReason);
			LogMessageManager.debugOperationSkipped(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name(),
					skipReason);
			master.end();
			results.add(master);
			return results;
		}

		Map<String, JdbcExecutionOperationResult> detailMap = new LinkedHashMap<>();
		for (OperationConfig d : masterOp.getDetail_operations()) {
			EffectiveOperationConfig e = EffectiveOperationConfig.forDetail(config, masterOp, d);
			JdbcExecutionOperationResult r = JdbcExecutionOperationResult.init(collectOp.getOrder(), e);
			detailMap.put(e.getOperation_name(), r);
		}

		// detail 별 row 수 체크
		for (Map<String, Object> masterRow : masterRows) {
			for (OperationConfig d : masterOp.getDetail_operations()) {

				List<Map<String, Object>> detailRows = extractDetailRecords(masterRow, d.getData_record());
				if (detailRows == null || detailRows.isEmpty()) {
					continue;
				}

				EffectiveOperationConfig detailEop = EffectiveOperationConfig.forDetail(config, masterOp, d);
				JdbcExecutionOperationResult dr = detailMap.get(detailEop.getOperation_name());

				dr.incrementRequestRecordCount(detailRows.size());
			}
		}
		try {

			int masterSuccessCount = 0;
			int masterAffectedCount = 0;

			for (Map<String, Object> masterRow : masterRows) {

				OperationOutput masterOut = runOperation(config, masterEop, List.of(masterRow), conn);

				for (OperationConfig d : masterOp.getDetail_operations()) {
					List<Map<String, Object>> detailRows = extractDetailRecords(masterRow, d.getData_record());
					if (detailRows == null)
						continue;

					EffectiveOperationConfig e = EffectiveOperationConfig.forDetail(config, masterOp, d);
					JdbcExecutionOperationResult dr = detailMap.get(e.getOperation_name());

					if (dr.getStartTime() == 0) {
						dr.start();
					}
					try {
						inheritMasterFields(detailRows, masterRow, masterOp, d.getInherit_fields());
						OperationOutput out = runOperation(config, e, detailRows, conn);
						dr.incrementSuccessCount(out.getSuccessCount());
						dr.incrementAffectedRows(out.getAffectedCount());
					} catch (Exception de) {
						dr.setSuccessCount(0);
						dr.setFailCount(detailRows.size());
						dr.fail(de instanceof ApiException ? (ApiException) de : new SystemException(de));
						throw (de instanceof RuntimeException) ? (RuntimeException) de : new SystemException(de);
					}
				}
				masterSuccessCount += masterOut.getSuccessCount();
				masterAffectedCount += masterOut.getAffectedCount();
			}
			master.setSuccess(true);
			master.setSuccessCount(masterSuccessCount);
			master.setAffectedRows(masterAffectedCount);

		} catch (Exception e) {
			master.setSuccessCount(0);
			master.setFailCount(masterRowSize);
			master.fail(e instanceof ApiException ? (ApiException) e : new SystemException(e));
			LogMessageManager.errorOperationExecutionFailed(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name(),
					e);
		}

		master.end();
		results.add(master);

		for (JdbcExecutionOperationResult dr : detailMap.values()) {
			if (master.isSuccess()) { // master가 success 이면 detail도 모두 성공으로 간주
				dr.setSuccess(true);
				dr.setSuccessCount(dr.getRequestRecordCount());
			} else {
				dr.setSuccess(false);
				dr.setFailCount(dr.getRequestRecordCount());
			}
			dr.end();
			results.add(dr);
		}

		return results;
	}

	/*
	 * ========================================================= ROW commit
	 * =========================================================
	 */
	private boolean executeMasterDetailRowCommit(CollectOperation collectOp, JdbcConfig config,
			JdbcExecutionResult result) {

		OperationConfig masterOp = collectOp.getOriginalConfig();
		EffectiveOperationConfig masterEop = collectOp.getEop();
		List<Map<String, Object>> masterRows = collectOp.getRows();

		JdbcExecutionOperationResult master = JdbcExecutionOperationResult.init(collectOp.getOrder(), masterEop);
		master.start();

		int masterRowSize = (masterRows == null ? 0 : masterRows.size());
		master.setRequestRecordCount(masterRowSize);

		if (!collectOp.isExistNode()) {
			String skipReason = String.format("Record node not present: path=%s, record=%s",
					masterEop.getData_record_path(), masterEop.getData_record());
			master.skip(skipReason);
			LogMessageManager.debugOperationSkipped(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name(),
					skipReason);
			master.end();
			result.add(master);
			return false;
		}

		LogMessageManager.debugRowCommitStarted(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name(),
				masterRowSize);

		// Operation별 JdbcExecutionOperationResult 생성 및 초기화
		Map<String, JdbcExecutionOperationResult> detailMap = new LinkedHashMap<>();
		for (OperationConfig d : masterOp.getDetail_operations()) {
			EffectiveOperationConfig e = EffectiveOperationConfig.forDetail(config, masterOp, d);
			JdbcExecutionOperationResult r = JdbcExecutionOperationResult.init(collectOp.getOrder(), e);
			detailMap.put(e.getOperation_name(), r);
		}

		// detail 별 row 수 체크
		for (Map<String, Object> masterRow : masterRows) {
			for (OperationConfig d : masterOp.getDetail_operations()) {

				List<Map<String, Object>> detailRows = extractDetailRecords(masterRow, d.getData_record());
				if (detailRows == null || detailRows.isEmpty()) {
					continue;
				}

				EffectiveOperationConfig detailEop = EffectiveOperationConfig.forDetail(config, masterOp, d);
				JdbcExecutionOperationResult dr = detailMap.get(detailEop.getOperation_name());

				dr.incrementRequestRecordCount(detailRows.size());
			}
		}

		int masterSuccessCount = 0;
		int masterAffectedCount = 0;

		for (int i = 0; i < masterRowSize; i++) {
			Map<String, Object> masterRow = masterRows.get(i);
			Connection conn = null;

			try {
				conn = dataSource.getConnection();
				conn.setAutoCommit(false);

				// Master 실행
				OperationOutput masterOut = runOperation(config, collectOp.getEop(), List.of(masterRow), conn);

				// Detail 실행 (executeMasterDetailInTx 스타일)
				for (OperationConfig d : masterOp.getDetail_operations()) {
					List<Map<String, Object>> detailRows = extractDetailRecords(masterRow, d.getData_record());
					if (detailRows == null || detailRows.isEmpty()) {
						continue;
					}

					EffectiveOperationConfig detailEop = EffectiveOperationConfig.forDetail(config, masterOp, d);
					JdbcExecutionOperationResult dr = detailMap.get(detailEop.getOperation_name());

					// 첫 실행 시 start
					if (dr.getStartTime() == 0) {
						dr.start();
					}

					try {
						inheritMasterFields(detailRows, masterRow, masterOp, d.getInherit_fields());
						OperationOutput out = runOperation(config, detailEop, detailRows, conn);
						dr.incrementSuccessCount(out.getSuccessCount());
						dr.incrementAffectedRows(out.getAffectedCount());

					} catch (Exception de) {
						// dr.setSuccessCount(0);
						dr.incrementFail(detailRows.size());
						// dr.setFailCount(detailRows.size());
						dr.fail(de instanceof ApiException ? (ApiException) de : new SystemException(de));
						throw (de instanceof RuntimeException) ? (RuntimeException) de : new SystemException(de);
					}
				}

				conn.commit();

				master.incrementSuccessCount(masterOut.getSuccessCount());
				master.incrementAffectedRows(masterOut.getAffectedCount());

//                LogMessageManager.debugRowCommitSuccess(log, msgID, 
//                    masterEop.getApi_name(), 
//                    masterEop.getOperation_name(), 
//                    i);

			} catch (Exception e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						LogMessageManager.errorExecuteJdbcFailed(log, msgID, ex);
					}
				}

				master.incrementFail(1);
				master.fail(e);
				master.addRowError(RowError.fromException(i, masterRow,
						e instanceof ApiException ? (ApiException) e : new SystemException(e)));

				LogMessageManager.debugRowCommitFailed(log, msgID, masterEop.getApi_name(), masterEop.getOperation_name(), i,
						e.getMessage());

				// 치명적 에러 여부 판단
				boolean isFatal = ExceptionMessageUtils.isFatalError(e);

				if (isFatal) {
					LogMessageManager.errorOperationExecutionFailed(log, msgID, masterEop.getApi_name(),
							masterEop.getOperation_name(), e);
				}
				// 중단 조건 체크: 치명적 에러이거나, 설정에 의해 중단해야 하거나
				if (isFatal || config.getStop_on_row_error()) {
					break;
				}

			} finally {
				if (conn != null) {
					try {
						if (!conn.isClosed()) {
			                conn.setAutoCommit(true); // 커넥션 반납 전 상태 복구
			                conn.close();
			            }
					} catch (SQLException e) {
						LogMessageManager.errorExecuteJdbcFailed(log, msgID, e);
					}
				}
			}

//            if ((i + 1) % 10 == 0 || (i + 1) == masterRowSize) {
//                LogMessageManager.debugRowCommitProgress(log, msgID, 
//                    masterEop.getApi_name(), 
//                    masterEop.getOperation_name(), 
//                    i + 1, 
//                    masterRowSize);
//            }
		}

		// Master 결과 마무리
		master.setSuccess(master.getFailCount() == 0);
		master.setCommitted(master.isSuccess());
		master.end();

		// Detail 결과 마무리 및 추가 (executeMasterDetailInTx 스타일)
		result.add(master, master.isSuccess());

		for (JdbcExecutionOperationResult r : detailMap.values()) {
			r.setSuccess(r.getFailCount() == 0);
			r.end();
			result.add(r, r.isSuccess());
		}

		return master.isFail();
	}

	private boolean executeRowCommit(CollectOperation collectOp, JdbcConfig config, JdbcExecutionResult result) {
		JdbcExecutionOperationResult r = JdbcExecutionOperationResult.init(collectOp.getOrder(), collectOp.getEop());

		List<Map<String, Object>> rows = collectOp.getRows();
		r.start();
		int rowSize = (rows == null ? 0 : rows.size());
		r.setRequestRecordCount(rowSize);

		LogMessageManager.debugRowCommitStarted(log, msgID, collectOp.getEop().getApi_name(),
				collectOp.getEop().getOperation_name(), rowSize);

		for (int i = 0; i < rowSize; i++) {
			Map<String, Object> row = rows.get(i);
			Connection conn = null;

			try {
				conn = dataSource.getConnection();
				conn.setAutoCommit(false);

				OperationOutput out = runOperation(config, collectOp.getEop(), List.of(row), conn);

				conn.commit();

				r.incrementSuccessCount(out.getSuccessCount());
				r.incrementAffectedRows(out.getAffectedCount());

//                LogMessageManager.debugRowCommitSuccess(log, msgID, 
//                    collectOp.getEop().getApi_name(), 
//                    collectOp.getEop().getOperation_name(), 
//                    i);

			} catch (Exception e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						LogMessageManager.errorExecuteJdbcFailed(log, msgID, ex);
					}
				}

				r.incrementFail(1);
				// 첫번째 에러일 때
				if (r.getFailCount() == 1) {
					r.fail(e);
				}

				r.addRowError(RowError.fromException(i, row,
						e instanceof ApiException ? (ApiException) e : new SystemException(e)));

				LogMessageManager.debugRowCommitFailed(log, msgID, collectOp.getEop().getApi_name(),
						collectOp.getEop().getOperation_name(), i, e.getMessage());

				// 치명적 에러 여부 판단
				boolean isFatal = ExceptionMessageUtils.isFatalError(e);

				if (isFatal) {
					LogMessageManager.errorOperationExecutionFailed(log, msgID, collectOp.getEop().getApi_name(),
							collectOp.getEop().getOperation_name(), e);
				}
				// 중단 조건 체크: 치명적 에러이거나, 설정에 의해 중단해야 하거나
				if (isFatal || config.getStop_on_row_error()) {
					break;
				}
			} finally {
				if (conn != null) {
					try {
						if (!conn.isClosed()) {
			                conn.setAutoCommit(true); // 커넥션 반납 전 상태 복구
			                conn.close();
			            }
					} catch (SQLException e) {
						LogMessageManager.errorExecuteJdbcFailed(log, msgID, e);
					}
				}
			}

			if ((i + 1) % 10 == 0 || (i + 1) == rowSize) {
				LogMessageManager.debugRowCommitProgress(log, msgID, collectOp.getEop().getApi_name(),
						collectOp.getEop().getOperation_name(), i + 1, rowSize);
			}
		}

		r.setSuccess(r.getFailCount() == 0);
		r.setCommitted(r.isSuccess());
		r.end();

		result.add(r, r.isSuccess());
		return r.isFail();
	}

	public OperationOutput runOperation(JdbcConfig config, EffectiveOperationConfig eop, List<Map<String, Object>> rows,
			Connection conn) throws Exception {

		int affectedCount = 0;

		try {
			switch (eop.getAction_type()) {
			case INSERT:
				affectedCount = strategy.executeBatch(conn, eop, config.getBatch_size(), rows);
				return new OperationOutput((rows == null ? 0 : rows.size()), affectedCount, null);
			case UPSERT:
				affectedCount = strategy.executeBatch(conn, eop, config.getBatch_size(), rows);
				return new OperationOutput((rows == null ? 0 : rows.size()), affectedCount, null);
			case UPDATE:
				if (rows == null || rows.isEmpty()) {
					affectedCount = strategy.executeSql(conn, eop);
					return new OperationOutput(0, affectedCount, null);
				} else {
					affectedCount = strategy.executeBatch(conn, eop, config.getBatch_size(), rows);
					return new OperationOutput(rows.size(), affectedCount, null);
				}
			case DELETE:
				if (rows == null || rows.isEmpty()) {
					affectedCount = strategy.executeSql(conn, eop);
					return new OperationOutput(0, affectedCount, null);
				} else {
					affectedCount = strategy.executeBatch(conn, eop, config.getBatch_size(), rows);
					return new OperationOutput(rows.size(), affectedCount, null);
				}
			case SELECT:
				List<Map<String, Object>> resultData = strategy.executeSelect(conn, eop, rows);
				return new OperationOutput(resultData.size(), 0, resultData);
			case PROCEDURE:
				if (rows == null || rows.isEmpty()) {
					strategy.executeProcedure(conn, eop, null);
					return new OperationOutput(DEFAULT_PROCEDURE_AFFECTED_ROWS, 0, null);
				} else {
					strategy.executeProcedure(conn, eop, rows);
					return new OperationOutput(rows.size(), 0, null);
				}
			default:
				throw new IllegalArgumentException("Unsupported type");
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	private List<Map<String, Object>> extractDetailRecords(Map<String, Object> masterRow, String key) {
		Object v = masterRow.get(key);
		if (v instanceof List)
			return (List<Map<String, Object>>) v;
		if (v instanceof Map)
			return List.of((Map<String, Object>) v);
		return null;
	}

	private void inheritMasterFields(List<Map<String, Object>> detailRows, Map<String, Object> masterRow,
			OperationConfig masterOp, List<FieldConfig> inheritFields) {

		if (inheritFields == null)
			return;
		for (Map<String, Object> d : detailRows) {
			for (FieldConfig f : inheritFields) {
				d.put(f.getData_field(), masterRow.get(f.getData_field()));
			}
		}
	}
}