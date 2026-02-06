package sec.siis.jdbc.strategy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;

import sec.siis.jdbc.config.EffectiveOperationConfig;
import sec.siis.jdbc.util.CommonUtil;

public class OracleStrategy extends BaseDbStrategy {
	
    private String msgID;
    
	public OracleStrategy(String msgID) {

        super(msgID); 
    }
	
    @Override
    public DbType getDbType() {
        return DbType.ORACLE;
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
        
        // DATE 처리
        if (sqlType == Types.DATE && value instanceof String) {
            Object convertedValue = CommonUtil.TemporalParse(
                (String) value,
                Types.DATE,
                config.getDate_formats()
            );
            pstmt.setDate(index, (java.sql.Date) convertedValue);
            return;
        }
        
        // TIMESTAMP 처리
        if (sqlType == Types.TIMESTAMP && value instanceof String) {
            Object convertedValue = CommonUtil.TemporalParse(
                (String) value,
                Types.TIMESTAMP,
                config.getTimestamp_formats()
            );
            pstmt.setTimestamp(index, (java.sql.Timestamp) convertedValue);
            return;
        }
        
        // BLOB 처리
        if (sqlType == Types.BLOB) {
            if (value instanceof byte[]) {
                byte[] bytes = (byte[]) value;
                pstmt.setBlob(index, new ByteArrayInputStream(bytes), bytes.length);
            } else if (value instanceof InputStream) {
                pstmt.setBlob(index, (InputStream) value);
            } else if (value instanceof String) {
                // Base64 디코딩
                byte[] decoded = Base64.getDecoder().decode((String) value);
                pstmt.setBlob(index, new ByteArrayInputStream(decoded), decoded.length);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported BLOB value type: " + value.getClass()
                );
            }
            return;
        }
        
        // CLOB 처리
        if (sqlType == Types.CLOB) {
            if (value instanceof String) {
                String str = (String) value;
                pstmt.setClob(index, new StringReader(str), str.length());
            } else if (value instanceof Reader) {
                pstmt.setClob(index, (Reader) value);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported CLOB value type: " + value.getClass()
                );
            }
            return;
        }
        
        // 기본 처리는 부모 클래스에 위임
        super.bindParameter(pstmt, index, config, value, sqlType);
    }
}