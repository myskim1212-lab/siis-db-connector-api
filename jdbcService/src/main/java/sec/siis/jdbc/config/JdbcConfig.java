package sec.siis.jdbc.config;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JdbcConfig {

	private String api_name;
	private String target_system_key;
	private String target_system_name;
	private String target_jndi_name;
    private Integer batch_size;
	private Integer max_row_limit;

    private String data_record_path;
    private List<String> date_formats;

	private List<String> timestamp_formats;
    
	private Boolean stop_on_operation_error; 
	private Boolean stop_on_row_error;
	
	// Config 클래스 내부 또는 초기화 로직
	@JsonIgnore
	private DateTimeFormatter primaryDateFormatter;
	@JsonIgnore
	private DateTimeFormatter primaryTimestampFormatter;
	
    private List<OperationConfig> operations;

	public JdbcConfig() {
    }
    
	public String getApi_name() {
		return api_name;
	}

	public void setApi_name(String api_name) {
		this.api_name = api_name;
	}	

	public String getTarget_system_key() {
		return target_system_key;
	}

	public void setTarget_system_key(String target_system_key) {
		this.target_system_key = target_system_key;
	}

	public String getTarget_system_name() {
		return target_system_name;
	}

	public void setTarget_system_name(String target_system_name) {
		this.target_system_name = target_system_name;
	}

	public String getTarget_jndi_name() {
		return target_jndi_name;
	}

	public void setTarget_jndi_name(String target_jndi_name) {
		this.target_jndi_name = target_jndi_name;
	}


	public Integer getMax_row_limit() {
		return max_row_limit;
	}

	public void setMax_row_limit(Integer max_row_limit) {
		this.max_row_limit = max_row_limit;
	}

    public Integer getBatch_size() {
        return batch_size;
    }

    public void setBatch_size(Integer batch_size) {
        this.batch_size = batch_size;
    }

    public String getData_record_path() {
		return data_record_path;
	}

	public void setData_record_path(String data_record_path) {
		this.data_record_path = data_record_path;
	}

    public List<String> getDate_formats() {
		return date_formats;
	}

	public void setDate_formats(List<String> date_formats) {
	    this.date_formats = date_formats;
	    if (date_formats != null && !date_formats.isEmpty()) {
	        this.primaryDateFormatter = DateTimeFormatter.ofPattern(date_formats.get(0));
	    }
	}
	
	public void setTimestamp_formats(List<String> timestamp_formats) {
	    this.timestamp_formats = timestamp_formats;
	    if (timestamp_formats != null && !timestamp_formats.isEmpty()) {
	        this.primaryTimestampFormatter = DateTimeFormatter.ofPattern(timestamp_formats.get(0));
	    }
	}

    public DateTimeFormatter getPrimaryDateFormatter() {
		return primaryDateFormatter;
	}

	public DateTimeFormatter getPrimaryTimestampFormatter() {
		return primaryTimestampFormatter;
	}
	
    public List<String> getTimestamp_formats() {
		return timestamp_formats;
	}

	public Boolean getStop_on_operation_error() {
		return stop_on_operation_error;
	}

	public void setStop_on_operation_error(Boolean stop_on_operation_error) {
		this.stop_on_operation_error = stop_on_operation_error;
	}

	public Boolean getStop_on_row_error() {
		return stop_on_row_error;
	}

	public void setStop_on_row_error(Boolean stop_on_row_error) {
		this.stop_on_row_error = stop_on_row_error;
	}
	
    public List<OperationConfig> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationConfig> operations) {
        this.operations = operations;
    }

	public Optional<OperationConfig> findOperation(String operationName) {
        if (operationName == null || operations == null) {
            return Optional.empty();
        }

        return operations.stream()
                .filter(op -> operationName.equalsIgnoreCase(op.getOperation_name()))
                .findFirst();
    }
}
