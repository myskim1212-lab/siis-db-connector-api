package sec.siis.jdbc.mediator.response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sec.siis.jdbc.config.ActionType;
import sec.siis.jdbc.config.ConfigValidationException;
import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.config.OperationConfig;
import sec.siis.jdbc.exception.ExceptionMessageUtils;
import sec.siis.jdbc.execution.JdbcExecutionOperationResult;
import sec.siis.jdbc.execution.JdbcExecutionResult;
import sec.siis.jdbc.util.DateTimeFormatUtil;

public class ApiResponseFactory {

    private static final String CODE_SUCCESS = "S000";
    private static final String CODE_PARTIAL_FAIL = "E206"; // 부분 실패
    private static final String CODE_SYSTEM_ERROR = "E500"; // 시스템/전체 에러
    private static final String CODE_CONFIG_VALIDATION_ERROR = "E400"; // 설정/검증 에러

    /**
     * [CASE 1] 시스템 예외 발생 시 (DB 연결 전이나 실행 중 치명적 예외)
     */
    public static Map<String, Object> error(JdbcConfig jcfg, Exception e,String msgID, long startTime, long endTime, JdbcExecutionResult jdbcExecResult) {
        // jcfg가 null일 수 있음을 고려하여 헤더 생성
        Map<String, Object> result = createHeader(jcfg, msgID, startTime, endTime);
        result.put("success", false);
        
        // 에러 메시지 수집
        String collectedMessages = (e != null) ? ExceptionMessageUtils.collectMessages(e) : "Unknown error occurred";
        String code = (e instanceof ConfigValidationException) ? CODE_CONFIG_VALIDATION_ERROR : CODE_SYSTEM_ERROR;
        
        boolean isAnyCommitted = false;
        if (jdbcExecResult != null && !jdbcExecResult.getOperations().isEmpty()) {
            isAnyCommitted = jdbcExecResult.getOperations().stream()
                                           .anyMatch(JdbcExecutionOperationResult::isCommitted);
            
            if (isAnyCommitted && !(e instanceof ConfigValidationException)) {
                code = CODE_PARTIAL_FAIL;
            }
            
            String opErrors = buildErrorDetails(jdbcExecResult.getOperations());
            result.put("errorSummary", String.format("ERROR: %s \n[Operation Errors]:\n%s", collectedMessages, opErrors));
            result.put("operations", transformOperations(jdbcExecResult, isAnyCommitted));
        } else {
            result.put("errorSummary", collectedMessages);
            result.put("operations", Collections.emptyList());
        }

        result.put("code", code);
        // e.getMessage() 호출 시 e가 null인 경우 방어
        String mainMsg = (e != null) ? e.getMessage() : "Error details unavailable";
        result.put("message", isAnyCommitted ? "Error occurred but some changes were committed." : mainMsg);

        return result;
    }

    /**
     * [CASE 2] 실행 완료 후 결과를 분석하여 응답 생성 (전체성공/부분실패/전체실패)
     */
    /**
     * 실행 완료 후 결과를 분석하여 응답 생성
     */
    public static Map<String, Object> createResponse(JdbcConfig jcfg,String msgID, long startTime, long endTime, JdbcExecutionResult execResult) {
        Map<String, Object> result = createHeader(jcfg, msgID, startTime, endTime);
        List<JdbcExecutionOperationResult> ops = execResult.getOperations();
        
        long totalOps = ops.size();
        long successOps = ops.stream().filter(JdbcExecutionOperationResult::isSuccess).count();
        
        // [변경 핵심] 실제 DB에 반영(Commit)된 오퍼레이션이 하나라도 있는지 확인
        boolean isAnyCommitted = ops.stream().anyMatch(JdbcExecutionOperationResult::isCommitted);

        if (successOps == totalOps) {
            // 1. 전체 성공
            result.put("success", true);
            result.put("code", CODE_SUCCESS);
            result.put("message", "Request processed successfully.");
            
            // root에 Select 모으지 않음.
            // result.put("resultData", extractSelectData(jcfg, execResult));
        } else {
            // 2. 실패 케이스
            result.put("success", false);

            if (isAnyCommitted) {
                // 일부는 성공해서 Commit까지 완료된 경우 (예: 개별 커밋 모드)
                result.put("code", CODE_PARTIAL_FAIL);
                result.put("message", String.format("Partially committed. (Success: %d, Total: %d)", successOps, totalOps));
            } else {
                // 실행은 성공했어도 롤백되었거나, 아예 실패한 경우 (결과적으로 DB 변동 없음)
                result.put("code", CODE_SYSTEM_ERROR);
                result.put("message", successOps > 0 
                    ? "Transaction rolled back due to error." 
                    : "All operations failed to execute.");
            }
            
            result.put("errorSummary", buildErrorDetails(ops));
        }

        // 전체 롤백 시 성공했던 오퍼레이션들의 success 상태 동기화 (옵션)
        result.put("operations", transformOperations(execResult, isAnyCommitted));
        return result;
    }

    /**
     * 에러 메시지를 가독성 있게 조합
     */
    private static String buildErrorDetails(List<JdbcExecutionOperationResult> ops) {
        return ops.stream()
                .filter(op -> !op.isSuccess())
                .map(op -> {
                    String opName = op.getOperationName();
                    // String errMsg = (op.getErrorMessage() != null) ? op.getErrorMessage() : "Execution Failed";
                    String errDetail = (op.getErrorDetail() != null) ? op.getErrorDetail().trim() : "";

                    // 대괄호는 오퍼레이션 명칭에만 사용하고, 상세 내용은 화살표와 괄호로 구분
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(opName).append("] ");
                    //sb.append(errMsg);

                    if (!errDetail.isEmpty()) {
                        // 에러 코드 대괄호를 소괄호로 치환하거나, 구분자 사용
                        String cleanedDetail = errDetail.replace("[", "(").replace("]", ")");
                        sb.append(cleanedDetail);
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Select 오퍼레이션의 결과만 추출
     */
    private static Map<String, Object> extractSelectData(JdbcConfig jcfg, JdbcExecutionResult result) {
        return result.getOperations().stream()
                .filter(r -> ActionType.SELECT==r.getActionType() && r.getResultData() != null)
                .collect(Collectors.toMap(
                    r -> jcfg.findOperation(r.getOperationName())
                             .map(OperationConfig::getOperation_name)
                             .orElse(r.getOperationName()),
                    JdbcExecutionOperationResult::getResultData,
                    (oldVal, newVal) -> newVal,
                    LinkedHashMap::new
                ));
    }

    private static Map<String, Object> createHeader(JdbcConfig jcfg,String msgID, long startTime, long endTime) {
        Map<String, Object> header = new LinkedHashMap<>();
        
        // jcfg가 null이면 기본값이나 빈 문자열 처리
        header.put("apiName", jcfg != null ? jcfg.getApi_name() : "UNKNOWN_API");
        header.put("targetSystemName", jcfg != null ? jcfg.getTarget_system_name() : "");
        header.put("targetSystemKey", jcfg != null ? jcfg.getTarget_system_key() : "");
        header.put("global_transaction_id", msgID != null ? msgID : "");
        header.put("startTime", DateTimeFormatUtil.formatTimestamp(startTime));
        header.put("endTime", DateTimeFormatUtil.formatTimestampOrNull(endTime));
        header.put("elapsedMs", (startTime > 0 && endTime >= startTime) ? (endTime - startTime) : 0);
        return header;
    }

    /**
     * [개선] 트랜잭션 상태에 따른 오퍼레이션 변환
     */
    private static Map<String, Object> transformOperations(JdbcExecutionResult execution, boolean isAnyCommitted) {
        if (execution == null || execution.getOperations() == null) {
            return Collections.emptyMap();
        }

        return execution.getOperations().stream()
                .collect(Collectors.toMap(
                    op -> {
                        // 비즈니스 로직: 롤백 처리 시 메시지 보정
                        if (!isAnyCommitted && op.isSuccess()) {
                            op.setErrorMessage("Rolled back due to transaction failure");
                        }
                        return op.getOperationName(); // Key
                    },
                    op -> op, // Value
                    (existing, replacement) -> existing, // Key 중복 시 처리 (기존 값 유지)
                    LinkedHashMap::new // 순서 보장을 위해 LinkedHashMap 사용
                ));
    }
        
}