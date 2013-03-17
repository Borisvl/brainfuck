package de.vonloesch.brainfuck;

/**
 * State that counts the number of operations. It makes sure that the interpreter stops executing gently when the number
 * of maxInstructions is reached.
 *
 * @author  Boris von Loesch
 */
public class CountingState extends SaveOutputState implements StatusState {
    private long instructionCount = 0;
    private long maxInstructions;

    public CountingState(final long maxInstructions) {
        this.maxInstructions = maxInstructions;
    }

    public void print() {
        instructionCount++;
        super.print();
    }

    private int getPointerValue() {
        int i = pointer % 65536;
        if (i < 0) {
            i += 65536;
        }

        return i;
    }

    public byte getValue() {
        instructionCount++;
        return cell[getPointerValue()];
    }

    public void addToPointer(final int value) {
        instructionCount++;
        pointer += value;
    }

    public void addToValue(final int value) {
        instructionCount++;
        cell[getPointerValue()] += value;
    }

    public void clearValue() {
        instructionCount++;
        cell[getPointerValue()] = 0;
    }

    public long getInstructionCount() {
        return instructionCount;
    }

    @Override
    public boolean isTerminated() {
        return maxInstructions < instructionCount;
    }
}
