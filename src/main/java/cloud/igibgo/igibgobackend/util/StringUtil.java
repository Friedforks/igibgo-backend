package cloud.igibgo.igibgobackend.util;


public class StringUtil {
    public static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
    public static String toCamelCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean convertNext = false;
        for (char ch : snakeCase.toCharArray()) {
            if (ch == '_') {
                convertNext = true;
            } else {
                if (convertNext) {
                    result.append(Character.toUpperCase(ch));
                    convertNext = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }
}
