package io.scriptor.riscvm;

import io.scriptor.riscvm.asm.Operand;

import static io.scriptor.riscvm.ISA.OperandType.IMMEDIATE;
import static io.scriptor.riscvm.ISA.OperandType.REGISTER;
import static io.scriptor.riscvm.Instruction.IType.*;

// https://marks.page/riscv/
// https://riscv-programming.org/book/riscv-book.html
// https://drive.google.com/file/d/1s0lZxUZaa7eV_O0_WsZzaurFLLww7ou5/view
public enum ISA {

    NULL(),

    // Logic, Shift, and Arithmetic instructions
    AND(R, REGISTER, REGISTER, REGISTER),
    OR(R, REGISTER, REGISTER, REGISTER),
    XOR(R, REGISTER, REGISTER, REGISTER),

    ANDI(I, REGISTER, REGISTER, IMMEDIATE),
    ORI(I, REGISTER, REGISTER, IMMEDIATE),
    XORI(I, REGISTER, REGISTER, IMMEDIATE),

    SLL(R, REGISTER, REGISTER, REGISTER),
    SRL(R, REGISTER, REGISTER, REGISTER),
    SRA(R, REGISTER, REGISTER, REGISTER),

    SLLI(I, REGISTER, REGISTER, IMMEDIATE),
    SRLI(I, REGISTER, REGISTER, IMMEDIATE),
    SRAI(I, REGISTER, REGISTER, IMMEDIATE),

    ADD(R, REGISTER, REGISTER, REGISTER),
    SUB(R, REGISTER, REGISTER, REGISTER),

    ADDI(I, REGISTER, REGISTER, IMMEDIATE),
    SUBI(I, REGISTER, REGISTER, IMMEDIATE),

    MUL(R, REGISTER, REGISTER, REGISTER),
    DIV(R, REGISTER, REGISTER, REGISTER),
    REM(R, REGISTER, REGISTER, REGISTER),

    // Data movement instructions
    LUI(U, REGISTER, IMMEDIATE),
    AUIPC(U, REGISTER, IMMEDIATE),

    MV(REGISTER, REGISTER), // pseudo
    LI(REGISTER, IMMEDIATE), // pseudo
    LA(REGISTER, IMMEDIATE), // pseudo

    LW(I, REGISTER, REGISTER, IMMEDIATE), // load: dst, base, offset
    LH(I, REGISTER, REGISTER, IMMEDIATE),
    LB(I, REGISTER, REGISTER, IMMEDIATE),

    SW(S, REGISTER, REGISTER, IMMEDIATE), // store: src, base, offset
    SH(S, REGISTER, REGISTER, IMMEDIATE),
    SB(S, REGISTER, REGISTER, IMMEDIATE),

    // Conditional and Unconditional control-flow instructions
    BEQ(S, REGISTER, REGISTER, IMMEDIATE),
    BNE(S, REGISTER, REGISTER, IMMEDIATE),

    BEQZ(REGISTER, IMMEDIATE), // pseudo
    BNEZ(REGISTER, IMMEDIATE), // pseudo

    BLT(S, REGISTER, REGISTER, IMMEDIATE),
    BGE(S, REGISTER, REGISTER, IMMEDIATE),

    J(IMMEDIATE), // pseudo
    JR(IMMEDIATE), // pseudo

    JAL(U, REGISTER, IMMEDIATE),
    JALR(I, REGISTER, REGISTER, IMMEDIATE),

    RET(), // pseudo

    ECALL(E),
    EBREAK(E),

    // Conditional set instructions
    SLT(R, REGISTER, REGISTER, REGISTER),
    SLTI(I, REGISTER, REGISTER, IMMEDIATE),

    SEQZ(REGISTER, REGISTER), // pseudo
    SNEZ(REGISTER, REGISTER), // pseudo
    SLTZ(REGISTER, REGISTER), // pseudo
    SGTZ(REGISTER, REGISTER), // pseudo

    // Custom instructions
    NOP(), // pseudo

    PUSH(REGISTER), // pseudo
    POP(REGISTER), // pseudo

    ;

    /**
     * t*: temporary register
     * <p>
     * s*: saved register
     * <p>
     * a*: function argument
     */
    public enum RegisterAlias {
        /**
         * hardwired zero
         */
        ZERO,
        /**
         * return address
         */
        RA,
        /**
         * stack pointer
         */
        SP,
        /**
         * global pointer
         */
        GP,
        /**
         * thread pointer
         */
        TP,
        T0, T1, T2,
        S0, S1,
        A0, A1, A2, A3, A4, A5, A6, A7,
        S2, S3, S4, S5, S6, S7, S8, S9, S10, S11,
        T3, T4, T5, T6,
        /**
         * Program counter
         */
        PC;

        public byte asByte() {
            return (byte) ordinal();
        }
    }

    public enum OperandType {
        REGISTER,
        IMMEDIATE,
    }

    public final Instruction.IType itype;
    public final OperandType[] operands;

    ISA(Instruction.IType itype, OperandType... operands) {
        this.itype = itype;
        this.operands = operands;
    }

    ISA(OperandType... operands) {
        this(null, operands);
    }

    public Instruction toInstruction(Operand... ops) {
        return switch (itype) {
            case R -> Instruction.fromR(
                    ordinal(),
                    ops[0].asReg(),
                    ops[1].asReg(),
                    ops[2].asReg());
            case I -> Instruction.fromI(
                    ordinal(),
                    ops[0].asReg(),
                    ops[1].asReg(),
                    ops[2].asImm());
            case S -> Instruction.fromS(
                    ordinal(),
                    ops[0].asReg(),
                    ops[1].asReg(),
                    ops[2].asImm());
            case U -> Instruction.fromU(
                    ordinal(),
                    ops[0].asReg(),
                    ops[1].asImm());
            case E -> Instruction.fromE(
                    ordinal(),
                    true);
        };
    }
}
