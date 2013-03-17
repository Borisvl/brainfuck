package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class ClearOp implements Operations {

    public void execute(final State state) {
        state.clearValue();
    }

    @Override
    public void execute(final StatusState state) {
        state.clearValue();
    }
}
