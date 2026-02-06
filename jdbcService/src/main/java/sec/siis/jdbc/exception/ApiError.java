package sec.siis.jdbc.exception;

public class ApiError {

    private final String code;
    private final String message;   // 외부 노출
    private final String type;
    private final String detail;    // 내부용 (선택)

    public ApiError(String code, String message, String type, String detail) {
        this.code = code;
        this.message = message;
        this.type = type;
        this.detail = detail;
    }

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}

	public String getDetail() {
		return detail;
	}

    // getters
}