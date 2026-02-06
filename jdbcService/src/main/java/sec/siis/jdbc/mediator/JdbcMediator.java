package sec.siis.jdbc.mediator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sec.siis.jdbc.config.ConfigValidationException;
import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.config.JsonConfigLoader;
import sec.siis.jdbc.exception.ApiException;
import sec.siis.jdbc.execution.JdbcExecutionResult;
import sec.siis.jdbc.execution.JdbcExecutor;
import sec.siis.jdbc.logging.LogMessageManager;
import sec.siis.jdbc.mediator.response.ApiResponseFactory;
import sec.siis.jdbc.util.CommonJsonUtil;
import sec.siis.jdbc.util.CommonUtil;
import sec.siis.jdbc.util.MiPayloadUtil;

public class JdbcMediator extends AbstractMediator {

	private static final Logger log = LoggerFactory.getLogger(JdbcMediator.class);

	public boolean mediate(MessageContext context) {
	    JdbcConfig jdbcCfg = null;
	    // NullPointerException 방지를 위해 선언 시 초기화 추천
	    JdbcExecutionResult jdbcExecResult = new JdbcExecutionResult(); 
	    Map<String, Object> response = new LinkedHashMap<>();
	    
	    boolean if_success = true;
	    long startTime = System.currentTimeMillis();
	    long endTime = 0;
	    String msgID ="";
	    
	    try {
	        // 1. Config 로드
	        String encodedIfConfig = getStringProperty(context, "ifConfig");
	        
	        if (encodedIfConfig == null) throw new Exception("ifConfig property is missing");

//	        String decodedIfConfig = new String(
//	            Base64.getDecoder().decode(encodedIfConfig),
//	            StandardCharsets.UTF_8
//	        );

	        String decodedIfConfig = new String(Base64.getDecoder().decode(encodedIfConfig), StandardCharsets.UTF_8);
	        
	       // String decodedIfConfig = CommonUtil.decodeYamlConfig(encodedIfConfig);
	        
	        jdbcCfg = JsonConfigLoader.loadFromString(decodedIfConfig);
	        
	        // msgID 읽어오기
	        msgID = getStringProperty(context, "msgID");
	        
	        // 입력 데이터 확보
	        String inputJson = MiPayloadUtil.readJsonPayload(context);
	        
	        // DataSource 준비
	        Context ctx = new InitialContext();
	        DataSource ds = (DataSource) ctx.lookup(jdbcCfg.getTarget_jndi_name());
	        
			String ops = LogMessageManager.formatOperations(jdbcCfg.getOperations(), op -> op.getOperation_name());
	        LogMessageManager.infoStartIf(log, msgID, jdbcCfg.getApi_name(), ops);
	        
	        // 실행
	        JdbcExecutor executor = new JdbcExecutor(ds,msgID);
	        jdbcExecResult = executor.executeJdbc(jdbcCfg, inputJson, jdbcExecResult);
	        
	        endTime = System.currentTimeMillis();
	        
	        if (jdbcExecResult.hasFailure()) {
	        	 if_success = false;
	        }
	        
            response = ApiResponseFactory.createResponse(jdbcCfg,msgID, startTime, endTime, jdbcExecResult);

	    } catch (ConfigValidationException e) {
	        // 설정 검증 실패 전용 처리
	        log.error("JDBC Configuration Error: {}", e.getMessage());
	        if_success = false;
	        endTime = System.currentTimeMillis();
	        // Factory에서 'E400' 또는 'INVALID_CONFIG' 같은 코드로 처리하도록 구성 추천
	        response = ApiResponseFactory.error(null, e, msgID, startTime, endTime, jdbcExecResult);

	    } catch (ApiException e) {
	        log.error("API error", e);
	        if_success = false;
	        endTime = System.currentTimeMillis();
	        response = ApiResponseFactory.error(jdbcCfg, e, msgID, startTime, endTime, jdbcExecResult);
	    } catch (Exception e) {
	        log.error("System error", e);
	        if_success = false;
	        endTime = System.currentTimeMillis();
	        response = ApiResponseFactory.error(jdbcCfg, e, msgID, startTime, endTime, jdbcExecResult);
	    } finally {
	        if (endTime <= 0) endTime = System.currentTimeMillis();
	    }

	 // 5. 최종 응답 설정
	    try {       
	        // 정상 또는 이전 catch에서 만들어진 response를 JSON으로 변환하여 설정
	        MiPayloadUtil.setJsonPayload(context, CommonJsonUtil.toJson(response));
	    } catch (Exception e) {
	        log.error("Critical: Failed to set JSON payload", e);
	        
	        // [Fallback] JSON 변환조차 실패한 경우를 위한 최소한의 에러 응답 재생성
	        // jdbcExecResult가 깨졌을 가능성이 높으므로 null로 안전하게 처리
	        Map<String, Object> fallbackResponse = ApiResponseFactory.error(
	            jdbcCfg, 
	            new RuntimeException("Response serialization failed: " + e.getMessage()),
	            msgID,
	            startTime, 
	            System.currentTimeMillis(), 
	            null
	        );
	        
	        try {
	            // 다시 한번 시도 (최소한의 필드만 가진 JSON)
	            MiPayloadUtil.setJsonPayload(context, CommonJsonUtil.toJson(fallbackResponse));
		        CommonJsonUtil.filterOperationData(response,jdbcCfg);
	        } catch (Exception fatal) {
	            // 이 단계까지 오면 메시지 엔진 수준의 에러 처리가 필요함
	            throw new SynapseException("Fatal error during response delivery", fatal);
	        }finally{
		        CommonJsonUtil.filterOperationData(fallbackResponse,jdbcCfg);
		        try {
		        	// synapse-handler로 모니터링 데이터 설정
			        // 이때 recored_name ( data ) 는 제거		        	
		        	context.setProperty("eventDetail", CommonJsonUtil.toJson(fallbackResponse));
		        }catch(Exception ex) {
		        	
		        }	        	
	        }
	    } finally {
	        // 로그는 응답 설정 성공 여부와 상관없이 실행
	        logExecutionSummarySafely(msgID,jdbcCfg, jdbcExecResult, if_success, startTime, endTime);   
	        LogMessageManager.infoEndIf(log, msgID, jdbcCfg.getApi_name());
	        
        	// synapse-handler로 모니터링 데이터 설정
	        // 이때 recored_name ( data ) 는 제거
	        CommonJsonUtil.filterOperationData(response,jdbcCfg);
	        try {
	        	context.setProperty("eventDetail", CommonJsonUtil.toJson(response));
	        }catch(Exception e) {
	        	
	        }
	        
	    }
	    
	    return true;
	}
	
    public static String getStringProperty(
            MessageContext ctx, String key) {

        Object v = ctx.getProperty(key);

        if (v == null) return null;

        if (v instanceof OMElement) {
            return ((OMElement) v).getText().trim();
        }

        return v.toString().trim();
    }
    
    private void logExecutionSummarySafely(String msgID,
            JdbcConfig ifcfg,
            JdbcExecutionResult result,
            boolean success,
            long start,
            long end) {

        try {
            LogMessageManager.infoExecutionSummary(
                log,
                msgID,
                ifcfg != null ? ifcfg.getApi_name() : "UNKNOWN",
                success,
                end - start,
                result != null ? result.getOperations() : null
            );
        } catch (Exception e) {
            log.warn("Failed to log execution summary", e);
        }
    }    
}
