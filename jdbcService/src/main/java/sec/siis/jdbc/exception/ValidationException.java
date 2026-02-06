package sec.siis.jdbc.exception;

public class ValidationException extends ApiException {
    public ValidationException(String msg) {
        super(
            new ApiError(
                "E100",
                msg,
                "VALIDATION",
                "INVALID_PARAMETER"
            ),
            null
        );
    }
}