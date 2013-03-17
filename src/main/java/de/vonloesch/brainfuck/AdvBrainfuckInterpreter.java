package de.vonloesch.brainfuck;

import java.util.ArrayList;
import java.util.List;

import de.vonloesch.brainfuck.op.AddOp;
import de.vonloesch.brainfuck.op.AddOp2;
import de.vonloesch.brainfuck.op.ClearOp;
import de.vonloesch.brainfuck.op.Loop;
import de.vonloesch.brainfuck.op.Operations;
import de.vonloesch.brainfuck.op.PShiftOp;
import de.vonloesch.brainfuck.op.PrintOp;

/**
 * Simple brainfuck interpreter that uses the command pattern to execute the bf program.
 *
 * @author  Boris von Loesch
 */
public class AdvBrainfuckInterpreter implements BrainfuckInterpreter {
    private Operations[] operations;

    public State run(final String program, final State s) {

        operations = compile(program);
        if (s instanceof StatusState) {
            StatusState statusState = (StatusState) s;
            for (Operations operation : operations) {
                operation.execute(statusState);
                if ((statusState).isTerminated()) {
                    break;
                }
            }
        } else {
            for (Operations operation : operations) {
                operation.execute(s);
            }
        }

        return s;
    }

    public static Operations[] compile(final String program) {
        List<Operations> operations = new ArrayList<Operations>();
        int p = 0;
        char[] chars = program.toCharArray();
        while (p < chars.length) {
            char c = chars[p];
            int i = 1;
            switch (c) {

            case '<' :

                i = Utils.getRepeatCount(p, chars);

                operations.add(new PShiftOp(-i));
                break;

            case '>' :

                i = Utils.getRepeatCount(p, chars);

                operations.add(new PShiftOp(i));
                break;

            case '+' :
                i = Utils.getRepeatCount(p, chars);
                operations.add(new AddOp(i));
                break;

            case '-' :
                i = Utils.getRepeatCount(p, chars);
                operations.add(new AddOp(-i));
                break;

            case '.' :
                operations.add(new PrintOp());
                break;

            case '[' :

                int end = Utils.findEndBracket(p, program);
                String body = program.substring(p + 1, end);

                // Optimize
                if ("-".equals(body) || "+".equals(body)) {
                    operations.add(new ClearOp());
                } else if ("->+<".equals(body) || ">+<-".equals(body)) {
                    operations.add(new AddOp2(1));
                } else if ("-<+>".equals(body) || "<+>-".equals(body)) {
                    operations.add(new AddOp2(-1));
                } else {
                    operations.add(new Loop(body));
                }

                p = end;
                break;

            default :
                break;
            }

            p += i;
        }

        return operations.toArray(new Operations[] {});
    }

}
