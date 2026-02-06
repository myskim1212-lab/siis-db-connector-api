package sec.siis.jdbc.execution;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import sec.siis.jdbc.util.DateTimeFormatUtil;

public class JdbcExecutionResult {

    private final List<JdbcExecutionOperationResult> operations = new ArrayList<>();

    @JsonIgnore
    private long startTime = System.currentTimeMillis();

    @JsonIgnore
    private long endTime;

    public void add(JdbcExecutionOperationResult r) {
        operations.add(r);
    }

    public void add(JdbcExecutionOperationResult r, boolean committed) {
    	r.setCommitted(committed);
        operations.add(r);
    }
    
    public void finish() {
        this.endTime = System.currentTimeMillis();
    }

    public boolean hasFailure() {
        return operations.stream().anyMatch(r -> !r.isSuccess());
    }

    @JsonIgnore
    public List<JdbcExecutionOperationResult> getErrorOperations() {
    	List<JdbcExecutionOperationResult> failop =
    			operations.stream()
    		        .filter(r -> !r.isSuccess())
    		        .toList(); 
        return failop;
    }
    
    public List<JdbcExecutionOperationResult> getOperations() {
        return operations;
    }
    
    public void addAll(List<JdbcExecutionOperationResult> results, boolean committed) {
        results.forEach(r -> {
            r.setCommitted(committed);
            add(r);
        });
    }
        
    /* =======================
     * JSON 전용 Getter
     * ======================= */

    @JsonIgnore
    @JsonProperty("startTime")
    public String getStartTimeFormatted() {
        return DateTimeFormatUtil.formatTimestamp(startTime);
    }

    @JsonIgnore
    @JsonProperty("endTime")
    public String getEndTimeFormatted() {
        return DateTimeFormatUtil.formatTimestampOrNull(endTime);
    }    
    @JsonIgnore
    @JsonProperty("elapsedMs")
    public long getElapsedMs() {
        return endTime == 0 ? 0 : endTime - startTime;
    }
}
