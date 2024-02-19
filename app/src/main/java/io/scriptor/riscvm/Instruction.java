package io.scriptor.riscvm;

public class Instruction {

    public enum IType {
        R, I, S, U, E,
    }

    public final IType itype;

    /**
     * 7 Bits
     */
    public final int opcode;
    /**
     * 5 Bits
     */
    public final int rd;
    /**
     * 5 Bits
     */
    public final int rs1;
    /**
     * 5 Bits
     */
    public final int rs2;
    /**
     * 20(U)/15(S,I)
     */
    public final int imm;

    private Instruction(IType itype, int opcode, int rd, int rs1, int rs2, int imm) {
        this.itype = itype;
        this.opcode = opcode;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.imm = imm;
    }

    public int pack() {
        return switch (itype) {
            case R -> toR(opcode, rd, rs1, rs2);
            case I -> toI(opcode, rd, rs1, imm);
            case S -> toS(opcode, rs1, rs2, imm);
            case U -> toU(opcode, rd, imm);
            case E -> toE(opcode);
        };
    }

    @Override
    public String toString() {
        return switch (itype) {
            case R -> String.format("%5s %4s, %4s, %8s", opcode(opcode), reg(rd), reg(rs1), reg(rs2));
            case I -> String.format("%5s %4s, %4s, %08X", opcode(opcode), reg(rd), reg(rs1), imm);
            case S -> String.format("%5s %4s, %4s, %08X", opcode(opcode), reg(rs1), reg(rs2), imm);
            case U -> String.format("%5s %4s, %4s  %08X", opcode(opcode), reg(rd), "", imm);
            case E -> String.format("%5s %4s  %4s  %8s", opcode(opcode), "", "", "");
        };
    }

    private static ISA.RegisterAlias reg(int reg) {
        return ISA.RegisterAlias.values()[reg];
    }

    private static ISA opcode(int opcode) {
        return ISA.values()[opcode];
    }

    public static int toR(int opcode, int rd, int rs1, int rs2) {
        return (rs2 & 0b11111) << 17 | (rs1 & 0b11111) << 12 | (rd & 0b11111) << 7 | (opcode & 0b1111111);
    }

    public static int toI(int opcode, int rd, int rs1, int imm) {
        return (imm & 0b111111111111111) << 17 | (rs1 & 0b11111) << 12 | (rd & 0b11111) << 7 | (opcode & 0b1111111);
    }

    public static int toS(int opcode, int rs1, int rs2, int imm) {
        return (imm & 0b111111111111111) << 17 | (rs2 & 0b11111) << 12 | (rs1 & 0b11111) << 7 | (opcode & 0b1111111);
    }

    public static int toU(int opcode, int rd, int imm) {
        return (imm & 0b11111111111111111111) << 12 | (rd & 0b11111) << 7 | (opcode & 0b1111111);
    }

    public static int toE(int opcode) {
        return opcode & 0b1111111;
    }

    public static Instruction fromR(int inst) {
        final int opcode = getOpcode(inst);
        final int rd = (inst >> 7) & 0b11111;
        final int rs1 = (inst >> 12) & 0b11111;
        final int rs2 = (inst >> 17) & 0b11111;

        return new Instruction(IType.R, opcode, rd, rs1, rs2, 0);
    }

    public static Instruction fromI(int inst) {
        final int opcode = getOpcode(inst);
        final int rd = (inst >> 7) & 0b11111;
        final int rs1 = (inst >> 12) & 0b11111;
        final int imm = (inst >> 17) & 0b111111111111111;

        return new Instruction(IType.I, opcode, rd, rs1, 0, imm);
    }

    public static Instruction fromS(int inst) {
        final int opcode = getOpcode(inst);
        final int rs1 = (inst >> 7) & 0b11111;
        final int rs2 = (inst >> 12) & 0b11111;
        final int imm = (inst >> 17) & 0b111111111111111;

        return new Instruction(IType.S, opcode, 0, rs1, rs2, imm);
    }

    public static Instruction fromU(int inst) {
        final int opcode = getOpcode(inst);
        final int rd = (inst >> 7) & 0b11111;
        final int imm = (inst >> 12) & 0b11111111111111111111;

        return new Instruction(IType.U, opcode, rd, 0, 0, imm);
    }

    public static Instruction fromE(int inst) {
        final int opcode = getOpcode(inst);

        return new Instruction(IType.E, opcode, 0, 0, 0, 0);
    }

    public static Instruction fromR(int opcode, int rd, int rs1, int rs2) {
        return new Instruction(IType.R, opcode, rd, rs1, rs2, 0);
    }

    public static Instruction fromI(int opcode, int rd, int rs1, int imm) {
        return new Instruction(IType.I, opcode, rd, rs1, 0, imm);
    }

    public static Instruction fromS(int opcode, int rs1, int rs2, int imm) {
        return new Instruction(IType.S, opcode, 0, rs1, rs2, imm);
    }

    public static Instruction fromU(int opcode, int rd, int imm) {
        return new Instruction(IType.U, opcode, rd, 0, 0, imm);
    }

    public static Instruction fromE(int opcode, boolean unused) {
        return new Instruction(IType.E, opcode, 0, 0, 0, 0);
    }

    public static int getOpcode(int inst) {
        return inst & 0b1111111;
    }

    public static Instruction valueOf(int inst) {
        final var opcode = getOpcode(inst);
        if (opcode < 0 || opcode >= ISA.values().length)
            return null;

        final var itype = ISA.values()[opcode].itype;
        if (itype == null)
            return null;

        return switch (itype) {
            case R -> fromR(inst);
            case I -> fromI(inst);
            case S -> fromS(inst);
            case U -> fromU(inst);
            case E -> fromE(inst);
        };
    }
}
