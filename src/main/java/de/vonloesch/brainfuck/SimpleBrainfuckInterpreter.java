package de.vonloesch.brainfuck;

import java.util.Stack;

public class SimpleBrainfuckInterpreter implements BrainfuckInterpreter {
    State state;
    String globalProgram;
    int[] jumpMarkers;
    boolean hasStatus;

    public void compile(String program) {
        program = optimize(program);
        jumpMarkers = new int[program.length()];
        for (int i = 0; i < program.length(); i++) {
            if (program.charAt(i) == '[') {
                jumpMarkers[i] = i + extract(i, program).length() + 1;
            }
        }

        globalProgram = program;
    }

    private String optimize(final String program) {
        String opt = program.replaceAll("\\[[-\\+]\\]", "0");
        return opt;
    }

    private String extract(final int i, final String program) {
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

        String subprogram = program.substring(i + 1, p - 1);
        return subprogram;
    }

    public State run(final String program, final State s) {
        state = s;
        compile(program);
        hasStatus = false;
        if (s instanceof StatusState) {
            hasStatus = true;
        }

        runInternal(globalProgram);
        return state;
    }

    private void runInternal(final String program) {
        int p = 0;
        char[] chars = program.toCharArray();
        Stack<Integer> loopb = new Stack<Integer>();
        while (p < chars.length) {
            char c = chars[p];
            switch (c) {

            case '<' :
                state.addToPointer(-1);
                break;

            case '>' :
                state.addToPointer(1);
                break;

            case '-' :
                state.addToValue(-1);
                break;

            case '+' :
                state.addToValue(1);
                break;

            case '0' :
                state.clearValue();
                break;

            case '[' :
                if (state.getValue() != 0) {
                    loopb.push(p);
                } else {

                    // p += subprograms.get(p).length()+1;
                    p = jumpMarkers[p];
                }

                break;

            case ']' :
                if (state.getValue() != 0) {
                    p = loopb.peek();
                } else {
                    loopb.pop();
                }

                break;

            case '.' :
                state.print();

            default :
                break;
            }

            p++;
            if (hasStatus && ((StatusState) state).isTerminated()) {
                break;
            }
        }
    }
}
