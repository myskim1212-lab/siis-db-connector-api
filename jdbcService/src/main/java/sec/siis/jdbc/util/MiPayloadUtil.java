package sec.siis.jdbc.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;

public class MiPayloadUtil {
    private MiPayloadUtil() {}

    public static void setJsonPayload(MessageContext mc, String jsonString) {
        org.apache.axis2.context.MessageContext axis2MC = 
                ((org.apache.synapse.core.axis2.Axis2MessageContext) mc).getAxis2MessageContext();
        
        // 1. 기존 JSON 데이터 구조 엔진에서 삭제
        org.apache.synapse.commons.json.JsonUtil.removeJsonPayload(axis2MC);
        
        // 2. SOAP Body 내부의 오염된 자식 노드들 강제 제거 (방법 2 적용)
        try {
            org.apache.axiom.om.OMElement body = axis2MC.getEnvelope().getBody();
            if (body != null) {
                body.build();
                while (body.getFirstOMChild() != null) {
                    body.getFirstOMChild().detach();
                }
            }
        } catch (Exception e) {
            // 무시
        }

        // 3. 새로운 JSON 스트림 주입
        try {
            byte[] bytes = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            java.io.InputStream is = new java.io.ByteArrayInputStream(bytes);
            
            // 새로운 페이로드 설정 (기존 데이터가 비워졌으므로 안전하게 성공함)
            org.apache.synapse.commons.json.JsonUtil.getNewJsonPayload(axis2MC, is, true, true);
            
            axis2MC.setProperty("messageType", "application/json");
            axis2MC.setProperty("ContentType", "application/json");
            
        } catch (Exception e) {
            throw new SynapseException("JSON Payload setting failed", e);
        }
    }
    
    public static String readJsonPayload(MessageContext synCtx) throws Exception{

        org.apache.axis2.context.MessageContext axis2Ctx =
            ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        axis2Ctx =
        	    ((org.apache.synapse.core.axis2.Axis2MessageContext) synCtx)
        	        .getAxis2MessageContext();

        InputStream jsonStream = JsonUtil.getJsonPayload(axis2Ctx);

        return new String(jsonStream.readAllBytes(), StandardCharsets.UTF_8);
    }
    
}
