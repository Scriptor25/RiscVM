package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.asm.ASMConfig;

public class Machine {

    private final Memory mMemory;
    private final CPU mCPU;
    private final SystemBus mBus;

    public Machine(ASMConfig config) {
        this(config.machine_memory(), config.machine_registers());
    }

    public Machine(int memory, int registers) {
        this.mMemory = new Memory(this, memory);
        this.mCPU = new CPU(this, registers);
        this.mBus = new SystemBus(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("----------- Memory ----------\n")
                .append(mMemory).append('\n')
                .append("------------ CPU ------------\n")
                .append(mCPU).append('\n')
                .append("------------ Bus ------------\n")
                .append(mBus).append('\n')
                .toString();
    }

    public Memory getMemory() {
        return this.mMemory;
    }

    public CPU getCPU() {
        return this.mCPU;
    }

    public SystemBus getBus() {
        return this.mBus;
    }

    public void cycle() {
        mMemory.cycle();
        mCPU.cycle();
    }
}
