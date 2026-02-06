package sec.siis.jdbc.strategy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sec.siis.jdbc.config.EffectiveOperationConfig;
import sec.siis.jdbc.config.FieldConfig;
import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.logging.LogMessageManager;
import sec.siis.jdbc.util.CommonUtil;

public abstract class BaseDbStrategy implements DbStrategy {

    private static final Logger log = LoggerFactory.getLogger(BaseDbStrategy.class);
    private static final int STREAM_BUFFER_SIZE = 8192;
    private static final int CHAR_BUFFER_SIZE = 4096;
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":(\\w+)");
    
    private String msgID;
    
    BaseDbStrategy(String msdID){
    	this.msgID=msdID;
    }
    // ======================================================================
    // 1. Core Execution Methods (Update, Batch, Select)
    // ======================================================================

    @Override
    public int executeUpdate(Connection conn, EffectiveOperationConfig eop, List<Map<String, Object>> rows) throws Exception {
    	
    	int totalAffectedCount = 0;
        if (rows == null || rows.isEmpty()) {
            LogMessageManager.debugNoRowsToProcess(log, msgID, eop.getApi_name(), eop.getOperation_name());
            return totalAffectedCount;
        }
     
        for (Map<String, Object> row : rows) {
            // 행마다 데이터(특히 Collection)에 맞춰 SQL 동적 생성
            ParsedSql parsedSql = parseNamedParameters(eop.getSql(), row, eop.getFields());
            //LogMessageManager.debugExecSql(log, msgID, eop.getApi_name(), eop.getOperation_name(), parsedSql.sql);

            // SQL 중복 로깅 방지: EOP 객체에 상태 저장
            if (!eop.isSqlLogged()) {
                LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), parsedSql.sql);
                eop.setSqlLogged(true); // 한 번 찍으면 true로 변경
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(parsedSql.sql)) {
                bindParameters(pstmt, parsedSql, eop.getFields(), eop, row);
                int affectedCount=pstmt.executeUpdate();
                totalAffectedCount += affectedCount;
            }
        }
        //LogMessageManager.debugUpdatedRows(log, msgID, eop.getApi_name(), eop.getOperation_name(), rows.size());
        return totalAffectedCount;
    }

    @Override
    public int executeBatch(Connection conn, EffectiveOperationConfig eop, int batchSize, List<Map<String, Object>> rows) throws Exception {
    	
    	int totalAffectedCount=0; //실제 DB 행 변화 수 합계
    	 
        if (rows == null || rows.isEmpty()) return totalAffectedCount;

        // 1. 가변 IN 절(Collection) 여부 확인
        boolean hasCollection = rows.stream()
                .anyMatch(row -> row.values().stream().anyMatch(v -> v instanceof Collection));

        // 데이터가 가변적이면 Batch 대신 개별 Update 실행
        if (hasCollection) {
            //log.debug("Dynamic Collection detected. Diverting to executeUpdate.");
            // executeUpdate가 영향을 준 총 행수를 반환하도록 설계되어 있다면 그 값을 결과 객체에 담아야 함
            totalAffectedCount = executeUpdate(conn, eop, rows); 
            return totalAffectedCount;
        }

        //LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), eop.getSql());
        // SQL 중복 로깅 방지: EOP 객체에 상태 저장
        if (!eop.isSqlLogged()) {
            LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), eop.getSql());
            eop.setSqlLogged(true); // 한 번 찍으면 true로 변경
        }
        ParsedSql parsedSql = parseNamedParameters(eop.getSql(), rows.get(0), eop.getFields());
        
        int processedCount = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(parsedSql.sql)) {
            for (Map<String, Object> row : rows) {
                bindParameters(pstmt, parsedSql, eop.getFields(), eop, row);
                pstmt.addBatch();
                processedCount++;

                if (processedCount % batchSize == 0) {
                    int[] counts = pstmt.executeBatch();
                    totalAffectedCount += sumUpdateCounts(counts); // [추가] 합계 계산
                    pstmt.clearBatch();
                }
            }
            
            if (processedCount % batchSize != 0) {
                int[] counts = pstmt.executeBatch();
                totalAffectedCount += sumUpdateCounts(counts); // [추가] 합계 계산
            }
        }
        //LogMessageManager.debugOperationProcessed(log, msgID, eop.getApi_name(), eop.getOperation_name(), processedCount);
        return totalAffectedCount;
    }

    @Override
    public int executeSql(Connection conn, EffectiveOperationConfig eop) throws Exception {
    	
        //LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), eop.getSql());
        if (!eop.isSqlLogged()) {
            LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), eop.getSql());
            eop.setSqlLogged(true); // 한 번 찍으면 true로 변경
        }    	
        ParsedSql parsedSql = parseNamedParameters(eop.getSql(), new HashMap<>(), eop.getFields());
        try (PreparedStatement pstmt = conn.prepareStatement(parsedSql.sql)) {
            int affected = pstmt.executeUpdate();
            LogMessageManager.debugExecutedOperation(log, msgID, eop.getApi_name(), eop.getOperation_name(), affected);
            return affected;
        }
    }

    @Override
    public void executeProcedure(Connection conn, EffectiveOperationConfig eop, List<Map<String, Object>> rows) throws Exception {
        String callSql = String.format("{CALL %s}", eop.getSql());
        if (!eop.isSqlLogged()) {
            LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), callSql);
            eop.setSqlLogged(true); // 한 번 찍으면 true로 변경
        }        
        if (rows == null || rows.isEmpty()) {
            try (CallableStatement cstmt = conn.prepareCall(callSql)) {
                cstmt.execute();
            }
            return;
        }
        for (Map<String, Object> row : rows) {
            ParsedSql parsedSql = parseNamedParameters(callSql, row, eop.getFields());
            try (CallableStatement cstmt = conn.prepareCall(parsedSql.sql)) {
                bindParameters(cstmt, parsedSql, eop.getFields(), eop, row);
                cstmt.execute();
            }
        }
    }
    @Override
    public List<Map<String, Object>> executeSelect(Connection conn, EffectiveOperationConfig eop, List<Map<String, Object>> params) throws Exception {
        Map<String, String> columnToJsonMap = buildColumnToJsonMap(eop);
        
        // Select의 경우 첫번째 배열만 참조
        Map<String, Object> whereParams = (params != null && !params.isEmpty()) ? params.get(0) : new HashMap<>();

        // Select 파라미터에 맞게 SQL 동적 생성
        ParsedSql parsedSql = parseNamedParameters(eop.getSql(), whereParams, eop.getParamFields());

        //LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), parsedSql.sql);
        
        if (!eop.isSqlLogged()) {
            LogMessageManager.debugSqlStatement(log, msgID, eop.getApi_name(), eop.getOperation_name(), parsedSql.sql);
            eop.setSqlLogged(true); // 한 번 찍으면 true로 변경
        }    

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(parsedSql.sql)) {
            if (eop.getRow_limit() > 0) pstmt.setMaxRows(eop.getRow_limit());

            if (!whereParams.isEmpty()) {
            	bindParameters(pstmt, parsedSql, eop.getWhereParamFields(), eop, whereParams);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(extractRow(rs, columnToJsonMap,eop));
                }
            }
        }
        LogMessageManager.debugSelectReturned(log, msgID, eop.getApi_name(), eop.getOperation_name(), result.size());
        return result;
    }

    // ======================================================================
    // 2. Dynamic SQL Parsing & Parameter Binding
    // ======================================================================

    protected ParsedSql parseNamedParameters(String sql, Map<String, Object> row, List<FieldConfig> fieldConfigs) {
        List<String> paramNames = new ArrayList<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        // FieldConfig의 param과 data_field 매핑 준비
        Map<String, String> paramToDataField = (fieldConfigs == null) ? new HashMap<>() :
                fieldConfigs.stream().collect(Collectors.toMap(FieldConfig::getParam, FieldConfig::getData_field, (a, b) -> a));

        while (matcher.find()) {
            sb.append(sql, lastEnd, matcher.start());
            String paramName = matcher.group(1);
            String dataField = paramToDataField.get(paramName);
            Object value = (dataField != null) ? row.get(dataField) : null;

            // Collection인 경우 "IN (?, ?, ?)" 형태로 확장
            // Config에서 IN 절은 반드시 ( ) 붙여서 정의해야 한다.
            if (value instanceof Collection<?> col) {
                int size = col.size();
                if (size == 0) {
                    // 빈 리스트일 때 IN () 에러 방지를 위해 NULL 문자열 직접 삽입
                    sb.append("NULL"); 
                } else {
                    for (int i = 0; i < size; i++) {
                        sb.append("?");
                        paramNames.add(paramName);
                        if (i < size - 1) sb.append(", ");
                    }
                }
            } else {
                // value가 null인 경우 포함 (기존 로직 유지)
                sb.append("?");
                paramNames.add(paramName);
            }
            lastEnd = matcher.end();
        }
        sb.append(sql.substring(lastEnd));
        return new ParsedSql(sb.toString(), paramNames);
    }

    protected void bindParameters(PreparedStatement pstmt, ParsedSql parsedSql, List<FieldConfig> fieldConfigs, 
                                  EffectiveOperationConfig eop, Map<String, Object> row) throws SQLException {
        if (fieldConfigs == null) return;

        Map<String, FieldConfig> fieldMap = fieldConfigs.stream()
                .collect(Collectors.toMap(FieldConfig::getParam, fc -> fc, (a, b) -> a));

        Map<String, Integer> listIndexTracker = new HashMap<>(); // 리스트 파라미터 내 현재 위치 추적
        int jdbcIndex = 1;

        for (String paramName : parsedSql.paramNames) {
            FieldConfig fc = fieldMap.get(paramName);
            if (fc == null) {
                pstmt.setObject(jdbcIndex++, null);
                continue;
            }

            Object value = row.get(fc.getData_field());
            int sqlType = getSqlType(FieldType.fromString(fc.getType()));

            if (value instanceof List<?> list) {
                int currentIdx = listIndexTracker.getOrDefault(paramName, 0);
                Object singleValue = (currentIdx < list.size()) ? list.get(currentIdx) : null;
                bindParameter(pstmt, jdbcIndex++, eop, singleValue, sqlType);
                listIndexTracker.put(paramName, currentIdx + 1);
            } else {
                bindParameter(pstmt, jdbcIndex++, eop, value, sqlType);
            }
        }
    }

    protected void bindParameter(PreparedStatement pstmt, int index, EffectiveOperationConfig config, Object value, int sqlType) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, sqlType);
            return;
        }

        if (sqlType == Types.DATE && value instanceof String str) {
            pstmt.setDate(index, (java.sql.Date) CommonUtil.TemporalParse(str, Types.DATE, config.getDate_formats()));
        } else if (sqlType == Types.TIMESTAMP && value instanceof String str) {
            pstmt.setTimestamp(index, (Timestamp) CommonUtil.TemporalParse(str, Types.TIMESTAMP, config.getTimestamp_formats()));
        } else {
            pstmt.setObject(index, value, sqlType);
        }
    }

    // ======================================================================
    // 3. ResultSet & Helper Methods
    // ======================================================================

//    protected Map<String, Object> extractRow(ResultSet rs, Map<String, String> columnToJsonMap,JdbcConfig jcfg) throws SQLException {
//        ResultSetMetaData meta = rs.getMetaData();
//        int count = meta.getColumnCount();
//        Map<String, Object> row = new LinkedHashMap<>(count);
//
//        for (int i = 1; i <= count; i++) {
//            String label = meta.getColumnLabel(i);
//            Object value = extractValueByType(rs, i, meta.getColumnType(i),jcfg);
//            // DB 컬럼명을 Json 필드명으로 변환하여 설정
//            row.put(columnToJsonMap.getOrDefault(label, label), value);
//        }
//        return row;
//    }

    protected Map<String, Object> extractRow(ResultSet rs, Map<String, String> columnToJsonMap, EffectiveOperationConfig eop) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        Map<String, Object> row = new LinkedHashMap<>(count);

        // 1. 빠른 조회를 위해 FieldConfig 매핑 (Param명 기준)
     // 결과 매핑용 필드들만 필터링하여 Map 생성
        Map<String, FieldConfig> resultFieldMap = eop.getResultMappingFields().stream()
                .collect(Collectors.toMap(
                    FieldConfig::getParam, // DB 컬럼명 혹은 매핑될 파라미터명
                    fc -> fc, 
                    (a, b) -> a
                ));
        
        for (int i = 1; i <= count; i++) {
            String label = meta.getColumnLabel(i);
            
            // 2. 우선순위 결정: 설정된 타입이 있는가?
            FieldConfig fc = resultFieldMap.get(label);
            
            // YAML의 fields에 정의되지 않았거나, Direction이 IN인 필드는 결과 JSON에서 제외
            if (fc == null) {
                continue; 
            }
                        
            int targetType = (fc.getType() != null) 
                    ? getSqlType(FieldType.fromString(fc.getType())) 
                    : meta.getColumnType(i);

            Object value = extractValueByType(rs, i, targetType, eop.getJdbcConfig());
            
            // DB 컬럼명을 Json 필드명으로 변환하여 설정
            row.put(columnToJsonMap.getOrDefault(label, label), value);
        }
        return row;
    }
    
    protected Object extractValueByType(ResultSet rs, int index, int type, JdbcConfig jcfg) throws SQLException {
        return switch (type) {
	        case Types.CHAR -> {
	            String val = rs.getString(index);
	            yield (val != null) ? val.trim() : ""; // null 보단 빈 문자열이 안전할 수 있음
	        }
	        
	        case Types.CLOB, Types.NCLOB -> extractClob(rs, index);
            case Types.BLOB ->extractBlob(rs, index);
            
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                Timestamp ts = rs.getTimestamp(index);
                if (ts == null) yield null;
                
                // 핵심 수정: toLocalDateTime() 대신 ZonedDateTime을 사용하여 타임존(XXX) 대응
                yield ts.toInstant()
                        .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대 적용
                        .format(jcfg.getPrimaryTimestampFormatter()); // JDBCConfig 로딩 시점에 Formatter 설정
            }     
            case Types.DATE -> {
                Date date = rs.getDate(index);
                if (date == null) yield null;
                yield date.toLocalDate().format(jcfg.getPrimaryDateFormatter());  // JDBCConfig 로딩 시점에 Formatter 설정
            }            
            
            default -> rs.getObject(index);
        };
    }

 // LOB 추출 및 해제를 담당하는 헬퍼 메서드들
    private String extractClob(ResultSet rs, int index) throws SQLException {
        Clob clob = rs.getClob(index);
        if (clob == null) return null;
        try {
            return clobToString(clob);
        } finally {
            try { clob.free(); } catch (SQLException ignore) {}
        }
    }

    private String extractBlob(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        if (blob == null) return null;
        try {
            return blobToBase64(blob);
        } finally {
            try { blob.free(); } catch (SQLException ignore) {}
        }
    }
    
    private Map<String, String> buildColumnToJsonMap(EffectiveOperationConfig eop) {
        if (eop.getFields() == null) return new HashMap<>();
        return eop.getResultMappingFields().stream()
                .collect(Collectors.toMap(FieldConfig::getParam, FieldConfig::getData_field, (a, b) -> a));
    }

    protected static class ParsedSql {
        final String sql;
        final List<String> paramNames;
        ParsedSql(String sql, List<String> paramNames) { this.sql = sql; this.paramNames = paramNames; }
    }
    
    private static String blobToBase64(Blob blob) {
        if (blob == null) {
            return null;
        }

        try (InputStream inputStream = blob.getBinaryStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            //LogMessageManager.errorFailedConvertBlob(log, msgID, e);
            throw new RuntimeException("Failed to convert BLOB to Base64", e);
        }
    }    
    
    private static String clobToString(Clob clob) {
        if (clob == null) {
            return null;
        }

        try (Reader reader = clob.getCharacterStream();
             StringWriter writer = new StringWriter()) {

            char[] buffer = new char[CHAR_BUFFER_SIZE];
            int length;

            while ((length = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, length);
            }

            return writer.toString();

        } catch (Exception e) {
            //LogMessageManager.errorFailedReadClob(log, msgID, e);
            throw new RuntimeException("Failed to read CLOB", e);
        }
    }   

    @Override
    public int getSqlType(FieldType fieldType) {
        if (fieldType == null) {
            LogMessageManager.warnSqlTypeNull(log);
            return Types.VARCHAR;
        }
        
        switch (fieldType) {
            case INT:
                return Types.INTEGER;
            case LONG:
                return Types.BIGINT;
            case DOUBLE:
                return Types.DOUBLE;
            case FLOAT:
                return Types.FLOAT;
            case STRING:
                return Types.VARCHAR;
            case CHAR:
                return Types.CHAR;                
            case DATE:
                return Types.DATE;
            case TIMESTAMP:
                return Types.TIMESTAMP;
            case BLOB:
                return Types.BLOB;
            case CLOB:
                return Types.CLOB;
            case BOOLEAN:
                return Types.BOOLEAN;
            case DECIMAL:
                return Types.DECIMAL;
            default:
                LogMessageManager.warnUnknownSqlType(log, msgID, fieldType.name());
                return Types.VARCHAR;
        }
    }

    private int sumUpdateCounts(int[] counts) {
        int sum = 0;
        for (int count : counts) {
            if (count >= 0) {
                sum += count;
            } else if (count == PreparedStatement.SUCCESS_NO_INFO) {
                // 영향을 받은 행 수를 알 수 없는 경우, 최소 1건으로 간주하거나 로그를 남김
                sum += 1; 
            }
        }
        return sum;
    }
}
