package sec.siis.jdbc.execution;

public class JdbcExecutionRowResult {
    private int rowIndex;
    private boolean success;
    private boolean committed;
    private String errorCode;
    private String errorMessage;
    private long startTime;
    private long endTime;
    

	public JdbcExecutionRowResult() {
		
	}

	public int getRowIndex() {
		return rowIndex;
	}


	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}


	public boolean isSuccess() {
		return success;
	}


	public void setSuccess(boolean success) {
		this.success = success;
	}


	public boolean isCommitted() {
		return committed;
	}


	public void setCommitted(boolean committed) {
		this.committed = committed;
	}


	public String getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}

