package org.example;

import java.util.Arrays;
import java.util.List;

public class Utils {

    private static final List<String> keywords = Arrays.asList(
            "break", "case", "chan", "const", "continue", "default",
            "defer", "else", "fallthrough", "for", "func", "go", "goto",
            "if", "import", "interface", "map", "package", "range", "return",
            "select", "struct", "switch", "type", "var", "iota"
    );

    public static boolean isKeyword(String str) {
        return keywords.contains(str);
    }

    public static boolean isBoolean(String str) {
        return str.equals("true") || str.equals("false");
    }

    public static boolean isNull(String str) {
        return str.equals("null");
    }

    public static boolean isSeparator(Character c) {
        return c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']' || c == '.' || c == ',' || c == ';';
    }

    public static boolean isOperator(Character c) {
        return c == '&' || c == '|' || c == '=' || c == '<' || c == '>' || c == '?' || c == ':'
                || c == '!' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%';
    }
}
