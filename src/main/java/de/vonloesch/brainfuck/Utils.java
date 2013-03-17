package de.vonloesch.brainfuck;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    static int getRepeatCount(final int p, final char[] chars) {
        int i = 1;
        char opCode = chars[p];
        while (p + i < chars.length && chars[p + i] == opCode) {
            i++;
        }

        return i;
    }

    static int findEndBracket(final int i, final String program) {
        int nrOpenBrackets = 1;
        int p = i + 1;
        while (nrOpenBrackets > 0) {
            if (program.charAt(p) == '[') {
                nrOpenBrackets++;
            }

            if (program.charAt(p) == ']') {
                nrOpenBrackets--;
            }

            p++;
        }

        return p - 1;
    }

    static Map<String, Integer> getMethods(final String program) {
        char[] chars = program.toCharArray();
        Map<String, Integer> methodMap = new HashMap<String, Integer>();
        int methodCount = 0;
        for (int p = 0; p < chars.length; p++) {
            char c = chars[p];
            if (c == '[') {
                int end = findEndBracket(p, program);
                String body = program.substring(p + 1, end);
                methodMap.put(body, methodCount++);
            }
        }

        return methodMap;
    }

}
