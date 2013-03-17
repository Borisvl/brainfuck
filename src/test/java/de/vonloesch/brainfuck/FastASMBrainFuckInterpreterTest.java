package de.vonloesch.brainfuck;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

public class FastASMBrainFuckInterpreterTest {

    @Test
    public void testHelloWorld() throws IOException {
        String line = BFTestUtils.readBFProgram("src/test/resources/bf/hello.bf");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;

        // Tell Java to use your special stream
        System.setOut(ps);

        BrainfuckInterpreter interpreter = new FastASMBrainfuckInterpreter();
        interpreter.run(line, null);
        System.setOut(old);
        assertEquals("Hello World!\n", baos.toString());
    }

    @Test
    public void perfTest() throws IOException {
        String line = BFTestUtils.readBFProgram("src/test/resources/bf/mandelbrot.b");

        long time1 = System.currentTimeMillis();
        BrainfuckInterpreter interpreter = new FastASMBrainfuckInterpreter();
        interpreter.run(line, null);

        long time2 = System.currentTimeMillis();
        System.out.println();
        System.out.println("Fast ASM: " + (time2 - time1) + " ms");
    }

}
