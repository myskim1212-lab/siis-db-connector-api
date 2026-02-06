package sec.siis.jdbc.config;

import java.util.ArrayList;
import java.util.List;

import sec.siis.jdbc.execution.CommitAction;
import sec.siis.jdbc.execution.CommitScope;

/**
 * Operation 설정 (Master-Detail 지원)
 * DetailOperationConfig 없이 OperationConfig를 재사용
 */
public class OperationConfig {
    // 기존 필드들...
    private String operation_name;
    private ActionType action_type;
    private String data_record;
    private String data_record_path;
    private CommitAction commit_action=CommitAction.CONTINUE;
    private CommitScope commit_scope=CommitScope.ALL;
    private String sql;
    private List<FieldConfig> fields;
    private List<FieldConfig> param_fields;
    
    // 날짜 포맷
    private List<String> date_formats;
    private List<String> timestamp_formats;
    
    // Master-Detail 지원
    private boolean has_detail = false;              // Detail Operation 존재 여부
    private List<OperationConfig> detail_operations; // OperationConfig 재사용
    //private List<String> inherit_fields;             // Detail이 Master에서 상속받을 필드명 리스트
    private List<FieldConfig> inherit_fields;
    
    private int row_limit=0;
    
	public OperationConfig() {
    	
    }

    public OperationConfig(OperationConfig src) {
        this.operation_name = src.operation_name;
        this.action_type = src.action_type;
        this.data_record = src.data_record;
        this.data_record_path = src.data_record_path;
        this.sql = src.sql;

        this.commit_scope = src.commit_scope;
        this.commit_action = src.commit_action;

        this.fields = src.fields != null
                ? new ArrayList<>(src.fields)
                : null;

        this.inherit_fields = src.inherit_fields != null
                ? new ArrayList<>(src.inherit_fields)
                : null;

        this.date_formats = src.date_formats != null
                ? new ArrayList<>(src.date_formats)
                : null;

        this.timestamp_formats = src.timestamp_formats != null
                ? new ArrayList<>(src.timestamp_formats)
                : null;
    }
    
    public OperationConfig copy() {
        return new OperationConfig(this);
    }
    
    public boolean isHas_detail() {
        return has_detail;
    }

    public void setHas_detail(boolean has_detail) {
        this.has_detail = has_detail;
    }
    
    public String getOperation_name() {
		return operation_name;
	}

	public void setOperation_name(String operation_name) {
		this.operation_name = operation_name;
	}

	public ActionType getAction_type() {
		return action_type;
	}

	public void setAction_type(ActionType action_type) {
		this.action_type = action_type;
	}

	public String getData_record() {
		return data_record;
	}

	public void setData_record(String data_record) {
		this.data_record = data_record;
	}

	public String getData_record_path() {
		return data_record_path;
	}

	public void setData_record_path(String data_record_path) {
		this.data_record_path = data_record_path;
	}

	public CommitAction getCommit_action() {
		return commit_action;
	}

	public void setCommit_action(CommitAction commit_action) {
		this.commit_action = commit_action;
	}

	public CommitScope getCommit_scope() {
		return commit_scope;
	}

	public void setCommit_scope(CommitScope commit_scope) {
		this.commit_scope = commit_scope;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<FieldConfig> getFields() {
		return fields;
	}

	public void setFields(List<FieldConfig> fields) {
		this.fields = fields;
	}

	public List<FieldConfig> getParam_fields() {
		return param_fields;
	}

	public void setParam_fields(List<FieldConfig> param_fields) {
		this.param_fields = param_fields;
	}

	public List<String> getDate_formats() {
		return date_formats;
	}

	public void setDate_formats(List<String> date_formats) {
		this.date_formats = date_formats;
	}

	public List<String> getTimestamp_formats() {
		return timestamp_formats;
	}

	public void setTimestamp_formats(List<String> timestamp_formats) {
		this.timestamp_formats = timestamp_formats;
	}

	public List<OperationConfig> getDetail_operations() {
        return detail_operations;
    }

    public void setDetail_operations(List<OperationConfig> detail_operations) {
        this.detail_operations = detail_operations;
    }
    
//    public List<String> getInherit_fields() {
//        return inherit_fields;
//    }
//
//    public void setInherit_fields(List<String> inherit_fields) {
//        this.inherit_fields = inherit_fields;
//    }
//    
    
    public List<FieldConfig> getInherit_fields() {
		return inherit_fields;
	}

	public void setInherit_fields(List<FieldConfig> inherit_fields) {
		this.inherit_fields = inherit_fields;
	}
	
    /**
     * Detail Operation이 있는지 확인
     */
    public boolean hasDetailOperations() {
        return has_detail && detail_operations != null && !detail_operations.isEmpty();
    }
    


	public int getRow_limit() {
		return row_limit;
	}

	public void setRow_limit(int row_limit) {
		this.row_limit = row_limit;
	}    

}