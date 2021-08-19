package kindle.services;

import java.util.regex.Pattern;

public class KindleAppArgumentParserService {

    public boolean isValid(String argument) {
        Pattern pattern = Pattern.compile("^\\$\\{(.*?)}");
        return pattern.matcher(argument).find();
    }

    public String[] extract(String argument) {
        String strippedArgumentTokens = replaceLast(argument.replaceFirst("^\\$\\{",""),"}","");
        return strippedArgumentTokens.isEmpty() ? new String[]{} : strippedArgumentTokens.split(" ");
    }

    private String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
