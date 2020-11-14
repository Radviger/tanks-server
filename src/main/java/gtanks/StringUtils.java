package gtanks;

public class StringUtils {
    public static String trimChars(String src) {
        return src.replaceAll("(.)\\1+", "$1");
    }

    public static String concatMassive(String[] src, int start) {
        StringBuilder sbf = new StringBuilder();

        for (int i = start; i < src.length; ++i) {
            sbf.append(src[i]);
            if (i != src.length - 1) {
                sbf.append(' ');
            }
        }

        return sbf.toString();
    }
}
