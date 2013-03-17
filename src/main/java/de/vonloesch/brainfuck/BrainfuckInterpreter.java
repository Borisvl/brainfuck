package de.vonloesch.brainfuck;

public interface BrainfuckInterpreter {

    State run(String program, State s);

}
