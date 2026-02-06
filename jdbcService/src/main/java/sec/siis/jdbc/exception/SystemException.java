package sec.siis.jdbc.exception;

public class SystemException extends ApiException {
    public SystemException(Throwable cause) {
        super(
            new ApiError(
                "E500",
                "System error",
                "SYSTEM",
                ExceptionMessageUtils.collectMessages(cause)
            ),
            cause
        );
    }
}