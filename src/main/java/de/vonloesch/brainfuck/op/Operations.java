package de.vonloesch.brainfuck.op;

import de.vonloesch.brainfuck.State;
import de.vonloesch.brainfuck.StatusState;

public interface Operations {
    void execute(State state);

    void execute(StatusState state);
}
