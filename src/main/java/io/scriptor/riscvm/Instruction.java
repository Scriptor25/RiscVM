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
            case R -> String.format("%s{%s, %s, %s}", ISA.values()[opcode], rd, rs1, rs2);
            case I -> String.format("%s{%s, %s, %s}", ISA.values()[opcode], rd, rs1, imm);
            case S -> String.format("%s{%s, %s, %s}", ISA.values()[opcode], rs1, rs2, imm);
            case U -> String.format("%s{%s, %s}", ISA.values()[opcode], rd, imm);
            case E -> String.format("%s{}", ISA.values()[opcode]);
        };
    }

    private String toBinString() {
        return switch (itype) {
            case R -> String.format("%s %s %s %s %s %s",
                    itype,
                    toBin(0, 10, 0),
                    toBin(rs2, 5, 0),
                    toBin(rs1, 5, 0),
                    toBin(rd, 5, 0),
                    toBin(opcode, 7, 0));
            case I -> String.format("%s %s %s %s %s %s",
                    itype,
                    toBin(imm, 10, 5), toBin(imm, 5, 0),
                    toBin(rs1, 5, 0),
                    toBin(rd, 5, 0),
                    toBin(opcode, 7, 0));
            case S -> String.format("%s %s %s %s %s %s",
                    itype,
                    toBin(imm, 10, 5), toBin(imm, 5, 0),
                    toBin(rs2, 5, 0),
                    toBin(rs1, 5, 0),
                    toBin(opcode, 7, 0));
            case U -> String.format("%s %s %s %s %s %s",
                    itype,
                    toBin(imm, 10, 10), toBin(imm, 5, 5), toBin(imm, 5, 0),
                    toBin(rd, 5, 0),
                    toBin(opcode, 7, 0));
            case E -> String.format("%s %s %s %s %s %s",
                    itype,
                    toBin(0, 10, 0),
                    toBin(0, 5, 0),
                    toBin(0, 5, 0),
                    toBin(0, 5, 0),
                    toBin(opcode, 7, 0));
        };
    }

    private static String toBin(int i, int n, int o) {
        final var str = String.format("%" + n + "s", Integer.toBinaryString(i >> o)).replaceAll(" ", "0");
        return str.substring(str.length() - n);
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
        if (0 < opcode && opcode < ISA.values().length)
            return switch (ISA.values()[opcode].itype) {
                case R -> fromR(inst);
                case I -> fromI(inst);
                case S -> fromS(inst);
                case U -> fromU(inst);
                case E -> fromE(inst);
            };
        return null;
    }
}
