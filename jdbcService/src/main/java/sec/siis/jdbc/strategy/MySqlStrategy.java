package sec.siis.jdbc.strategy;

import sec.siis.jdbc.config.EffectiveOperationConfig;

public class MySqlStrategy extends BaseDbStrategy {

    private String msgID;
    
	public MySqlStrategy(String msgID) {

        super(msgID); 
    }
	
	@Override
	public DbType getDbType() {
		return DbType.MYSQL;
	}

// 대부분 기본 구현 그대로 사용
}
