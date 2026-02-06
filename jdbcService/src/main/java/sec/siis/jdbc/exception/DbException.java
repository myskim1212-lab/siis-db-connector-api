package sec.siis.jdbc.exception;


public class DbException extends ApiException {

    public DbException(Throwable cause) {
    	
    	
        super(
            new ApiError(
                "E102",
                "Database operation failed",
                "DB_ERROR",
                ExceptionMessageUtils.collectMessages(cause)
            ),
            cause
        );
    }
}