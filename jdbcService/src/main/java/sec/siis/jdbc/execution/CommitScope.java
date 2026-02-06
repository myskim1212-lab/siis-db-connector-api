package sec.siis.jdbc.execution;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CommitScope {
    ALL,     // batch 트랜잭션
    ROW;      // row 단위 트랜잭션
    
    @JsonCreator
    public static CommitScope from(Object value) {
        if (value == null) return null;
        return CommitScope.valueOf(
            value.toString().trim().toUpperCase()
        );
    }    
}