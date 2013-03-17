package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class PShiftOp implements Operations {

    int shift;

    public PShiftOp(final int i) {
        shift = i;
    }

    public void execute(final State state) {
        state.addToPointer(shift);
    }

    @Override
    public void execute(final StatusState state) {
        state.addToPointer(shift);
    }

}
