package de.vonloesch.brainfuck;

import java.io.UnsupportedEncodingException;

public class SimpleState implements State {
    public byte[] cell = new byte[65536];
    public int pointer = 0;

    public void print() {

        byte c = cell[pointer];
        byte[] b = {c};
        try {
            String str = new String(b, "ASCII");
            System.out.print(str);
        } catch (UnsupportedEncodingException e) { }
    }

    public byte getValue() {
        return cell[pointer];
    }

    public void addToPointer(final int value) {
        pointer += value;
    }

    public void addToValue(final int value) {
        cell[pointer] += value;
    }

    public void clearValue() {
        cell[pointer] = 0;
    }
}
