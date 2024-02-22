package io.scriptor.riscvm.vm;

public class Machine {

    private final Memory mMemory;
    private final CPU mCPU;

    public Machine(VMConfig config) {
        this(config.memory());
    }

    public Machine(int memory) {
        this.mMemory = new Memory(this, memory);
        this.mCPU = new CPU(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("----------- Memory ----------\n")
                .append(mMemory).append('\n')
                .append("------------ CPU ------------\n")
                .append(mCPU)
                .toString();
    }

    public Memory getMemory() {
        return this.mMemory;
    }

    public CPU getCPU() {
        return this.mCPU;
    }

    public void cycle() {
        mCPU.cycle();
    }

    public void reset() {
        mMemory.reset();
        mCPU.reset();
    }
}
