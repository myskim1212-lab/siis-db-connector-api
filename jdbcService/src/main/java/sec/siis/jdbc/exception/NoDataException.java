package sec.siis.jdbc.exception;

public class NoDataException extends ApiException {
    public NoDataException() {
        super(
            new ApiError(
                "E103",
                "No data found",
                "NO_DATA",
                "RESULT_EMPTY"
            ),
            null
        );
    }
}