package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.core.ISA;
import io.scriptor.riscvm.core.Instruction;

public class CU {

    private final CPU mCPU;

    public CU(CPU cpu) {
        mCPU = cpu;
    }

    public void cycle() {
        mCPU.instructionRegister(mCPU.getMachine().getMemory().getWord(mCPU.nextProgramCounter()));
        final var instruction = Instruction.valueOf(mCPU.instructionRegister());
        mCPU.opcode(ISA.values()[instruction.opcode]);
        mCPU.rd(instruction.rd);
        mCPU.rs1(instruction.rs1);
        mCPU.rs2(instruction.rs2);
        mCPU.imm(instruction.imm);
    }
}
