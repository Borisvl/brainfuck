package de.vonloesch.brainfuck;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;

/**
 * Translates the bf program to Java and compile it with the java compiler to bytecode.
 *
 * @author  Boris von Loesch
 */
public class JITBrainFuckInterpreter implements BrainfuckInterpreter {

    private State run2(final String program, final State s) {
        String java = convert2Java(program, s instanceof StatusState);
        StringBuilder builder = new StringBuilder();
        builder.append(
                "import de.vonloesch.brainfuck.State;import de.vonloesch.brainfuck.StatusState;\npublic class HelloWorld{\n ");
        if (s instanceof StatusState) {
            builder.append("public static void run(StatusState s){ byte add;");
        } else {
            builder.append("public static void run(State s){ byte add;");
        }

        builder.append(java);
        builder.append("}");

        Class bfClass = JavaCompilerCaller.compile("HelloWorld", builder.toString());

        if (bfClass != null) {
            try {
                if (s instanceof StatusState) {
                    bfClass.getDeclaredMethod("run", StatusState.class).invoke(null, s);
                } else {
                    bfClass.getDeclaredMethod("run", State.class).invoke(null, s);
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return s;
    }

    @Override
    public State run(final String program, final State s) {
        State state = s;
        return run2(program, state);
    }

    private String convertMethod2Java(final String methodBody, final Map<String, Integer> methodMap, final boolean loop,
            final boolean hasStatus) {
        StringBuilder b = new StringBuilder();
        char[] chars = methodBody.toCharArray();
        int p = 0;
        if (loop) {
            if (hasStatus) {
                b.append("while (s.getValue() != 0 && !s.isTerminated()) {\n");
            } else {
                b.append("while (s.getValue() != 0) {\n");
            }
        }
        while (p < chars.length) {
            char c = chars[p];
            int i = 1;
            switch (c) {

            case '<' :
                i = Utils.getRepeatCount(p, chars);

                b.append("s.addToPointer(").append(-i).append(");\n");
                break;

            case '>' :
                i = Utils.getRepeatCount(p, chars);

                b.append("s.addToPointer(").append(i).append(");\n");
                break;

            case '+' :
                i = Utils.getRepeatCount(p, chars);
                b.append("s.addToValue(").append(i).append(");\n");
                break;

            case '-' :
                i = Utils.getRepeatCount(p, chars);
                b.append("s.addToValue(").append(-i).append(");\n");
                break;

            case '.' :
                b.append("s.print();\n");
                break;

            case '[' :

                int end = Utils.findEndBracket(p, methodBody);
                String body = methodBody.substring(p + 1, end);

                // Optimize
                if ("-".equals(body) || "+".equals(body)) {
                    b.append("s.clearValue();\n");
                } else if ("->+<".equals(body) || ">+<-".equals(body)) {
                    b.append(
                            "add = s.getValue();s.addToPointer(1);s.addToValue(add);s.addToPointer(-1);s.clearValue();\n");
                } else if ("-<+>".equals(body) || "<+>-".equals(body)) {
                    b.append(
                            "add = s.getValue();s.addToPointer(-1);s.addToValue(add);s.addToPointer(1);s.clearValue();\n");
                } else {
                    b.append("meth").append(methodMap.get(body)).append("(s);");
                }

                p = end;

                break;

            case ']' :
                b.append("}\n");
                break;

            default :
                break;
            }

            p += i;
        }

        if (loop) {
            b.append("}\n");
        }

        return b.toString();
    }

    private String convert2Java(final String program, final boolean hasStatus) {
        StringBuilder b = new StringBuilder();
        Map<String, Integer> methodMap = Utils.getMethods(program);
        b.append(convertMethod2Java(program, methodMap, false, hasStatus));
        b.append("}");
        for (Map.Entry<String, Integer> entry : methodMap.entrySet()) {
            if (hasStatus) {
                b.append("private static void meth").append(entry.getValue()).append(" (StatusState s) {byte add;");
            } else {
                b.append("private static void meth").append(entry.getValue()).append(" (State s) {byte add;");
            }

            b.append(convertMethod2Java(entry.getKey(), methodMap, true, hasStatus)).append("}");
        }

        return b.toString();
    }

}
