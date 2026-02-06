package sec.siis.jdbc.strategy;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import sec.siis.jdbc.config.EffectiveOperationConfig;


public interface DbStrategy {

    DbType getDbType();

    /** LOB 바인딩 전략 */
//    void bindParameter(
//        MapSqlParameterSource ps,
//        EffectiveOperationConfig eop,
//        String column,
//        Object value,
//        int sqlType
//    );

    int executeUpdate(
            Connection conn,
            EffectiveOperationConfig eop,
            List<Map<String, Object>> rows) throws Exception;

    int executeSql(
            Connection conn,
            EffectiveOperationConfig eop) throws Exception;

    int executeBatch(
            Connection conn,
            EffectiveOperationConfig eop,
            int batchSize,
            List<Map<String, Object>> rows) throws Exception;

    List<Map<String, Object>> executeSelect(
            Connection conn,
            EffectiveOperationConfig eop,
            List<Map<String, Object>> params) throws Exception;

    void executeProcedure(
            Connection conn,
            EffectiveOperationConfig eop,
            List<Map<String, Object>> rows) throws Exception;
    
    //int getSqlType(String sqlType);
    int getSqlType(FieldType fieldType);
    
}