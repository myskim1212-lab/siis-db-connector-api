package sec.siis.jdbc.config;

import java.util.List;
import java.util.Set;

import sec.siis.jdbc.execution.CommitAction;
import sec.siis.jdbc.execution.CommitScope;

/**
 * JDBC Config 기본값 정의
 */
public class JdbcConfigDefaults {
    
    // ========== Global Defaults ==========
    
    /**
     * 기본 Batch 크기
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    /**
     * 기본 재시도 횟수
     */
    public static final int DEFAULT_RETRY_COUNT = 0;
    
    /**
     * 기본 최대 조회 건수 (SELECT)
     */
    public static final int DEFAULT_MAX_ROW_LIMIT = 100000;
    
    /**
     * 기본 데이터 레코드 경로
     */
    public static final String DEFAULT_DATA_RECORD_PATH = "/";
    
    /**
     * 기본 Operation 실패 시 중단 여부
     */
    public static final boolean DEFAULT_STOP_ON_OPERATION_ERROR = true;
    
    /**
     * 기본 Row 실패 시 중단 여부
     */
    public static final boolean DEFAULT_STOP_ON_ROW_ERROR = true;
    
    /**
     * 기본 날짜 포맷 목록
     */
    public static final List<String> DEFAULT_DATE_FORMATS = List.of(
        "yyyy-MM-dd",
        "yyyyMMdd",
        "yyyy/MM/dd"
    );
    
    /**
     * 기본 타임스탬프 포맷 목록
     */
    public static final List<String> DEFAULT_TIMESTAMP_FORMATS = List.of(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyyMMddHHmmss",
        "yyyyMMddHHmmssSSS",
        "yyyy-MM-dd HH:mm:ss.SSS"
    );
    
    // ========== Operation Defaults ==========
    
    /**
     * 기본 Commit Action
     */
    public static final CommitAction DEFAULT_COMMIT_ACTION = CommitAction.CONTINUE;
    
    /**
     * 기본 Commit Scope
     */
    public static final CommitScope DEFAULT_COMMIT_SCOPE = CommitScope.ALL;
    
    /**
     * 기본 has_detail 값
     */
    public static final boolean DEFAULT_HAS_DETAIL = false;
    
    // ========== Validation Constants ==========
    
    /**
     * 유효한 Operation 타입
     */
    public static final Set<String> VALID_OPERATION_TYPES = Set.of(
        "insert",
        "update",
        "delete",
        "select",
        "procedure",
        "insert&update"
    );
    
    /**
     * ROW Scope 허용 타입
     */
    public static final Set<String> ROW_SCOPE_ALLOWED_TYPES = Set.of(
        "insert",
        "insert&update"
    );
    
    /**
     * SQL이 필수가 아닌 타입
     */
    public static final Set<String> SQL_OPTIONAL_TYPES = Set.of(
        "procedure"
    );
    
    // ========== Helper Methods ==========
    
    /**
     * Operation 타입이 유효한지 검증
     */
    public static boolean isValidOperationType(String type) {
        if (type == null) return false;
        return VALID_OPERATION_TYPES.contains(type.toLowerCase());
    }
    
    /**
     * ROW Scope 사용 가능한 타입인지 검증
     */
    public static boolean isRowScopeAllowed(String type) {
        if (type == null) return false;
        return ROW_SCOPE_ALLOWED_TYPES.contains(type.toLowerCase());
    }
    
    /**
     * SQL이 필수인 타입인지 검증
     */
    public static boolean isSqlRequired(String type) {
        if (type == null) return true;
        return !SQL_OPTIONAL_TYPES.contains(type.toLowerCase());
    }
    
    /**
     * 지원되는 Operation 타입 목록 문자열 반환
     */
    public static String getSupportedOperationTypes() {
        return String.join(", ", VALID_OPERATION_TYPES);
    }
    
    // Private constructor to prevent instantiation
    private JdbcConfigDefaults() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}