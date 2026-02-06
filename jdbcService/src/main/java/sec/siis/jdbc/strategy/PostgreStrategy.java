package sec.siis.jdbc.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;

import sec.siis.jdbc.config.EffectiveOperationConfig;
import sec.siis.jdbc.util.CommonUtil;

public class PostgreStrategy extends BaseDbStrategy {

    private String msgID;
    
	public PostgreStrategy(String msgID) {

        super(msgID); 
    }
	
	@Override
	public DbType getDbType() {
		return DbType.POSTGRES;
	}

	// PostgreStrategy.java 에서 오버라이딩
	@Override
	protected void bindParameter(
	        PreparedStatement pstmt,
	        int index,
	        EffectiveOperationConfig config,
	        Object value,
	        int sqlType) throws SQLException {

	    if (value == null) {
	        pstmt.setNull(index, sqlType);
	        return;
	    }

	    // 1. DATE 처리
	    if (sqlType == Types.DATE && value instanceof String) {
	        Object convertedValue = CommonUtil.TemporalParse((String) value, Types.DATE, config.getDate_formats());
	        pstmt.setDate(index, (java.sql.Date) convertedValue);
	        return;
	    }

	    // 2. TIMESTAMP 처리 (PostgreSQL 밀리초 정밀도 유지 전략)
	    if (sqlType == Types.TIMESTAMP && value instanceof String) {
	        Object convertedValue = CommonUtil.TemporalParse((String) value, Types.TIMESTAMP, config.getTimestamp_formats());
	        if (convertedValue instanceof java.sql.Timestamp) {
	            // PostgreSQL 드라이버가 밀리초(.SSS)를 누락하지 않도록 setObject에 타입을 명시합니다.
	            pstmt.setObject(index, convertedValue, Types.TIMESTAMP);
	        }
	        return;
	    }

	    // 3. BLOB 처리 (PostgreSQL은 주로 bytea 타입을 사용)
	    if (sqlType == Types.BLOB) {
	        byte[] bytes;
	        if (value instanceof byte[]) {
	            bytes = (byte[]) value;
	        } else if (value instanceof String) {
	            bytes = Base64.getDecoder().decode((String) value);
	        } else if (value instanceof InputStream) {
	            try {
	                bytes = ((InputStream) value).readAllBytes();
	            } catch (IOException e) {
	                throw new SQLException("Failed to read InputStream for BLOB", e);
	            }
	        } else {
	            throw new IllegalArgumentException("Unsupported BLOB value type: " + value.getClass());
	        }
	        // PostgreSQL bytea 컬럼은 setBytes가 가장 안정적입니다.
	        pstmt.setBytes(index, bytes); 
	        return;
	    }

	    // 4. CLOB 처리 (PostgreSQL은 주로 text 타입을 사용)
	    if (sqlType == Types.CLOB) {
	        if (value instanceof String) {
	            // PostgreSQL text 타입은 단순setString으로 처리하는 것이 효율적입니다.
	            pstmt.setString(index, (String) value);
	        } else if (value instanceof Reader) {
	            // Reader를 String으로 변환하거나 setCharacterStream 사용
	            pstmt.setCharacterStream(index, (Reader) value);
	        } else {
	            throw new IllegalArgumentException("Unsupported CLOB value type: " + value.getClass());
	        }
	        return;
	    }

	    // 5. 그 외 기본 타입은 부모 클래스(BaseDbStrategy)에 위임
	    super.bindParameter(pstmt, index, config, value, sqlType);
	}
	
}
