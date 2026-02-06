package sec.siis.jdbc.execution;

import java.util.List;
import java.util.Map;

public class OperationOutput {
    private int successCount;
    private int affectedCount;
    private List<Map<String, Object>> data;

    public OperationOutput(int successCount,int affectedCount, List<Map<String, Object>> data) {
        this.successCount = successCount;
        this.affectedCount = affectedCount;
        this.data = data;
    }



	public int getSuccessCount() {
        return successCount;
    }

    public int getAffectedCount() {
		return affectedCount;
	}

	public void setAffectedCount(int affectedCount) {
		this.affectedCount = affectedCount;
	}
	
    public List<Map<String, Object>> getData() {
        return data;
    }
}
