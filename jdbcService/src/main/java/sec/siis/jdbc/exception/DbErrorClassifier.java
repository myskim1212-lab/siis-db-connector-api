package sec.siis.jdbc.exception;

public class DbErrorClassifier {

    public static String classify(String sqlState, String errorCode, String errorDetail) {
        String msg = (errorDetail != null) ? errorDetail.toLowerCase() : "";

        // 1. ErrorCode 기반 분류 (벤더별 상세 - 가장 정확하므로 최우선순위)
        if (errorCode != null) {
            try {
                int code = Integer.parseInt(errorCode);
                
                switch (code) {
                    // --- Oracle & Tibero 전용 ---
                    case 1797: return "Syntax: ORA-01797 (Operator must be followed by ANY or ALL)";
                    case 904:  return "Invalid Column: ORA-00904 (Invalid identifier)";
                    case 1:    return "Duplicate: Unique constraint violated";
                    case 942:  return "Missing: Table or view does not exist";
                    case 1017: return "Security: Invalid username/password";
                    case 1400: return "Missing: Mandatory field is null";
                    case 12899:return "Overflow: Data exceeds column maximum length";
                    case 2291: 
                    case 2292: return "Referential: Foreign key constraint error";
                    
                    // --- MySQL & MariaDB ---
                    case 1062: return "Duplicate: Entry already exists";
                    case 1146: return "Missing: Table doesn't exist";
                    case 1205: return "Timeout: Lock wait timeout exceeded";
                }
            } catch (NumberFormatException ignored) {}
        }

        // 2. SQLState 기반 분류 (표준 - ErrorCode가 매칭되지 않을 때 수행)
        if (sqlState != null) {
            if (sqlState.startsWith("08")) return "Network: Database connection lost or refused";
            if (sqlState.startsWith("23")) return "Constraint: Data integrity violation";
            if (sqlState.equals("40001"))  return "Transaction: Deadlock detected";
            if (sqlState.startsWith("28")) return "Security: Invalid permissions";
            if (sqlState.startsWith("42")) return "SQL Syntax or Object Access Error";
        }

        // 3. 문자열 패턴 매칭 (Fallback)
        if (msg.contains("any or all")) return "Syntax: ORA-01797 detected in message";
        if (msg.contains("permission") || msg.contains("denied")) return "Security: Access rights required";

        return "Database Error: " + (errorCode != null ? "Code " + errorCode : "General failure");
    }
}