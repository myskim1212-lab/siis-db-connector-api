package sec.siis.jdbc.config;

public class FieldConfig {

    private String data_field;
    private String param;
    private String type;
    private Direction direction;
    
    public FieldConfig() {
    }

    public String getType() {
        return type;
    }

    public String getData_field() {
		return data_field;
	}

	public void setData_field(String data_field) {
		this.data_field = data_field;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public void setType(String type) {
        this.type = type;
    }

    public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}