package sec.siis.jdbc.config;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParamExtractor {

    private static final Pattern PARAM_PATTERN = Pattern.compile(":(\\w+)");

    public static Set<String> extractNamedParams(String sql) {
        Set<String> params = new HashSet<>();
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }
}