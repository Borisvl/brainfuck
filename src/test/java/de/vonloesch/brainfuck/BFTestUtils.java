package de.vonloesch.brainfuck;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BFTestUtils {

    static String readBFProgram(final String program) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(program));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            builder.append(line).append('\n');
        }
    
        in.close();
        return builder.toString();
    }

}
