package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.ISA;
import io.scriptor.riscvm.ISA.RegisterAlias;
import io.scriptor.riscvm.Instruction;

import static io.scriptor.riscvm.ISA.RegisterAlias.*;

public class CPU extends VMComponent {

    private final int[] mRegisters;

    public CPU(Machine machine) {
        super(machine);
        mRegisters = new int[32];
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder()
                .append("Registers: ").append(mRegisters.length);

        for (int i = 0; i < mRegisters.length; i++)
            builder.append(String.format("%n%08X: %08X", i, mRegisters[i]));

        return builder.toString();
    }

    public void cycle() {
        final var instruction = getMachine().getMemory().getWord(nextPC());

        final var oc = Instruction.getOpcode(instruction);
        final var instCode = ISA.values()[oc];

        final var inst = switch (instCode.itype) {
            case R -> Instruction.fromR(instruction);
            case I -> Instruction.fromI(instruction);
            case S -> Instruction.fromS(instruction);
            case U -> Instruction.fromU(instruction);
            case E -> Instruction.fromE(instruction);
        };

        final var rd = inst.rd;
        final var rs1 = inst.rs1;
        final var rs2 = inst.rs2;
        final var imm = inst.imm;

        switch (instCode) {
            case AND -> set(rd, get(rs1) & get(rs2));
            case OR -> set(rd, get(rs1) | get(rs2));
            case XOR -> set(rd, get(rs1) ^ get(rs2));
            case ANDI -> set(rd, get(rs1) & imm);
            case ORI -> set(rd, get(rs1) | imm);
            case XORI -> set(rd, get(rs1) ^ imm);
            case SLL -> set(rd, get(rs1) << get(rs2));
            case SRL -> set(rd, get(rs1) >>> get(rs2));
            case SRA -> set(rd, get(rs1) >> get(rs2));
            case SLLI -> set(rd, get(rs1) << imm);
            case SRLI -> set(rd, get(rs1) >>> imm);
            case SRAI -> set(rd, get(rs1) >> imm);
            case ADD -> set(rd, get(rs1) + get(rs2));
            case SUB -> set(rd, get(rs1) - get(rs2));
            case ADDI -> set(rd, get(rs1) + imm);
            case SUBI -> set(rd, get(rs1) - imm);
            case MUL -> set(rd, get(rs1) * get(rs2));
            case DIV -> set(rd, get(rs1) / get(rs2));
            case REM -> set(rd, get(rs1) % get(rs2));

            case LW -> set(rd, getMachine().getMemory().getWord(get(rs1) + imm));
            case LH -> set(rd, getMachine().getMemory().getHalf(get(rs1) + imm));
            case LB -> set(rd, getMachine().getMemory().getByte(get(rs1) + imm));
            case SW -> getMachine().getMemory().setWord(get(rs2) + imm, get(rs1));
            case SH -> getMachine().getMemory().setHalf(get(rs2) + imm, (short) get(rs1));
            case SB -> getMachine().getMemory().setByte(get(rs2) + imm, (byte) get(rs1));

            case BEQ -> {
                if (get(rs1) == get(rs2))
                    set(PC, imm);
            }
            case BNE -> {
                if (get(rs1) != get(rs2))
                    set(PC, imm);
            }
            case BLT -> {
                if (get(rs1) < get(rs2))
                    set(PC, imm);
            }
            case BGE -> {
                if (get(rs1) >= get(rs2))
                    set(PC, imm);
            }
            case JAL -> {
                set(rd, get(PC));
                set(PC, imm);
            }
            case JALR -> {
                set(rd, get(PC));
                set(PC, get(rs1) + imm);
            }

            case SLT -> set(rd, get(rs1) < get(rs2) ? 1 : 0);
            case SLTI -> set(rd, get(rs1) < imm ? 1 : 0);

            case ECALL -> ecall();

            default -> throw new IllegalStateException("Unexpected value: " + inst);
        }
    }

    public void set(RegisterAlias a, int word) {
        if (a == ZERO) return; // Zero hardwired to NULL
        this.mRegisters[a.ordinal() - 1] = word;
    }

    public void set(int i, int word) {
        if (i == 0) return; // Zero hardwired to NULL
        this.mRegisters[i - 1] = word;
    }

    public int get(RegisterAlias a, int offset) {
        a = RegisterAlias.values()[a.ordinal() + offset];
        if (a == ZERO) return 0; // Zero hardwired to NULL
        return this.mRegisters[a.ordinal() - 1];
    }

    public int get(RegisterAlias a) {
        if (a == ZERO) return 0; // Zero hardwired to NULL
        return this.mRegisters[a.ordinal() - 1];
    }

    public int get(int i) {
        if (i == 0) return 0; // Zero hardwired to NULL
        return this.mRegisters[i - 1];
    }

    private int nextPC() {
        final var pc = get(PC);
        set(PC, pc + 4);
        return pc;
    }

    private void ecall() {
        switch (get(A7)) {

            /*
              write:
               a0: fd
               a1: buf
               a2: count
             */
            case 64 -> {
                final var fd = get(A0);
                final var buf = get(A1);
                final var count = get(A2);

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
                throw new ExitSignal(get(A0));
            }
        }
    }
}
