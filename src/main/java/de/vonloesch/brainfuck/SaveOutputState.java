package de.vonloesch.brainfuck;

import java.io.UnsupportedEncodingException;

public class SaveOutputState extends SimpleState implements State {

    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void print() {

        byte c = getValue();
        if (!(c == 13 || c == 10 || c >= 32 || c <= 126)) {
            return;
        }

        byte[] b = {c};
        try {
            String str = new String(b, "ASCII");
            stringBuilder.append(str);
        } catch (UnsupportedEncodingException e) { }
    }

    public String getOutput() {
        return stringBuilder.toString();
    }
}
