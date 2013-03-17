package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.AdvBrainfuckInterpreter;
import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class Loop implements Operations {
    final Operations[] operations;

    public Loop(final String subProgram) {
        operations = AdvBrainfuckInterpreter.compile(subProgram);
    }

    public void execute(final State state) {
        while (state.getValue() != 0) {
            for (Operations operation : operations) {
                operation.execute(state);
            }
        }
    }

    public void execute(final StatusState state) {

        // Wrong position but faster
        while (state.getValue() != 0 && !state.isTerminated()) {
            for (Operations operation : operations) {
                operation.execute(state);
            }
        }
    }
}
