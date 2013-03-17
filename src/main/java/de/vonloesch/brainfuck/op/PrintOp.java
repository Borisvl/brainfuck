package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class PrintOp implements Operations {

    public void execute(final State state) {
        state.print();
    }

    public void execute(final StatusState state) {
        state.print();
    }
}
