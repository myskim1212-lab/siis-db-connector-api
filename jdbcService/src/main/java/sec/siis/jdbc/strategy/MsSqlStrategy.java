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

public class MsSqlStrategy extends BaseDbStrategy {

    private String msgID;
    
	public MsSqlStrategy(String msgID) {

        super(msgID); 
    }
	
	@Override
	public DbType getDbType() {
		return DbType.MSSQL;
	}

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

	    // 2. TIMESTAMP 처리 (MSSQL DATETIME2 정밀도 유지)
	    if (sqlType == Types.TIMESTAMP && value instanceof String) {
	        Object convertedValue = CommonUtil.TemporalParse((String) value, Types.TIMESTAMP, config.getTimestamp_formats());
	        if (convertedValue instanceof java.sql.Timestamp) {
	            /* * MSSQL의 DATETIME은 3.33ms 단위로 반올림되는 고질적인 문제가 있습니다.
	             * 0.001초 단위(밀리초)를 정확히 저장하려면 DB 컬럼이 DATETIME2여야 하며,
	             * 바인딩 시에도 명시적으로 TIMESTAMP 타입을 지정하는 것이 안전합니다.
	             */
	            pstmt.setTimestamp(index, (java.sql.Timestamp) convertedValue);
	        }
	        return;
	    }

	    // 3. BLOB 처리 (MSSQL VARBINARY(MAX) 대응)
	    if (sqlType == Types.BLOB) {
	        if (value instanceof byte[]) {
	            pstmt.setBytes(index, (byte[]) value);
	        } else if (value instanceof String) {
	            pstmt.setBytes(index, Base64.getDecoder().decode((String) value));
	        } else if (value instanceof InputStream) {
	            // MSSQL은 스트림 바인딩 시 길이를 지정하는 것을 선호합니다.
	            try {
	                byte[] bytes = ((InputStream) value).readAllBytes();
	                pstmt.setBytes(index, bytes);
	            } catch (IOException e) {
	                throw new SQLException("Failed to read InputStream for MSSQL BLOB", e);
	            }
	        }
	        return;
	    }

	    // 4. CLOB 처리 (MSSQL VARCHAR(MAX) 또는 NVARCHAR(MAX) 대응)
	    if (sqlType == Types.CLOB) {
	        if (value instanceof String) {
	            // MSSQL은 setString으로 MAX 사이즈 데이터를 보낼 수 있습니다.
	            pstmt.setString(index, (String) value);
	        } else if (value instanceof Reader) {
	            pstmt.setCharacterStream(index, (Reader) value);
	        }
	        return;
	    }

	    // 5. 기본 처리는 부모 클래스에 위임
	    super.bindParameter(pstmt, index, config, value, sqlType);
	}
// 대부분 기본 구현 그대로 사용
}
