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

    public Memory getMemory() {
        return mMemory;
    }

    public CPU getCPU() {
        return mCPU;
    }

    public void cycle() {
        mCPU.cycle();
    }

    public void reset() {
        mMemory.reset();
        mCPU.reset();
    }
}
