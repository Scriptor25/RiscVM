package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.core.ISA;

import java.util.Arrays;

public class CPU extends VMComponent {

    private final int[] mRegisters = new int[31];
    private int mProgramCounter = 0;

    private int mInstructionRegister;
    private ISA mOpcode;
    private int mRD;
    private int mRS1;
    private int mRS2;
    private int mImm;

    private final ALU mALU = new ALU(this);
    private final CU mCU = new CU(this);

    public CPU(Machine machine) {
        super(machine);
    }

    public int nextProgramCounter() {
        final var pc = mProgramCounter;
        mProgramCounter += 4;
        return pc;
    }

    public int[] getRegisters() {
        return mRegisters;
    }

    public void set(int i, int word) {
        if (i > 0) mRegisters[i - 1] = word;
    }

    public int get(int i) {
        if (i > 0) return mRegisters[i - 1];
        return 0;
    }

    public void setrd(int word) {
        set(mRD, word);
    }

    public int getrs1() {
        return get(mRS1);
    }

    public int getrs2() {
        return get(mRS2);
    }

    public void programCounter(int address) {
        mProgramCounter = address;
    }

    public int programCounter() {
        return mProgramCounter;
    }

    public void instructionRegister(int value) {
        mInstructionRegister = value;
    }

    public int instructionRegister() {
        return mInstructionRegister;
    }

    public ISA opcode() {
        return mOpcode;
    }

    public int imm() {
        return mImm;
    }

    public void opcode(ISA opcode) {
        mOpcode = opcode;
    }

    public void rd(int rd) {
        mRD = rd;
    }

    public void rs1(int rs1) {
        mRS1 = rs1;
    }

    public void rs2(int rs2) {
        mRS2 = rs2;
    }

    public void imm(int imm) {
        mImm = imm;
    }

    public void ecall() {
        switch (get(ISA.RegisterAlias.A7.ordinal())) {

            /*
              write:
               a0: fd
               a1: buf
               a2: count
             */
            case 64 -> {
                final var fd = get(ISA.RegisterAlias.A0.ordinal());
                final var buf = get(ISA.RegisterAlias.A1.ordinal());
                final var count = get(ISA.RegisterAlias.A2.ordinal());

                final var ascii = getMachine().getMemory().getASCII(buf, count);

                switch (fd) {
                    // std out
                    case 1 -> System.out.print(ascii);
                    // std err
                    case 2 -> System.err.print(ascii);
                }
            }

            /*
             * exit:
             *  a0: code
             */
            case 93 -> {
                throw new ExitSignal(get(ISA.RegisterAlias.A0.ordinal()));
            }
        }
    }

    public void ebreak() {
        throw new BreakPoint();
    }

    @Override
    public void reset() {
        Arrays.fill(mRegisters, 0);
        mProgramCounter = 0;
    }

    @Override
    public void cycle() {
        mCU.cycle();
        mALU.cycle();
    }
}
