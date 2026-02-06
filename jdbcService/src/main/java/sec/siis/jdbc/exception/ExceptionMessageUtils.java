package sec.siis.jdbc.exception;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExceptionMessageUtils {

    private ExceptionMessageUtils() {}

    public static String collectMessages(Throwable t) {
        if (t == null) return "";

        List<String> messages = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Throwable cur = t;
        while (cur != null) {
        	
            String msg = cur.getMessage();

            if (msg != null && !msg.isBlank() && seen.add(msg)) {
                messages.add(
                    cur.getClass().getSimpleName() + ": " + msg
                );
            }
            cur = cur.getCause();
        }

        return String.join(" | caused by: ", messages);
    }
    
    public static boolean isFatalError(Exception e) {
        if (e instanceof DbException || e instanceof SQLException) {
            String sqlState = "";
            if (e instanceof SQLException) {
                sqlState = ((SQLException) e).getSQLState();
            } else if (e.getCause() instanceof SQLException) {
                sqlState = ((SQLException) e.getCause()).getSQLState();
            }

            if (sqlState != null) {
                // 08: 연결 오류, 42: 문법/객체 부재, 28: 권한 오류
                return sqlState.startsWith("08") || sqlState.startsWith("42") || sqlState.startsWith("28");
            }
        }
        // 기본적으로 알 수 없는 SystemException 등은 치명적 에러로 간주하여 중단하는 것이 안전합니다.
        return !(e instanceof ApiException || e instanceof NoDataException);
    }    
}