package sec.siis.jdbc.execution;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import sec.siis.jdbc.exception.ApiException;
import sec.siis.jdbc.util.DateTimeFormatUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RowError {

    private final int rowIndex;                     // rows 내 index
    private final Map<String, Object> rowData;      // 실패 row 데이터

    @JsonIgnore
    private final String errorCode;
    @JsonIgnore    
    private final String errorMessage;
    @JsonIgnore
    private final String errorType;
    private final String errorDetail;
    private final String timestamp;

    private RowError(
            int rowIndex,
            Map<String, Object> rowData,
            String errorCode,
            String errorMessage,
            String errorType,
            String errorDetail,
            String exceptionClass
    ) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.errorDetail = errorDetail;
        this.timestamp = DateTimeFormatUtil.formatTimestamp(System.currentTimeMillis());
       
    }

    /* ========= Factory ========= */

    public static RowError fromException(
            int rowIndex,
            Map<String, Object> rowData,
            ApiException e
    ) {
        return new RowError(
                rowIndex,
                rowData,
                e.getError().getCode(),
                e.getError().getMessage(),
                e.getError().getType(),
                e.getError().getDetail(),
                e.getCause() != null ? e.getCause().getClass().getSimpleName() : null
        );
    }

    public static RowError fromException(
            int rowIndex,
            Map<String, Object> rowData,
            Exception e
    ) {
        return new RowError(
                rowIndex,
                rowData,
                "E500",
                "Row execution failed",
                "SYSTEM",
                e.getMessage(),
                e.getClass().getSimpleName()
        );
    }

    /* ========= Getters ========= */

    public int getRowIndex() {
        return rowIndex;
    }

    public Map<String, Object> getRowData() {
        return rowData;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public String getTimestamp() {
        return timestamp;
    }
}