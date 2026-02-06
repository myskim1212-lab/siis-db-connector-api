package sec.siis.jdbc.config;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ActionType {
    INSERT,
    UPDATE,    // 단일 수정이 필요한 경우 대비
    DELETE,
    UPSERT,    // 또는 MERGE (사용하시는 DB 용어에 따라 선택)
    SELECT,    // 데이터를 조회하여 다음 단계로 넘기는 경우 필요
    PROCEDURE; // 프로시저 호출용
    
    @JsonCreator
    public static ActionType fromString(String value) {
        if (value == null) return null;
        return ActionType.valueOf(value.toUpperCase()); // 소문자를 대문자로 변환
    }    
}