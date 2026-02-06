package sec.siis.jdbc.execution;

import java.util.List;
import java.util.Map;

import sec.siis.jdbc.config.EffectiveOperationConfig;
import sec.siis.jdbc.config.OperationConfig;

public class CollectOperation {
	private int order;
	private final OperationConfig originalConfig;
	private EffectiveOperationConfig eop;
    private List<Map<String, Object>> rows;
    private boolean existNode;
    
	public CollectOperation(int order , EffectiveOperationConfig eop , List<Map<String, Object>> rows , boolean existNode) {
		this.order=order;
		this.eop=eop;
		this.existNode=existNode;
		this.rows=rows;
		this.originalConfig = eop.getOriginalConfig();
    }

    public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	public EffectiveOperationConfig getEop() {
		return eop;
	}

	public void setEop(EffectiveOperationConfig eop) {
		this.eop = eop;
	}

	public List<Map<String, Object>> getRows() {
		return rows;
	}

	public void setRows(List<Map<String, Object>> rows) {
		this.rows = rows;
	}
    public OperationConfig getOriginalConfig() {
        return originalConfig;
    }	
    
	public boolean isExistNode() {
		return existNode;
	}  
}
