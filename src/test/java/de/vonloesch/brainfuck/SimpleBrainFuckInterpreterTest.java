package de.vonloesch.brainfuck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class SimpleBrainFuckInterpreterTest {

    @Test
    public void testHelloWorld() throws IOException {
        String line = BFTestUtils.readBFProgram("src/test/resources/bf/hello.bf");

        BrainfuckInterpreter interpreter = new SimpleBrainfuckInterpreter();
        SaveOutputState state = new SaveOutputState();
        interpreter.run(line, state);
        assertEquals("Hello World!\n", state.getOutput());
        System.out.println(state.getOutput());
    }

    @Test
    public void terminationTest() throws IOException {
        String builder = BFTestUtils.readBFProgram("src/test/resources/bf/mandelbrot.b");

        final long instructions = 1000 * 1000 * 2;
        BrainfuckInterpreter interpreter = new SimpleBrainfuckInterpreter();
        CountingState state = new CountingState(instructions);
        interpreter.run(builder, state);

        System.out.println(state.getOutput());
        System.out.println("Instruction count: " + state.getInstructionCount());

        System.out.println();
        assertTrue(state.getInstructionCount() - instructions < instructions / 100);
        assertTrue(state.getOutput().startsWith("AAAAAAAAAAAAAAAAB"));
    }

    @Ignore("Too slow...")
    @Test
    public void perfTest() throws IOException {
        String line = BFTestUtils.readBFProgram("src/test/resources/bf/mandelbrot.b");

        long time1 = System.currentTimeMillis();
        BrainfuckInterpreter interpreter = new SimpleBrainfuckInterpreter();
        interpreter.run(line, new SimpleState());

        long time2 = System.currentTimeMillis();
        System.out.println();
        System.out.println("Simple: " + (time2 - time1) + " ms");
    }

    @Test
    public void selfTest() throws IOException {
        String builder = BFTestUtils.readBFProgram("src/test/resources/bf/self.bf");

        long time1 = System.currentTimeMillis();
        BrainfuckInterpreter interpreter = new SimpleBrainfuckInterpreter();

        SaveOutputState state = new SaveOutputState();
        interpreter.run(builder, state);

        long time2 = System.currentTimeMillis();
        System.out.println(state.getOutput());
        System.out.println();
        System.out.println(time2 - time1 + " ms");
        assertEquals(builder.toString(), state.getOutput() + '\n');
    }

}
