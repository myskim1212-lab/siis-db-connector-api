package sec.siis.jdbc.config;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Direction {
    IN,
    OUT,
    INOUT;
    
    @JsonCreator
    public static Direction fromString(String value) {
        if (value == null) return null;
        return Direction.valueOf(value.toUpperCase()); // 소문자를 대문자로 변환
    }    
}