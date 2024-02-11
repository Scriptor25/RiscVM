package io.scriptor.riscvm;

import static io.scriptor.riscvm.RV32IM.OperandType.IMMEDIATE;
import static io.scriptor.riscvm.RV32IM.OperandType.REGISTER;

// https://marks.page/riscv/
// https://riscv-programming.org/book/riscv-book.html
public enum RV32IM {

    NULL(false),

    // Logic, Shift, and Arithmetic instructions
    AND(false, REGISTER, REGISTER, REGISTER),
    OR(false, REGISTER, REGISTER, REGISTER),
    XOR(false, REGISTER, REGISTER, REGISTER),
    ANDI(false, REGISTER, REGISTER, IMMEDIATE),
    ORI(false, REGISTER, REGISTER, IMMEDIATE),
    XORI(false, REGISTER, REGISTER, IMMEDIATE),
    SLL(false, REGISTER, REGISTER, REGISTER),
    SRL(false, REGISTER, REGISTER, REGISTER),
    SRA(false, REGISTER, REGISTER, REGISTER),
    SLLI(false, REGISTER, REGISTER, IMMEDIATE),
    SRLI(false, REGISTER, REGISTER, IMMEDIATE),
    SRAI(false, REGISTER, REGISTER, IMMEDIATE),
    ADD(false, REGISTER, REGISTER, REGISTER),
    SUB(false, REGISTER, REGISTER, REGISTER),
    ADDI(false, REGISTER, REGISTER, IMMEDIATE),
    SUBI(false, REGISTER, REGISTER, IMMEDIATE),
    MUL(false, REGISTER, REGISTER, REGISTER),
    DIV(false, REGISTER, REGISTER, REGISTER),
    DIVU(false, REGISTER, REGISTER, REGISTER),
    REM(false, REGISTER, REGISTER, REGISTER),
    REMU(false, REGISTER, REGISTER, REGISTER),

    // Data movement instructions
    MV(true, REGISTER, REGISTER),
    LI(true, REGISTER, IMMEDIATE),
    LA(true, REGISTER, IMMEDIATE),
    LW(false, REGISTER, REGISTER),
    LH(false, REGISTER, REGISTER),
    LHU(false, REGISTER, REGISTER),
    LB(false, REGISTER, REGISTER),
    LBU(false, REGISTER, REGISTER),
    SW(false, REGISTER, REGISTER),
    SH(false, REGISTER, REGISTER),
    SB(false, REGISTER, REGISTER),
    // TODO: l{w|h|hu|b|bu} rd, lab   For each one of the lw, lh, lhu, lb, and lbu machine instructions there is a pseudo-instruction that performs the same operation, but the memory address is calculated based on a label (lab)(pseudo-instruction).
    // TODO: s{w|h|b} rd, lab         For each one of the sw, sh, and sb machine instructions there is a pseudo-instruction that performs the same operation, but the memory address is calculated based on a label (lab)(pseudo-instruction).

    // Conditional and Unconditional control-flow instructions
    BEQ(false, REGISTER, REGISTER, IMMEDIATE),
    BNE(false, REGISTER, REGISTER, IMMEDIATE),
    BEQZ(true, REGISTER, IMMEDIATE),
    BNEZ(true, REGISTER, IMMEDIATE),
    BLT(false, REGISTER, REGISTER, IMMEDIATE),
    BLTU(false, REGISTER, REGISTER, IMMEDIATE),
    BGE(false, REGISTER, REGISTER, IMMEDIATE),
    BGEU(false, REGISTER, REGISTER, IMMEDIATE),
    J(true, IMMEDIATE),
    JR(true, IMMEDIATE),
    // TODO: jal lab    Stores the return address (PC+4) on the return register (ra), then jumps to label lab (pseudo-instruction).
    JAL(false, REGISTER, IMMEDIATE),
    JALR(false, REGISTER, REGISTER, IMMEDIATE),
    RET(true),
    ECALL(false),
    MRET(false),

    // TODO: Control and Status Registers Read and Write instructions
    // TODO: csrr rd, csr         Copies the value from the control and status register csr into register rd (pseudo-instruction).
    // TODO: csrw csr, rs         Copies the value from register rs into the control and status register csr (pseudo-instruction).
    // TODO: csrrw rd, csr, rs1   Copies the value from the control and status register csr into register rd and the value from the rs1 register to the control and status register csr. If rd=rs1, the instruction performs an atomic swap between registers csr and rs1.
    // TODO: csrc csr, rs         Clears control and status register (csr) bits using the contents of the rs register as a bit mask.(pseudo-instruction).
    // TODO: csrs csr, rs         Sets control and status register (csr) bits using the contents of the rs register as a bit mask.(pseudo-instruction).

    // Conditional set instructions
    SLT(false, REGISTER, REGISTER, REGISTER),
    SLTI(false, REGISTER, REGISTER, IMMEDIATE),
    SLTU(false, REGISTER, REGISTER, REGISTER),
    SLTIU(false, REGISTER, REGISTER, IMMEDIATE),
    SEQZ(true, REGISTER, REGISTER),
    SNEZ(true, REGISTER, REGISTER),
    SLTZ(true, REGISTER, REGISTER),
    SGTZ(true, REGISTER, REGISTER),

    // Custom instructions
    SYS(false, IMMEDIATE),
    NOP(true),
    PUSH(true, REGISTER),
    POP(true, REGISTER),

    ;

    public static final int INST_SIZE = 4;
    public static final int BYTES = 16;

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
    }

    public enum OperandType {
        REGISTER,
        IMMEDIATE,
    }

    public final boolean pseudo;
    public final OperandType[] operands;

    RV32IM(boolean pseudo, OperandType... operands) {
        this.pseudo = pseudo;
        this.operands = operands;
    }
}
