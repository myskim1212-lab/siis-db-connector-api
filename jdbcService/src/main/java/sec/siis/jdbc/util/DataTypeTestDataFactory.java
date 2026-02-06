package sec.siis.jdbc.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 데이터 타입 테스트용 데이터 생성
 */
public class DataTypeTestDataFactory {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 모든 데이터 타입을 포함한 테스트 데이터 생성
     */
    public static String createAllTypesTestData() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> testData = new ArrayList<>();

        // ========== 1. 숫자형 테스트 데이터 ==========
        Map<String, Object> numericTest = new LinkedHashMap<>();
        numericTest.put("testId", 1001);
        numericTest.put("colNumber", 12345.6789);
        numericTest.put("colNumber10", 1234567890);
        numericTest.put("colNumber10_2", 12345678.90);
        numericTest.put("colInteger", 999999);
        numericTest.put("colFloat", 3.14159265359);
        numericTest.put("colDecimal", 123456789.123);
        numericTest.put("colNumeric", 12345678.1234);
        numericTest.put("colVarchar50", "Numeric Test");
        numericTest.put("colVarchar200", "Testing numeric data types");
        numericTest.put("colVarchar4000", "Short text");
        numericTest.put("colNvarchar50", "유니코드 숫자 테스트");
        numericTest.put("colChar10", "CHAR10");
        numericTest.put("colNchar10", "고정길이");
        numericTest.put("colClob", "CLOB data for numeric test");
        numericTest.put("colNclob", "NCLOB data");
        numericTest.put("colDate", "2026-01-16");
        numericTest.put("colTimestamp", "2026-01-16 10:30:45.123");
        numericTest.put("colRaw100", "DEADBEEF");
        numericTest.put("colBoolean", 1);
        numericTest.put("colJson", "{\"test\":\"numeric\",\"value\":123}");
        numericTest.put("testCategory", "NUMERIC_TEST");
        numericTest.put("testDescription", "숫자형 데이터 타입 종합 테스트");
        numericTest.put("createdBy", "JUNIT_TEST");
        numericTest.put("updatedBy", "JUNIT_TEST");
        testData.add(numericTest);

        // ========== 2. 문자형 테스트 데이터 ==========
        Map<String, Object> stringTest = new LinkedHashMap<>();
        stringTest.put("testId", 1002);
        stringTest.put("colNumber", null);
        stringTest.put("colNumber10", null);
        stringTest.put("colNumber10_2", null);
        stringTest.put("colInteger", null);
        stringTest.put("colFloat", null);
        stringTest.put("colDecimal", null);
        stringTest.put("colNumeric", null);
        stringTest.put("colVarchar50", "한글 English 日本語 中文");
        stringTest.put("colVarchar200", 
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        stringTest.put("colVarchar4000", generateLongString(500));
        stringTest.put("colNvarchar50", "유니코드 문자열 テスト 测试");
        stringTest.put("colChar10", "FIXED");
        stringTest.put("colNchar10", "고정10");
        stringTest.put("colClob", generateLongString(1000));
        stringTest.put("colNclob", "대용량 유니코드 텍스트 " + generateLongString(500));
        stringTest.put("colDate", "2026-12-31");
        stringTest.put("colTimestamp", "2026-12-31 23:59:59.999");
        stringTest.put("colRaw100", "CAFEBABE");
        stringTest.put("colBoolean", 0);
        stringTest.put("colJson", "{\"test\":\"string\",\"한글\":\"가능\"}");
        stringTest.put("testCategory", "STRING_TEST");
        stringTest.put("testDescription", "문자형 데이터 타입 종합 테스트");
        stringTest.put("createdBy", "JUNIT_TEST");
        stringTest.put("updatedBy", "JUNIT_TEST");
        testData.add(stringTest);

        // ========== 3. 날짜/시간 테스트 데이터 ==========
        Map<String, Object> datetimeTest = new LinkedHashMap<>();
        datetimeTest.put("testId", 1003);
        datetimeTest.put("colNumber", 42);
        datetimeTest.put("colNumber10", 2026);
        datetimeTest.put("colNumber10_2", 1.16);
        datetimeTest.put("colInteger", 20260116);
        datetimeTest.put("colFloat", null);
        datetimeTest.put("colDecimal", null);
        datetimeTest.put("colNumeric", null);
        datetimeTest.put("colVarchar50", "DateTime Test");
        datetimeTest.put("colVarchar200", "Testing date and time data types");
        datetimeTest.put("colVarchar4000", null);
        datetimeTest.put("colNvarchar50", "날짜시간 테스트");
        datetimeTest.put("colChar10", "2026-01-16");
        datetimeTest.put("colNchar10", null);
        datetimeTest.put("colClob", "Date test CLOB");
        datetimeTest.put("colNclob", null);
        datetimeTest.put("colDate", "2026-01-16 14:30:45");
        datetimeTest.put("colTimestamp", "2026-01-16 14:30:45.123");
        datetimeTest.put("colRaw100", "20260116");
        datetimeTest.put("colBoolean", 1);
        datetimeTest.put("colJson", "{\"date\":\"2026-01-16\",\"timestamp\":\"2026-01-16T14:30:45.123Z\"}");
        datetimeTest.put("testCategory", "DATETIME_TEST");
        datetimeTest.put("testDescription", "날짜/시간 데이터 타입 테스트");
        datetimeTest.put("createdBy", "JUNIT_TEST");
        datetimeTest.put("updatedBy", "JUNIT_TEST");
        testData.add(datetimeTest);

        // ========== 4. NULL 값 테스트 데이터 ==========
        Map<String, Object> nullTest = new LinkedHashMap<>();
        nullTest.put("testId", 1004);
        nullTest.put("colNumber", null);
        nullTest.put("colNumber10", null);
        nullTest.put("colNumber10_2", null);
        nullTest.put("colInteger", null);
        nullTest.put("colFloat", null);
        nullTest.put("colDecimal", null);
        nullTest.put("colNumeric", null);
        nullTest.put("colVarchar50", null);
        nullTest.put("colVarchar200", null);
        nullTest.put("colVarchar4000", null);
        nullTest.put("colNvarchar50", null);
        nullTest.put("colChar10", null);
        nullTest.put("colNchar10", null);
        nullTest.put("colClob", null);
        nullTest.put("colNclob", null);
        nullTest.put("colDate", null);
        nullTest.put("colTimestamp", null);
        nullTest.put("colRaw100", null);
        nullTest.put("colBoolean", null);
        nullTest.put("colJson", null);
        nullTest.put("testCategory", "NULL_TEST");
        nullTest.put("testDescription", "NULL 값 처리 테스트");
        nullTest.put("createdBy", "JUNIT_TEST");
        nullTest.put("updatedBy", null);
        testData.add(nullTest);

        // ========== 5. 경계값 테스트 데이터 ==========
        Map<String, Object> boundaryTest = new LinkedHashMap<>();
        boundaryTest.put("testId", 1005);
        boundaryTest.put("colNumber", 999999999999999.999);
        boundaryTest.put("colNumber10", 9999999999L);
        boundaryTest.put("colNumber10_2", 99999999.99);
        boundaryTest.put("colInteger", 2147483647);  // Integer.MAX_VALUE
        boundaryTest.put("colFloat", Double.MAX_VALUE);
        boundaryTest.put("colDecimal", 999999999999.999);
        boundaryTest.put("colNumeric", 99999999.9999);
        boundaryTest.put("colVarchar50", generateString('X', 50));  // 최대 길이
        boundaryTest.put("colVarchar200", generateString('A', 200));
        boundaryTest.put("colVarchar4000", generateString('B', 4000));
        boundaryTest.put("colNvarchar50", generateString('가', 50));
        boundaryTest.put("colChar10", "CHAR10");
        boundaryTest.put("colNchar10", "고정길이10");
        boundaryTest.put("colClob", generateLongString(5000));
        boundaryTest.put("colNclob", "대용량" + generateLongString(3000));
        boundaryTest.put("colDate", "9999-12-31");
        boundaryTest.put("colTimestamp", "9999-12-31 23:59:59.999");
        boundaryTest.put("colRaw100", generateHexString(100));
        boundaryTest.put("colBoolean", 1);
        boundaryTest.put("colJson", "{\"max\":true}");
        boundaryTest.put("testCategory", "BOUNDARY_TEST");
        boundaryTest.put("testDescription", "경계값 테스트");
        boundaryTest.put("createdBy", "JUNIT_TEST");
        boundaryTest.put("updatedBy", "JUNIT_TEST");
        testData.add(boundaryTest);

        // ========== 6. 특수문자 테스트 데이터 ==========
        Map<String, Object> specialCharTest = new LinkedHashMap<>();
        specialCharTest.put("testId", 1006);
        specialCharTest.put("colNumber", 0);
        specialCharTest.put("colNumber10", 0);
        specialCharTest.put("colNumber10_2", 0.0);
        specialCharTest.put("colInteger", 0);
        specialCharTest.put("colFloat", 0.0);
        specialCharTest.put("colDecimal", 0.0);
        specialCharTest.put("colNumeric", 0.0);
        specialCharTest.put("colVarchar50", "!@#$%^&*()_+-=[]{}|;:',.<>?/");
        specialCharTest.put("colVarchar200", "Special: \"quoted\", 'apostrophe', \\backslash, /slash");
        specialCharTest.put("colVarchar4000", "Newline:\nTab:\tCarriage Return:\r");
        specialCharTest.put("colNvarchar50", "특수문자: ♠♣♥♦ ★☆ ©®™");
        specialCharTest.put("colChar10", "SP CHAR");
        specialCharTest.put("colNchar10", "특수!@#");
        specialCharTest.put("colClob", "CLOB with special chars: <>\"'&\n\t");
        specialCharTest.put("colNclob", null);
        specialCharTest.put("colDate", "2026-01-16");
        specialCharTest.put("colTimestamp", "2026-01-16 12:00:00.000");
        specialCharTest.put("colRaw100", "FFEEDDCCBBAA");
        specialCharTest.put("colBoolean", 0);
        specialCharTest.put("colJson", "{\"special\":\"!@#$%\",\"unicode\":\"♠♣\"}");
        specialCharTest.put("testCategory", "SPECIAL_CHAR_TEST");
        specialCharTest.put("testDescription", "특수문자 및 제어문자 테스트");
        specialCharTest.put("createdBy", "JUNIT_TEST");
        specialCharTest.put("updatedBy", "JUNIT_TEST");
        testData.add(specialCharTest);

        root.put("test_data", testData);
        return mapper.writeValueAsString(root);
    }

    /**
     * SELECT 쿼리용 검색 파라미터 생성
     */
    public static String createSelectParams(String testCategory, Integer testId) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> searchParams = new ArrayList<>();
        
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("testCategory", testCategory);
        param.put("testId", testId);
        
        searchParams.add(param);
        root.put("search_params", searchParams);
        
        return mapper.writeValueAsString(root);
    }

    /**
     * UPDATE용 데이터 생성
     */
    public static String createUpdateData(int testId) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> updateData = new ArrayList<>();
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("testId", testId);
        data.put("colNumber", 99999.9999);
        data.put("colVarchar50", "Updated Value");
        data.put("colDate", "2026-12-31");
        data.put("colTimestamp", "2026-12-31 23:59:59.999");
        data.put("colBoolean", 0);
        data.put("updatedBy", "JUNIT_UPDATE");
        
        updateData.add(data);
        root.put("update_data", updateData);
        
        return mapper.writeValueAsString(root);
    }

    /**
     * DELETE용 파라미터 생성
     */
    public static String createDeleteParams(String testCategory) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> deleteParams = new ArrayList<>();
        
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("testCategory", testCategory);
        
        deleteParams.add(param);
        root.put("delete_params", deleteParams);
        
        return mapper.writeValueAsString(root);
    }

    /**
     * NULL 값 테스트 데이터
     */
    public static String createNullTestData() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> testData = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("testId", 2000 + i);
            
            // 부분적으로 NULL
            if (i == 0) {
                data.put("colNumber", 123);
                data.put("colVarchar50", null);
                data.put("colDate", "2026-01-16");
            } else if (i == 1) {
                data.put("colNumber", null);
                data.put("colVarchar50", "Test");
                data.put("colDate", null);
            } else {
                data.put("colNumber", null);
                data.put("colVarchar50", null);
                data.put("colDate", null);
            }
            
            data.put("colTimestamp", null);
            data.put("colBoolean", null);
            data.put("testCategory", "NULL_TEST");
            data.put("testDescription", "NULL 테스트 " + (i + 1));
            
            testData.add(data);
        }
        
        root.put("test_data", testData);
        return mapper.writeValueAsString(root);
    }

    /**
     * LOB 테스트 데이터
     */
    public static String createLobTestData() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<Map<String, Object>> lobData = new ArrayList<>();
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("testId", 3001);
        data.put("colClob", generateLongString(10000));  // 10KB 텍스트
        data.put("colBlob", generateHexString(1000));    // 1KB 바이너리 (hex)
        data.put("testCategory", "LOB_TEST");
        data.put("testDescription", "대용량 객체 테스트");
        
        lobData.add(data);
        root.put("lob_data", lobData);
        
        return mapper.writeValueAsString(root);
    }

    // ========== 헬퍼 메서드 ==========

    private static String generateLongString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
        for (int i = 0; i < length; i++) {
            sb.append(base.charAt(i % base.length()));
        }
        return sb.toString();
    }

    private static String generateString(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static String generateHexString(int byteLength) {
        StringBuilder sb = new StringBuilder(byteLength * 2);
        for (int i = 0; i < byteLength; i++) {
            sb.append(String.format("%02X", i % 256));
        }
        return sb.toString();
    }
}