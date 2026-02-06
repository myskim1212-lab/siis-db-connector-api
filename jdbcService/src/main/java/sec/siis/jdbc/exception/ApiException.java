package sec.siis.jdbc.exception;

public abstract class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;
    private final ApiError error;

    protected ApiException(ApiError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }

    public ApiError getError() {
        return error;
    }
}

