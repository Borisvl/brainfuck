package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class AddOp2 implements Operations {

    private int shift;

    public AddOp2(final int shift) {
        this.shift = shift;
    }

    public void execute(final State state) {
        byte add = state.getValue();
        state.addToPointer(shift);
        state.addToValue(add);
        state.addToPointer(-shift);
        state.clearValue();
    }

    public void execute(final StatusState state) {
        execute((State) state);
    }

}
