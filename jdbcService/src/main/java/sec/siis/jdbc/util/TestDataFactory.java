package sec.siis.jdbc.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDataFactory {

    /**
     * 전체 입력 데이터 생성
     */
    public static Map<String, List<Map<String, Object>>> createInput(String type) {

        Map<String, List<Map<String, Object>>> input = new HashMap<>();

        if(type=="insert" || type=="merge") {
            // tb_user 데이터
            input.put("tb_user", createTbUserList(10));

            // tb_user1 데이터 (구조 동일, 값만 다르게)
            input.put("tb_user1", createTbUserList(5));
        }else if(type=="select") {
        	input.put("tb_user", createTbUserListForSelect(1));        	
        }

        return input;
    }

    public static String createInputForJson(String type) throws Exception {

        Map<String, List<Map<String, Object>>> input = new HashMap<>();
        String retJson="";

        if(type=="insert" || type=="merge") {
            // tb_user 데이터
            input.put("tb_user", createTbUserList(10));

            // tb_user1 데이터 (구조 동일, 값만 다르게)
            input.put("tb_user1", createTbUserList(5));
            
            retJson=CommonJsonUtil.toJson(input);
        }else if(type=="select") {
        	//input.put("tb_user", createTbUserListForSelect(2));
        	//retJson=CommonJsonUtil.toJson(input);
        	retJson=selectJsonString();
        }
        else if(type=="delete_in") {
        	//input.put("tb_user", createTbUserListForSelect(2));
        	//retJson=CommonJsonUtil.toJson(input);
        	retJson="{\r\n"
        			+ "  \"tb_user_del\" : [{\r\n"
        			+ "    \"id\" : 1\r\n"
        			+ "  },{\r\n"
        			+ "    \"id\" : 2\r\n"
        			+ "  }]\r\n"
        			+ "}";
        }     
        else if(type=="delete_noparam") {
        	//input.put("tb_user", createTbUserListForSelect(2));
        	//retJson=CommonJsonUtil.toJson(input);
        	retJson="";
        }        
        else if(type=="delete_merge_procedure") {
        	
        	input.put("delete_tb_user", null);
        	input.put("merge_tb_user", createTbUserList(10));
        	input.put("proc_tb_user", null);
        	retJson=CommonJsonUtil.toJson(input);
        }else if(type=="delete_merge_procedure_row_error") {
        	retJson="{\r\n"
        			+ "  \"merge_tb_user\" : [ {\r\n"
        			+ "    \"nick\" : \"NICK_1\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfMQ==\",\r\n"
        			+ "    \"name\" : \"사용자_1\",\r\n"
        			+ "    \"active\" : \"N\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 1,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 1번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 21\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_2\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfMg==\",\r\n"
        			+ "    \"name\" : \"사용자_2\",\r\n"
        			+ "    \"active\" : \"Y\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 2,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 2번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 22\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_3\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfMw==\",\r\n"
        			+ "    \"name\" : \"사용자_3\",\r\n"
        			+ "    \"active\" : \"N\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 3,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 3번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 23\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_4\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfNA==\",\r\n"
        			+ "    \"name\" : \"사용자_4\",\r\n"
        			+ "    \"active\" : \"Y\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 4,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 4번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 24\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_5\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfNQ==\",\r\n"
        			+ "    \"name\" : \"사용자_5\",\r\n"
        			+ "    \"active\" : \"N\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 5,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 5번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 25\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_6\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfNg==\",\r\n"
        			+ "    \"name\" : \"사용자_6\",\r\n"
        			+ "    \"active\" : \"Y\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 6,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 6번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 26\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_7\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfNw==\",\r\n"
        			+ "    \"name\" : \"사용자_7\",\r\n"
        			+ "    \"active\" : \"N\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 7,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 7번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 27\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_8_TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfOA==\",\r\n"
        			+ "    \"name\" : \"사용자_8\",\r\n"
        			+ "    \"active\" : \"Y\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 8,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 8번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 28\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_9_TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfOQ==\",\r\n"
        			+ "    \"name\" : \"사용자_9\",\r\n"
        			+ "    \"active\" : \"N\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 9,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 9번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 29\r\n"
        			+ "  }, {\r\n"
        			+ "    \"nick\" : \"NICK_10\",\r\n"
        			+ "    \"score\" : 1234.56,\r\n"
        			+ "    \"profile\" : \"UFJPRklMRV9CSU5BUllfMTA=\",\r\n"
        			+ "    \"name\" : \"사용자_10\",\r\n"
        			+ "    \"active\" : \"Y\",\r\n"
        			+ "    \"optional\" : null,\r\n"
        			+ "    \"id\" : 10,\r\n"
        			+ "    \"addr\" : \"서울시 강남구 테헤란로 10번지\\nCLOB 테스트 데이터\",\r\n"
        			+ "    \"crtdt\" : \"2026-01-12\",\r\n"
        			+ "    \"updts\" : \"2026-01-12T04:32:10.816+00:00\",\r\n"
        			+ "    \"age\" : 20\r\n"
        			+ "  } ],\r\n"
        			+ "  \"delete_tb_user\" : null,\r\n"
        			+ "  \"proc_tb_user\" : null\r\n"
        			+ "}";
        }
        else if(type=="config_insert_master_detail") {
        	retJson="{\r\n"
        			+ "  \"users\": [\r\n"
        			+ "    {\r\n"
        			+ "      \"id\": 1001,\r\n"
        			+ "      \"name\": \"홍길동\",\r\n"
        			+ "      \"active\": \"Y\",\r\n"
        			+ "      \"details\": [\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 1,\r\n"
        			+ "          \"detailType\": \"PREFERENCE\",\r\n"
        			+ "          \"detailValue\": \"DARK_MODE\"\r\n"
        			+ "        },\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 2,\r\n"
        			+ "          \"detailType\": \"HISTORY\",\r\n"
        			+ "          \"detailValue\": \"LAST_LOGIN\",\r\n"
        			+ "          \"detailDate\": \"2026-01-12\"\r\n"
        			+ "        }\r\n"
        			+ "      ]\r\n"
        			+ "    },\r\n"
        			+ "    {\r\n"
        			+ "      \"id\": 1002,\r\n"
        			+ "      \"name\": \"김철수\",\r\n"
        			+ "      \"active\": \"N\",\r\n"
        			+ "      \"details\": [\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 3,\r\n"
        			+ "          \"detailType\": \"SETTING\",\r\n"
        			+ "          \"detailValue\": \"SMS_NOTIFICATION\",\r\n"
        			+ "          \"detailNumber\": 0\r\n"
        			+ "        }\r\n"
        			+ "      ]\r\n"
        			+ "    }\r\n"
        			+ "  ],\r\n"
        			+ "  \"delete_user_detail\" : null,\r\n"    
        			+ "  \"delete_user\" : null\r\n"          			
        			+ "}";        
        }
        else if(type=="config_insert_master_detail_error") {
        	retJson="{\r\n"
        			+ "  \"users\": [\r\n"
        			+ "    {\r\n"
        			+ "      \"id\": 1001,\r\n"
        			+ "      \"name\": \"홍길동\",\r\n"
        			+ "      \"active\": \"Y\",\r\n"
        			+ "      \"details\": [\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 1,\r\n"
        			+ "          \"detailType\": \"PREFERENCE\",\r\n"
        			+ "          \"detailValue\": \"DARK_MODE\"\r\n"
        			+ "        },\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 2,\r\n"
        			+ "          \"detailType\": \"HISTORY-11111111111111111111111111111111111111111111111111111111111111111111111111\",\r\n"
        			+ "          \"detailValue\": \"LAST_LOGIN\",\r\n"
        			+ "          \"detailDate\": \"2026-01-12\"\r\n"
        			+ "        }\r\n"
        			+ "      ]\r\n"
        			+ "    },\r\n"
        			+ "    {\r\n"
        			+ "      \"id\": 1002,\r\n"
        			+ "      \"name\": \"김철수\",\r\n"
        			+ "      \"active\": \"N\",\r\n"
        			+ "      \"details\": [\r\n"
        			+ "        {\r\n"
        			+ "          \"detailId\": 3,\r\n"
        			+ "          \"detailType\": \"SETTING\",\r\n"
        			+ "          \"detailValue\": \"SMS_NOTIFICATION\",\r\n"
        			+ "          \"detailNumber\": 0\r\n"
        			+ "        }\r\n"
        			+ "      ]\r\n"
        			+ "    }\r\n"
        			+ "  ],\r\n"
        			+ "  \"delete_user_detail\" : null,\r\n"    
        			+ "  \"delete_user\" : null\r\n"          			
        			+ "}";        
        }     
        else if(type=="select_noparam") {
        	retJson="{\"select_noparam\":null}";
        }
        return retJson;
    }
    
    public static String selectJsonString() {
    	String input="{\r\n"
        			+ "  \"select_tb_user\" : [{\r\n"
        			+ "    \"id\" : [1,2]\r\n"
        			+ "  }"
        			+ "]\r\n"
        			+ "}";  	
        return input;
    }
    
    public static String deleteJsonString() {
    	String input="{\r\n"
    			+ "  \"tb_user_del\" : ["
    			+ "{\r\n"
    			+ "    \"id\" : 1\r\n"
    			+ "  },"
    			+ "{\r\n"
    			+ "    \"id\" : 2\r\n"
    			+ "  }"    			
    			+ "]\r\n"  			
    			+ "}";    	
        return input;
    }

    
    /**
     * TB_USER 레코드 목록 생성
     */
    private static List<Map<String, Object>> createTbUserList(int count) {

        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            list.add(createTbUserRow(i));
        }
        return list;
    }
    /**
     * TB_USER 레코드 목록 생성
     */
    private static List<Map<String, Object>> createTbUserListForSelect(int count) {

        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
        	list.add(createTbUserRowForSelect(i));
        }
        return list;
    }
    /**
     * TB_USER 단일 Row
     */
    private static Map<String, Object> createTbUserRow(int id) {

        Map<String, Object> row = new HashMap<>();

        row.put("id", id);
        row.put("name", "사용자_" + id);
        row.put("nick", "NICK_" + id);
        row.put("age", 20 + (id % 10));
        row.put("score", new BigDecimal("1234.56"));
        row.put("active", id % 2 == 0 ? "Y" : "N");

        row.put("crtdt", new java.sql.Date(System.currentTimeMillis()));
        row.put("updts", Timestamp.valueOf(LocalDateTime.now()));

        // CLOB
        row.put(
            "addr",
            "서울시 강남구 테헤란로 " + id + "번지\nCLOB 테스트 데이터"
        );

        // BLOB
        row.put(
            "profile",
            ("PROFILE_BINARY_" + id)
                .getBytes(StandardCharsets.UTF_8)
        );

        // NULL 테스트
        row.put("optional", null);

        return row;
    }
    
    private static Map<String, Object> createTbUserRowForSelect(int id) {

        Map<String, Object> row = new HashMap<>();

        row.put("id", id);

        return row;
    }    
}