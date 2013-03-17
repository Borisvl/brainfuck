package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public class AddOp implements Operations {

    final int summand;

    public AddOp(final int i) {
        summand = i;
    }

    public void execute(final State state) {
        state.addToValue(summand);
    }

    @Override
    public void execute(final StatusState state) {
        state.addToValue(summand);
    }
}
