package sec.siis.jdbc.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeFormatUtil {

    private DateTimeFormatUtil() {}

    /** 시스템 표준 Timestamp 포맷 */
    public static final String TS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /** 시스템 기본 타임존 (KST 환경이면 KST) */
    public static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    /** Timestamp Formatter */
    public static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern(TS_PATTERN)
                             .withZone(SYSTEM_ZONE);

    /* =======================
     * Helper Methods
     * ======================= */

    public static String formatTimestamp(long epochMillis) {
        return TS_FMT.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String formatTimestampOrNull(long epochMillis) {
        return epochMillis == 0 ? null : formatTimestamp(epochMillis);
    }
}