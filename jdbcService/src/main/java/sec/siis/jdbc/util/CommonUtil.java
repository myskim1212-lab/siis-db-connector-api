package sec.siis.jdbc.util;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

public class CommonUtil {

    /* ================= MAIN ================= */

    public static Object TemporalParse(
            String value,
            int sqlType,
            List<String> formats
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String v = value.trim();

        // 🔹 formats null/empty 방어
        List<String> effectiveFormats = resolveFormats(sqlType, formats);

        for (String fmt : effectiveFormats) {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern(fmt);

                /* ================= DATE ================= */
                if (sqlType == Types.DATE) {

                    if (fmt.contains("H") || fmt.contains("m") || fmt.contains("s")) {
                        LocalDateTime ldt = LocalDateTime.parse(v, f);
                        return Date.valueOf(ldt.toLocalDate());
                    }

                    LocalDate d = LocalDate.parse(v, f);
                    return Date.valueOf(d);
                }

                /* ================= TIMESTAMP ================= */
                if (sqlType == Types.TIMESTAMP) {

                    // OffsetDateTime (timezone)
                    try {
                        OffsetDateTime odt = OffsetDateTime.parse(v, f);
                        return Timestamp.from(odt.toInstant());
                    } catch (DateTimeParseException ignore) {
                    }

                    if (fmt.contains("H") || fmt.contains("m") || fmt.contains("s")) {
                        LocalDateTime ldt = LocalDateTime.parse(v, f);
                        return Timestamp.valueOf(ldt);
                    }

                    LocalDate d = LocalDate.parse(v, f);
                    return Timestamp.valueOf(d.atStartOfDay());
                }

            } catch (DateTimeParseException ignore) {
                // 다음 포맷 시도
            }
        }

        throw new IllegalArgumentException(
            "Unsupported date/time format: " + value
        );
    }

    /* ================= HELPER ================= */

    private static List<String> resolveFormats(
            int sqlType,
            List<String> formats
    ) {
        if (formats != null && !formats.isEmpty()) {
            return formats;
        }
        return List.of(); // 안전 fallback
    }
    
    public static String decodeYamlConfig(String encodedStr) throws Exception{
        if (encodedStr == null || encodedStr.isEmpty()) return null;

        // 1. 앞뒤 공백 제거 및 URL-Safe 대응 (언더바(_)를 슬래시(/)로, 대시(-)를 플러스(+)로 복구)
        // 5f(_) 에러의 직접적인 해결책입니다.
        String normalized = encodedStr.trim()
                                      .replace('_', '/')
                                      .replace('-', '+');

        try {
            // 2. MIME 디코더 사용 (중간에 섞인 줄바꿈(\n, \r)을 알아서 무시함)
            byte[] decodedBytes = Base64.getMimeDecoder().decode(normalized);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }    
}