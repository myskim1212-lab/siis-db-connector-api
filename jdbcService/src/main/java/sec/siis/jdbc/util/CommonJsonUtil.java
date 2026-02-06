package sec.siis.jdbc.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.config.OperationConfig;
import sec.siis.jdbc.execution.JdbcExecutionOperationResult;
import sec.siis.jdbc.execution.JdbcExecutionResult;
import sec.siis.jdbc.execution.ObjectMapperHolder;

public class CommonJsonUtil {
	
    private static ObjectMapper mapper() {
        return ObjectMapperHolder.INSTANCE.mapper;
    }

    public static String toJson(
            Map<String, List<Map<String, Object>>> data
    ) throws Exception {

        return mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }
 
    public static String toJson(
    		Object data
    ) throws Exception {

        return mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }
    
 
    public static String toJson(
    		JdbcExecutionResult data
    ) throws Exception {

        return mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }

  
    public static String toJsonResult(
    	    List<JdbcExecutionOperationResult>  data
    ) throws Exception {

        return mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }

    public static String toJson(
            List<Map<String, Object>> data
    ) throws Exception {

        return mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }    
    
   
    private static JsonNode resolvePath(JsonNode root, String path) {
        if (path == null || "/".equals(path)) return root;

        JsonNode cur = root;
        for (String p : path.substring(1).split("/")) {
            cur = cur.path(p);
            if (cur.isMissingNode()) return null;
        }
        return cur;
    }
    
    public static boolean existRecordNode(
            JsonNode root,
            String recordBasePath,
            String opName,
            String recordName,
            Map<String, JsonNode> pathCache
    ) {
        // 1. Base 노드(예: /operations) 확보
        JsonNode base = getBaseNode(root, recordBasePath, pathCache);
        if (base == null) return false;

        // 2. Base가 배열인 경우 (예: "operations": [ {...} ])
        if (base.isArray()) {
            for (JsonNode node : base) {
                // 해당 노드가 오퍼레이션 명(opName)을 가지고 있는지 확인
                if (node.has(opName)) {
                    // 오퍼레이션 노드 내부에서 recordName(예: data)이 있는지 확인
                    return true;
                }
            }
            return false;
        }

        // 3. Base가 일반 객체인 경우 (하위 호환성)
        if (base.has(opName)) {
            return true;
        }

        return false;
    }

    public static List<Map<String, Object>> extractRecord(
            JsonNode root,
            String recordBasePath,
            String opName,      // 추가: delete_tb_user 등
            String recordName,  // 실제 데이터 필드: data
            Map<String, JsonNode> pathCache
    ) {
        // 1. Base 노드(예: /operations) 확보
        JsonNode base = getBaseNode(root, recordBasePath, pathCache);
        if (base == null) return null;

        JsonNode targetOpNode = null;

        // 2. Base가 배열인 경우 순회하며 opName 노드 찾기
        if (base.isArray()) {
            for (JsonNode node : base) {
                if (node.has(opName)) {
                    targetOpNode = node.get(opName);
                    break;
                }
            }
        } else {
            targetOpNode = base.get(opName);
        }

        // 3. Operation 노드가 없거나, 그 안에 recordName(data) 필드가 없는 경우
        if (targetOpNode == null || !targetOpNode.has(recordName)) {
            return null; 
        }

        JsonNode recordNode = targetOpNode.get(recordName);

        // 4. recordNode(data)가 null인 경우 (delete-only 등)
        if (recordNode.isNull() || recordNode.isMissingNode()) {
            return Collections.emptyList();
        }

        // 5. recordNode가 배열이 아닌 경우 예외 처리
        if (!recordNode.isArray()) {
            throw new IllegalArgumentException(
                    "Record '" + recordName + "' under operation '" + opName + "' must be an array or null"
            );
        }

        // 6. List<Map>으로 변환하여 반환
        return ObjectMapperHolder.INSTANCE.mapper.convertValue(
                recordNode,
                new TypeReference<List<Map<String, Object>>>() {}
        );
    }

    // 중복되는 base 노드 추출 로직을 별도 메서드로 분리
    private static JsonNode getBaseNode(JsonNode root, String recordPath, Map<String, JsonNode> pathCache) {
        String pathKey = (recordPath == null || recordPath.isBlank()) ? "/" : recordPath;
        return pathCache.computeIfAbsent(pathKey, p -> resolvePath(root, p));
    }    

    public static void filterOperationData(Map<String, Object> response, JdbcConfig jdbcCfg) {
        if (response == null || jdbcCfg == null) return;

        Map<String, Object> ops = (Map<String, Object>) response.get("operations");
        if (ops != null) {
            ops.forEach((opName, opResult) -> {
                // 1. JdbcConfig에서 현재 오퍼레이션 이름에 해당하는 설정을 찾음
                String recordName = jdbcCfg.getOperations().stream()
                        .filter(op -> op.getOperation_name().equals(opName))
                        .map(OperationConfig::getData_record) // 설정된 record_name 추출
                        .findFirst()
                        .orElse("data"); // 기본값이 "data"라면 대비책으로 설정

                // 2. 결과 데이터 제거
                if (opResult instanceof JdbcExecutionOperationResult) {
                    // 객체 형태인 경우 내부 필드 초기화
                    ((JdbcExecutionOperationResult) opResult).setResultData(null);
                } else if (opResult instanceof Map) {
                    // Map 형태인 경우 해당 recordName 키를 삭제
                    Map<String, Object> resultMap = (Map<String, Object>) opResult;
                    resultMap.remove(recordName);
                    
                    // 혹시 모를 기본값 "data"도 확인 사살 (선택 사항)
                    resultMap.remove("data"); 
                }
            });
        }
    }
}
