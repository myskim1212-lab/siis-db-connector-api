package sec.siis.jdbc.strategy;

public enum FieldType {
    STRING,
    CHAR,     // DB의 CHAR 타입 지원을 위해 추가
    INT,
    LONG,
    DECIMAL,
    DATE,
    TIMESTAMP,
    CLOB,
    BLOB,
    BOOLEAN,
    DOUBLE,    // 추가
    FLOAT;     // 추가
	
    public static boolean isValid(String type) {
        if (type == null) return false;
        
        try {
            FieldType.valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Alias 지원 (선택적)
     * INTEGER → INT
     * BIGINT → LONG
     * VARCHAR → STRING
     * NUMERIC → DECIMAL
     */
    public static FieldType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        
        String normalized = type.toUpperCase();
        
        // Alias 처리
        switch (normalized) {
            case "INTEGER": return INT;
            case "BIGINT": return LONG;
            case "VARCHAR": return STRING;
            case "NUMERIC": return DECIMAL;
            default:
                return FieldType.valueOf(normalized);
        }
    }    
}