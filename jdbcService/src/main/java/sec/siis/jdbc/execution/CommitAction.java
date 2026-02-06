package sec.siis.jdbc.execution;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CommitAction {
	COMMIT,     // 여기까지 묶어서 커밋
	CONTINUE;    // 다음 operation에게 트랜잭션 제어 넘김
	
    @JsonCreator
    public static CommitAction from(Object value) {
        if (value == null) return null;
        return CommitAction.valueOf(
            value.toString().trim().toUpperCase()
        );
    }	
}
