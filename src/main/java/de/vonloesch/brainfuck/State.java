package de.vonloesch.brainfuck;

public interface State {

    void print();

    byte getValue();

    void addToPointer(int value);

    void addToValue(int value);

    void clearValue();
}
