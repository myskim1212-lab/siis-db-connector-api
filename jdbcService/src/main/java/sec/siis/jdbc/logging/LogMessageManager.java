package sec.siis.jdbc.logging;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import sec.siis.jdbc.config.ActionType;
import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.execution.JdbcExecutionOperationResult;

/**
 * 데이터베이스 작업 관련 로그 메시지를 중앙에서 관리하는 클래스
 * 개선된 로그 포맷 적용
 */
public class LogMessageManager {

    private LogMessageManager() {
        // 인스턴스화 방지
    }

    // ========== 로그 포맷 스타일 설정 ==========
    
    /**
     * 로그 포맷 스타일
     * COMPACT: 간결한 형식 (기본)
     * DETAILED: 상세한 형식
     * STRUCTURED: 구조화된 형식 (JSON-like)
     */
    public enum LogStyle {
        COMPACT,    // [API:xxx | OP:yyy] 메시지
        DETAILED,   // apiName=xxx, operationName=yyy : 메시지
    }
    
    private static LogStyle currentStyle = LogStyle.COMPACT;
    
    public static void setLogStyle(LogStyle style) {
        currentStyle = style;
    }
    
    // ========== 메시지 포맷팅 헬퍼 ==========
    
    private static String formatPrefix(String msgID , String apiName, String operationName) {
        switch (currentStyle) {
            case COMPACT:
                return String.format("%s [%s | %s]", 
                	msgID,
                    truncate(apiName, 50), 
                    truncate(operationName, 50));
            case DETAILED:
            default:
            	return String.format("%s API [%s] , OP [%s] ", msgID, apiName, operationName);
        }
    }
    
    private static String formatPrefix(String msgID , String apiName) {
        switch (currentStyle) {
            case COMPACT:
                return String.format("%s [%s]", 
                	msgID,
                    truncate(apiName, 50));
            case DETAILED:
            default:
            	return String.format("%s API [%s] ", msgID, apiName);
        }
    }    
    
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 2) + ".." : str;
    }
    
    // ========== JdbcExecutor - INFO 레벨 로그 메서드 ==========
  
    public static void infoStartIf(Logger log, String msgID, String apiName, String ops) {
        if (log.isInfoEnabled()) {
            log.info("{} >> BEGIN | Operations : {}",formatPrefix(msgID, apiName),ops);
        }
    }
    public static void infoEndIf(Logger log, String msgID, String apiName) {
        if (log.isInfoEnabled()) {
            log.info("{} << END ",formatPrefix(msgID, apiName));
        }
    }    
    public static void infoTxUnitOperations(Logger log, String msgID, String apiName,String operations) {
        if (log.isInfoEnabled()) {
            log.info("{} TxUnit Started",formatPrefix(msgID, apiName,operations));
        }
    }
    
    public static void infoTxUnitCommitted(Logger log, String msgID, String apiName, String operations) {
        if (log.isInfoEnabled()) {
            log.info("{} TxUnit Committed",formatPrefix(msgID, apiName,operations));
        }
    }
    
    public static void infoTxUnitRolledBack(Logger log, String msgID, String apiName, String operations) {
        if (log.isInfoEnabled()) {
            log.info("{} [{}] , OP [{}] TxUnit Rolled Back",formatPrefix(msgID, apiName,operations));
        }
    }

    // ========== JdbcExecutor - DEBUG 레벨 로그 메서드 ==========
    
    public static void debugExecutingMasterDetail(Logger log, String msgID, String apiName, String operationName) {
        if (log.isDebugEnabled()) {
            log.debug("{} [ Master-Detail ] execution started", formatPrefix(msgID, apiName, operationName));
        }
    }
 /*   
    public static void debugMasterRecordProcessed(Logger log, String msgID, String apiName, String operationName, 
                                                  int current, int total) {
        if (log.isDebugEnabled()) {
            log.debug("{}   ├─ Master [{}/{}] processed", 
                formatPrefix(msgID, apiName, operationName), current, total);
        }
    }
    
    public static void debugDetailRecordProcessed(Logger log, String msgID, String apiName, String operationName, 
                                                  String detailOpName, int rowCount) {
        if (log.isDebugEnabled()) {
            log.debug("{}   │  └─ Detail '{}': {} rows", 
                formatPrefix(msgID, apiName, operationName), detailOpName, rowCount);
        }
    }
    */
    public static void debugRowCommitStarted(Logger log, String msgID, String apiName, String operationName, int totalRows) {
        if (log.isDebugEnabled()) {
            log.debug("{} Row-by-row commit: {} rows", 
                formatPrefix(msgID, apiName, operationName), totalRows);
        }
    }
    
    public static void debugRowCommitProgress(Logger log, String msgID, String apiName, String operationName, 
                                             int completed, int total) {
        if (log.isDebugEnabled()) {
            int percentage = (completed * 100) / total;
            log.debug("{} Progress: {}/{} ({}%)", 
                formatPrefix(msgID, apiName, operationName), completed, total, percentage);
        }
    }
    
    public static void debugRowCommitSuccess(Logger log, String msgID, String apiName, String operationName, int rowIndex) {
        if (log.isDebugEnabled()) {
            log.debug("{} Row[{}] committed", formatPrefix(msgID, apiName, operationName), rowIndex);
        }
    }
    
    public static void debugRowCommitFailed(Logger log, String msgID, String apiName, String operationName, 
                                           int rowIndex, String reason) {
        if (log.isDebugEnabled()) {
            log.debug("{} Row[{}] failed: {}", 
                formatPrefix(msgID, apiName, operationName), rowIndex, reason);
        }
    }
    
    public static void debugOperationSkipped(Logger log, String msgID, String apiName, String operationName, String reason) {
        if (log.isDebugEnabled()) {
            log.debug("{} Skipped: {}", formatPrefix(msgID, apiName, operationName), reason);
        }
    }
    
    public static void debugTransactionStarted(Logger log, String msgID, String apiName, String operationName) {
        if (log.isDebugEnabled()) {
            log.debug("{} Transaction started", formatPrefix(msgID, apiName, operationName));
        }
    }
    
    public static void debugTransactionCommitted(Logger log, String msgID, String apiName, String operationName) {
        if (log.isDebugEnabled()) {
            log.debug("{} Transaction committed", formatPrefix(msgID, apiName, operationName));
        }
    }
    
    public static void debugTransactionRolledBack(Logger log, String msgID, String apiName, String operationName, String reason) {
        if (log.isDebugEnabled()) {
            log.debug("{} Transaction rolled back: {}", 
                formatPrefix(msgID, apiName, operationName), reason);
        }
    }

    // ========== AbstractDatabaseStrategy - DEBUG 레벨 로그 메서드 ==========
    
    public static void debugExecSql(Logger log, String msgID, String apiName, String operationName , String sql) {
        if (log.isDebugEnabled()) {
            log.debug("{} SQL = {}", formatPrefix(msgID, apiName, operationName),sql);
        }
    }
    
    public static void debugNoRowsToProcess(Logger log, String msgID, String apiName, String operationName) {
        if (log.isDebugEnabled()) {
            log.debug("{} No rows to process", formatPrefix(msgID, apiName, operationName));
        }
    }
    
    public static void debugUpdatedRows(Logger log, String msgID, String apiName, String operationName, int rowCount) {
        if (log.isDebugEnabled()) {
            log.debug("{} Updated {} rows", formatPrefix(msgID, apiName, operationName), rowCount);
        }
    }
    
    public static void debugExecutedOperation(Logger log, String msgID, String apiName, String operationName, int affectedRows) {
        if (log.isDebugEnabled()) {
            log.debug("{} Executed, affected {} rows", 
                formatPrefix(msgID, apiName, operationName), affectedRows);
        }
    }
    
    public static void debugOperationProcessed(Logger log, String msgID, String apiName, String operationName, int totalRows) {
        if (log.isDebugEnabled()) {
            log.debug("{} Operation processed {} rows", 
                formatPrefix(msgID, apiName, operationName), totalRows);
        }
    }
    
    public static void debugSelectReturned(Logger log, String msgID, String apiName, String operationName, int rowCount) {
        if (log.isDebugEnabled()) {
            log.debug("{} Select returned {} rows", 
                formatPrefix(msgID, apiName, operationName), rowCount);
        }
    }
    
    public static void debugProcedureNoParams(Logger log, String msgID, String apiName, String operationName) {
        if (log.isDebugEnabled()) {
            log.debug("{} Procedure executed (no params)", 
                formatPrefix(msgID, apiName, operationName));
        }
    }
    
    public static void debugProcedureExecuted(Logger log, String msgID, String apiName, String operationName, int executeCount) {
        if (log.isDebugEnabled()) {
            log.debug("{} Procedure executed {} times", 
                formatPrefix(msgID, apiName, operationName), executeCount);
        }
    }
    
    public static void debugBindingParameter(Logger log, String msgID, String apiName, String operationName, 
                                            String field, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("{} Param: {}={}", 
                formatPrefix(msgID, apiName, operationName), field, value);
        }
    }
    
    public static void debugSqlStatement(Logger log, String msgID, String apiName, String operationName, String sql) {
        if (log.isDebugEnabled()) {
            // SQL이 긴 경우 여러 줄로 표시하지 않고 한 줄로 축약
            String compactSql = sql.replaceAll("\\s+", " ").trim();
            if (compactSql.length() > 500) {
                compactSql = compactSql.substring(0, 497) + "...";
            }
            log.debug("{} SQL = {}", formatPrefix(msgID, apiName, operationName), compactSql);
        }
    }

    // ========== WARN 레벨 로그 메서드 ==========
    
    public static void warnSqlTypeNull(Logger log) {
        if (log.isWarnEnabled()) {
            log.warn("SQL type is null, defaulting to VARCHAR");
        }
    }
    
    public static void warnUnknownSqlType(Logger log, String msgID, String sqlType) {
        if (log.isWarnEnabled()) {
            log.warn("Unknown SQL type '{}', defaulting to VARCHAR", sqlType);
        }
    }

    // ========== ERROR 레벨 로그 메서드 ==========
    
    public static void errorExecuteJdbcFailed(Logger log, String msgID, Exception e) {
        log.error("JDBC execution failed", e);
    }
    
    public static void errorOperationExecutionFailed(Logger log, String msgID, String apiName, String operationName, Exception e) {
        log.error("{} Operation failed", formatPrefix(msgID, apiName, operationName), e);
    }
    
    public static void errorOperationExecutionCriticalFailed(Logger log, String msgID, String apiName, String operationName,Exception e) {
        log.error("{} Operation failed ( Critical DB error detected. Stopping all subsequent rows ) , {}", formatPrefix(msgID, apiName, operationName), e);
    }    
    
    public static void errorFailedReadClob(Logger log, Exception e) {
        log.error("Failed to read CLOB", e);
    }
    
    public static void errorFailedConvertBlob(Logger log, Exception e) {
        log.error("Failed to convert BLOB to Base64", e);
    }
    
    // ========== 유틸리티 메서드 ==========
    
    public static <T> String formatOperations(List<T> operations, 
                                             java.util.function.Function<T, String> formatter) {
        return operations.stream()
                        .map(formatter)
                        .reduce((a, b) -> a + " -> " + b)
                        .orElse("(none)");
    }
    
    public static void debugOperationStart(Logger log, String msgID, String apiName, String operationName, 
                                          ActionType actionType) {
        if (log.isDebugEnabled()) {
            log.debug("{} Starting {} operation", 
                formatPrefix(msgID, apiName, operationName), actionType);
        }
    }
    
    public static void debugOperationComplete(Logger log, String msgID, String apiName, String operationName, 
                                             long elapsedMs) {
        if (log.isDebugEnabled()) {
            log.debug("{} Completed in {}ms", 
                formatPrefix(msgID, apiName, operationName), elapsedMs);
        }
    }
    
    public static void debugBatchProgress(Logger log, String msgID, String apiName, String operationName, 
                                         int processed, int total) {
        if (log.isDebugEnabled()) {
            int percentage = (processed * 100) / total;
            log.debug("{} Batch progress: {}/{} ({}%)", 
                formatPrefix(msgID, apiName, operationName), processed, total, percentage);
        }
    }
    
    public static void infoExecutionSummary(
            Logger log,
            String msgID,
            String apiName,
            boolean success,
            long elapsedMs,
            List<JdbcExecutionOperationResult> operations) {

        if (!log.isInfoEnabled() || operations == null || operations.isEmpty()) return;

        // 1. 통계 및 각 필드의 최대 길이 조사
        boolean isAllCommitted = operations.stream().allMatch(JdbcExecutionOperationResult::isCommitted);

        int maxOrderLen = 1;
        int maxNameLen = 10; // 최소 헤더 길이
        int maxActionLen = 3;
        int maxSuccessLen = 1;
        int maxFailLen = 1;
        int maxAffectedLen = 1;
        int maxElapsedLen = 1;

        for (JdbcExecutionOperationResult r : operations) {
            maxOrderLen = Math.max(maxOrderLen, String.valueOf(r.getOrder()).length());
            maxNameLen = Math.max(maxNameLen, r.getOperationName() == null ? 0 : r.getOperationName().length());
            maxActionLen = Math.max(maxActionLen, simpleOpType(r.getActionType()).length());
            maxSuccessLen = Math.max(maxSuccessLen, String.valueOf(r.getSuccessCount()).length());
            maxFailLen = Math.max(maxFailLen, String.valueOf(r.getFailCount()).length());
            maxAffectedLen = Math.max(maxAffectedLen, String.valueOf(r.getAffectedRows()).length());
            maxElapsedLen = Math.max(maxElapsedLen, String.valueOf(r.getElapsedMs()).length());
        }

        // 2. 동적 포맷 문자열 생성 
        // %-[길이]s 형태를 만들어 실제 데이터 길이에 맞춤
        String formatStr = String.format(
            "  [%%%dd] %%-%ds | %%-%ds | %%-3s | Committed: %%-1s | Success: %%%dd | Fail: %%%dd | Affected: %%%dd | Elapsed(ms): %%%dd",
            maxOrderLen, maxNameLen, maxActionLen, maxSuccessLen, maxFailLen, maxAffectedLen, maxElapsedLen
        );

        // [Header] 출력
        log.info("{} [{}] [Summary] | Result: {} | Elapsed: {}ms", 
                 msgID, apiName, 
                 isAllCommitted ? "COMMITTED" : (success ? "SUCCESS" : "ROLLBACK"), 
                 elapsedMs);
        log.info("{} [{}] Operations:", msgID, apiName);

        // [Body] 출력
        for (JdbcExecutionOperationResult r : operations) {
            String statusMark = r.isSuccess() ? "OK " : "ERR";
            String commitMark = r.isCommitted() ? "Y" : "N";
            String actionType = simpleOpType(r.getActionType());

            String formattedLine = String.format(formatStr,
                r.getOrder(),
                r.getOperationName(),
                actionType,
                statusMark,
                commitMark,
                r.getSuccessCount(),
                r.getFailCount(),
                r.getAffectedRows(),
                r.getElapsedMs()
            );

            log.info("{} [{}] {}", msgID, apiName, formattedLine);

            if (r.isFail()) {
                String errDetail = r.getErrorDetail() != null ? r.getErrorDetail().trim() : "Unknown Error";
                log.info("{} [{}]          >> [ERROR] {}", msgID, apiName, errDetail);
            }
        }
    }
    
    private static String simpleOpType(ActionType actionType) {

        if (actionType == null) {
            return "UNK";
        }

        switch (actionType) {
            case DELETE:
                return "DEL";
            case INSERT:
                return "INS";
            case UPSERT:
                return "INU";
            case UPDATE:
                return "UPD";
            case SELECT:
                return "SEL";
            case PROCEDURE:
                return "PRO";
            default:
                return "UNK";
        }
    }
    
    private static String extractShortError(String detail) {
        if (detail == null) return "";

        // ORA-xxxxx
        int ora = detail.indexOf("ORA-");
        if (ora >= 0) {
            int end = detail.indexOf("\n", ora);
            return end > ora
                    ? detail.substring(ora, end)
                    : detail.substring(ora);
        }

        // fallback: 앞 80자
        return detail.length() > 80
                ? detail.substring(0, 77) + "..."
                : detail;
    }    
}