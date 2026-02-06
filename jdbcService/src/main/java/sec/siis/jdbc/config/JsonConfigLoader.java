package sec.siis.jdbc.config;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import sec.siis.jdbc.execution.CommitAction;
import sec.siis.jdbc.execution.CommitScope;

public class JsonConfigLoader {

	public static JdbcConfig load(String resourcePath) {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new ConfigValidationException("Config file not found: " + resourcePath);
			}
			return parseAndValidate(is);
		} catch (ConfigValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigValidationException(
					"Failed to load config from resource: " + resourcePath + " | " + e.getMessage());
		}
	}

	public static JdbcConfig loadFromString(String yamlConfig) {
		if (yamlConfig == null || yamlConfig.isBlank()) {
			throw new ConfigValidationException("YAML config string is empty");
		}
		try {
			ObjectMapper mapper = createYamlMapper();
			JdbcConfig config = mapper.readValue(yamlConfig, JdbcConfig.class);
			applyDefaults(config);
			validateConfig(config);
			return config;
		} catch (ConfigValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigValidationException("Invalid YAML format: " + e.getMessage());
		}
	}

	private static JdbcConfig parseAndValidate(InputStream is) throws Exception {
		ObjectMapper mapper = createYamlMapper();
		JdbcConfig config = mapper.readValue(is, JdbcConfig.class);
		applyDefaults(config);
		validateConfig(config);
		return config;
	}

	private static ObjectMapper createYamlMapper() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);
		return mapper;
	}

	private static void applyDefaults(JdbcConfig config) {
		if (config.getBatch_size() == null || config.getBatch_size() <= 0)
			config.setBatch_size(100);
		if (config.getData_record_path() == null || config.getData_record_path().trim().isEmpty())
			config.setData_record_path("/");
		if (config.getStop_on_operation_error() == null)
			config.setStop_on_operation_error(true);
		if (config.getStop_on_row_error() == null)
			config.setStop_on_row_error(false);

		if (config.getOperations() != null) {
			for (OperationConfig op : config.getOperations())
				applyOperationDefaults(op);
		}
	}

	private static void applyOperationDefaults(OperationConfig op) {
		if (op.getCommit_action() == null)
			op.setCommit_action(CommitAction.CONTINUE);
		if (op.getCommit_scope() == null)
			op.setCommit_scope(CommitScope.ALL);
		applyFieldDefaults(op);
		
		if (op.getDetail_operations() != null) {
			for (OperationConfig detailOp : op.getDetail_operations())
				applyOperationDefaults(detailOp);
		}
		
	}

	private static void applyFieldDefaults(OperationConfig op) {
	    if (op.getFields() == null) return;

	    op.getFields().stream()
	        .filter(field -> field.getDirection() == null)
	        .forEach(field -> {
	        	if(op.getAction_type()==ActionType.SELECT)
	        	{
        			// SELECT이 Direction이 NULL 경우 OUT으로 기본값 설정
        			field.setDirection(Direction.OUT);
	        	}else {
	        		// INSERT / DELETE / UPDATE / UPSERT / PROCEDURE의 Direction이 null 일경우 기본값은 IN
	        		field.setDirection(Direction.IN);
	        	}
	        });
	}

	private static void validateConfig(JdbcConfig config) {
		StringBuilder errors = new StringBuilder();
		validateRequired(config.getApi_name(), "api_name", errors);
		validateRequired(config.getTarget_system_key(), "target_system_key", errors);

		if (config.getOperations() == null || config.getOperations().isEmpty()) {
			errors.append("  - No operations defined\n");
		} else {
			int order = 0;
			for (OperationConfig op : config.getOperations()) {
				validateOperation(op, ++order, errors);
			}
		}

		if (errors.length() > 0) {
			throw new ConfigValidationException("Configuration validation failed:\n" + errors.toString());
		}
	}

	private static void validateOperation(OperationConfig op, int order, StringBuilder errors) {
	    String prefix = (op.getOperation_name() != null && !op.getOperation_name().isEmpty())
	            ? "Operation '" + op.getOperation_name() + "'"
	            : "Operation #" + order;

	    validateRequired(op.getOperation_name(), prefix + ".operation_name", errors);
	    validateRequired(op.getAction_type(), prefix + ".action_type", errors);
	    validateRequired(op.getData_record(), prefix + ".data_record", errors);

	    // [추가] SELECT가 아닌데 OUT/INOUT으로 설정된 필드 검증
	    if (op.getAction_type() != ActionType.SELECT && op.getFields() != null) {
	        for (FieldConfig fc : op.getFields()) {
	            if (fc.getDirection() == Direction.OUT || fc.getDirection() == Direction.INOUT) {
	                errors.append("  - ").append(prefix)
	                      .append(": Only SELECT action can have OUT or INOUT direction. Field '")
	                      .append(fc.getData_field()).append("' has invalid direction: ")
	                      .append(fc.getDirection()).append("\n");
	            }
	        }
	    }
	    
	    // Procedure가 아닌 경우 SQL 및 파라미터 검증
	    if (op.getAction_type() != null && !(ActionType.PROCEDURE == op.getAction_type())) {
	        validateRequired(op.getSql(), prefix + ".sql", errors);
	        
	        if (op.getSql() != null && !op.getSql().isBlank()) {
	            checkParameterMapping(op, prefix, errors);
	        }
	    }

	    // Detail Operations 재귀 검증
	    if (op.getDetail_operations() != null) {
	        int detailOrder = 0;
	        for (OperationConfig detailOp : op.getDetail_operations()) {
	            validateOperation(detailOp, ++detailOrder, errors);
	        }
	    }
	}

	private static void checkParameterMapping(OperationConfig op, String prefix, StringBuilder errors) {
	    // 1. SQL에서 사용된 :variable 추출
	    Set<String> sqlParams = SqlParamExtractor.extractNamedParams(op.getSql());
	    
	    // 2. 파라미터 수집 (fields + param_fields + inherit_fields)
	    Set<String> definedParams = new HashSet<>();
	    
	    // 일반 필드 수집
	    if (op.getFields() != null) {
	        for (FieldConfig fc : op.getFields()) {
	            if (fc.getParam() != null) definedParams.add(fc.getParam());
	        }
	    }
	    
	    // 파라미터 전용 필드 수집
	    if (op.getParam_fields() != null) {
	        for (FieldConfig fc : op.getParam_fields()) {
	            if (fc.getParam() != null) definedParams.add(fc.getParam());
	        }
	    }

	    // [추가] 상속 필드 수집 (YAML에서 수정한 부분)
	    if (op.getInherit_fields() != null) {
	        for (FieldConfig fc : op.getInherit_fields()) {
	            if (fc.getParam() != null) definedParams.add(fc.getParam());
	        }
	    }

	    // 3. 비교 검증
	    for (String sqlParam : sqlParams) {
	        if (!definedParams.contains(sqlParam)) {
	            errors.append("  - ").append(prefix)
	                  .append(": SQL uses ':").append(sqlParam)
	                  .append("', but it is not defined in 'fields', 'param_fields', or 'inherit_fields'\n");
	        }
	    }
	}

	private static void validateRequired(Object value, String path, StringBuilder errors) {
		if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
			errors.append("  - Required field '").append(path).append("' is missing\n");
		}
	}
}