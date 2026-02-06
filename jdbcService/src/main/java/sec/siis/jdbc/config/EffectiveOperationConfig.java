package sec.siis.jdbc.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sec.siis.jdbc.execution.CommitAction;
import sec.siis.jdbc.execution.CommitScope;

public class EffectiveOperationConfig {

	private final OperationConfig originalConfig;
    private final OperationConfig op;
    private final JdbcConfig ifcfg;

    private boolean sqlLogged = false;
    

	/* =========================
     * 기본 생성자 (Master / 일반)
     * ========================= */
    public EffectiveOperationConfig(
            JdbcConfig ifcfg,
            OperationConfig op
    ) {
    	this.originalConfig = op;  
        this.ifcfg = ifcfg;
        this.op = op;
    }

    /* =========================
     * Detail 전용 Factory
     * ========================= */
    public static EffectiveOperationConfig forDetail(
            JdbcConfig ifcfg,
            OperationConfig masterOp,
            OperationConfig detailOp
    ) {
        OperationConfig merged = mergeDetailOperation(masterOp, detailOp);
        return new EffectiveOperationConfig(ifcfg, merged);
    }

    /* =========================
     * Detail 병합 로직 (캡슐화)
     * ========================= */
    private static OperationConfig mergeDetailOperation(
            OperationConfig masterOp,
            OperationConfig detailOp
    ) {
        OperationConfig merged = new OperationConfig(detailOp); // copy constructor 권장

        /* 날짜 / 타임스탬프 포맷 상속 */
        if (merged.getDate_formats() == null) {
            merged.setDate_formats(masterOp.getDate_formats());
        }
        if (merged.getTimestamp_formats() == null) {
            merged.setTimestamp_formats(masterOp.getTimestamp_formats());
        }

        /* fields 병합 */
        List<FieldConfig> combinedFields = new ArrayList<>();

        if (detailOp.getFields() != null) {
            combinedFields.addAll(detailOp.getFields());
        }

        if (detailOp.getInherit_fields() != null && !detailOp.getInherit_fields().isEmpty()) {

            Map<String, FieldConfig> masterFieldMap = new HashMap<>();
            if (masterOp.getFields() != null) {
                for (FieldConfig f : masterOp.getFields()) {
                    masterFieldMap.put(f.getData_field(), f);
                }
            }

            for (FieldConfig inheritField : detailOp.getInherit_fields()) {
                FieldConfig masterField = masterFieldMap.get(inheritField.getData_field());
                if (masterField != null) {
                    //combinedFields.add(copyField(masterField));
                	combinedFields.add(masterField);
                }
            }
        }

        merged.setFields(combinedFields);
        return merged;
    }

//    private static FieldConfig copyField(FieldConfig src) {
//        FieldConfig f = new FieldConfig();
//        f.setData_field(src.getData_field());
//        f.setParam(src.getParam());
//        f.setType(src.getType());
//        return f;
//    }

    public boolean isMasterDetail() {
        return originalConfig != null && originalConfig.hasDetailOperations();
    }
    
    public OperationConfig getOriginalConfig() {
        return originalConfig;
    }
    
    /* =========================
     * 이하 getter들은 그대로
     * ========================= */
    
    public String getData_record() {
        return op.getData_record();
    }

	public ActionType getAction_type() {
		return op.getAction_type();
	}

    public String getOperation_name() {
        return op.getOperation_name();
    }

    public String getSql() {
        return op.getSql();
    }

    public List<FieldConfig> getFields() {
        return op.getFields();
    }

    public String getData_record_path() {
        return op.getData_record_path() != null
                ? op.getData_record_path()
                : ifcfg.getData_record_path();
    }

    public List<String> getDate_formats() {
        return op.getDate_formats() != null
                ? op.getDate_formats()
                : ifcfg.getDate_formats();
    }

    public List<String> getTimestamp_formats() {
        return op.getTimestamp_formats() != null
                ? op.getTimestamp_formats()
                : ifcfg.getTimestamp_formats();
    }

    public List<FieldConfig> getParamFields() {
        return op.getParam_fields();
    }

    public List<FieldConfig> getWhereParamFields() {
        // fields가 null일 경우를 대비해 빈 스트림 처리 (Optional 또는 null 체크)
        if (op.getFields() == null) return Collections.emptyList();

        return op.getFields().stream()
                // SQL 바인딩(Input)에 필요한 필드만 필터링 (IN, INOUT)
                .filter(field -> field.getDirection() == Direction.IN 
                              || field.getDirection() == Direction.INOUT)
                .collect(Collectors.toList());
    }
    
    public List<FieldConfig> getResultMappingFields() {
        if (op.getFields() == null) return Collections.emptyList();
        
        return op.getFields().stream()
                // 결과 매핑(Output)에 필요한 필드만 필터링 (OUT, INOUT)
                .filter(field -> field.getDirection() == Direction.OUT 
                              || field.getDirection() == Direction.INOUT)
                .collect(Collectors.toList());
    }
    
    public CommitScope getCommit_scope() {
        return op.getCommit_scope();
    }

    public CommitAction getCommit_Action() {
        return op.getCommit_action();
    }
    
    public String getApi_name() {
        return ifcfg.getApi_name();
    }
    
    public int getRow_limit() {
    	return op.getRow_limit();
    }
    
    public JdbcConfig getJdbcConfig(){
    	return this.ifcfg;
    }

    public boolean isSqlLogged() {
		return sqlLogged;
	}

	public void setSqlLogged(boolean sqlLogged) {
		this.sqlLogged = sqlLogged;
	}
}