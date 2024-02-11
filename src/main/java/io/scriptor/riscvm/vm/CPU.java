package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.RV32IM;

import static io.scriptor.riscvm.RV32IM.INST_SIZE;
import static io.scriptor.riscvm.RV32IM.RegisterAlias.*;
import static io.scriptor.riscvm.vm.SystemBus.*;

public class CPU extends VMComponent {

    private final int[] mRegisters;

    private int mNanoCounter = -1;
    private int mProgramCounter = 0;
    private final int[] mInstructionRegister = new int[INST_SIZE]; // inst o0 o1 o2

    private Runnable mNext;
    private final int[] mBusBackup = new int[3]; // control, address, data

    public CPU(Machine machine, int registers) {
        super(machine);
        mRegisters = new int[registers];
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder()
                .append("Registers: ").append(mRegisters.length);

        for (int i = 0; i < mRegisters.length; i++)
            builder.append(String.format("%n%08X: %08X", i, mRegisters[i]));

        return builder.toString();
    }

    @Override
    public void cycle() {
        if (mNext != null) {
            mNext.run();
            mNext = null;
            restoreBus();
            mNanoCounter = -1;
            return;
        }

        if (mNanoCounter < 0) {
            getMachine().getBus().writeControl(CTRL_MEM_READ_WORD).writeAddress(nextPC());
            mNanoCounter = 0;
            return;
        }

        mInstructionRegister[mNanoCounter++] = getMachine().getBus().readData();
        if (mNanoCounter >= INST_SIZE) {
            mNanoCounter = 0;

            final var opcode = mInstructionRegister[0];
            final var o0 = mInstructionRegister[1];
            final var o1 = mInstructionRegister[2];
            final var o2 = mInstructionRegister[3];

            final var inst = RV32IM.values()[opcode];
            switch (inst) {
                case AND -> set(o0, get(o1) & get(o2));
                case OR -> set(o0, get(o1) | get(o2));
                case XOR -> set(o0, get(o1) ^ get(o2));
                case ANDI -> set(o0, get(o1) & o2);
                case ORI -> set(o0, get(o1) | o2);
                case XORI -> set(o0, get(o1) ^ o2);
                case SLL -> set(o0, get(o1) << get(o2));
                case SRL -> set(o0, get(o1) >>> get(o2));
                case SRA -> set(o0, get(o1) >> get(o2));
                case SLLI -> set(o0, get(o1) << o2);
                case SRLI -> set(o0, get(o1) >>> o2);
                case SRAI -> set(o0, get(o1) >> o2);
                case ADD -> set(o0, get(o1) + get(o2));
                case SUB -> set(o0, get(o1) - get(o2));
                case ADDI -> set(o0, get(o1) + o2);
                case SUBI -> set(o0, get(o1) - o2);
                case MUL -> set(o0, get(o1) * get(o2));
                case DIV, DIVU -> set(o0, get(o1) / get(o2));
                case REM, REMU -> set(o0, get(o1) % get(o2));

                case LW -> {
                    next(() -> set(o0, getMachine().getBus().readData()));
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_READ_WORD)
                            .writeAddress(get(o1));
                }
                case LH, LHU -> {
                    next(() -> set(o0, getMachine().getBus().readData()));
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_READ_HALF)
                            .writeAddress(get(o1));
                }
                case LB, LBU -> {
                    next(() -> set(o0, getMachine().getBus().readData()));
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_READ_BYTE)
                            .writeAddress(get(o1));
                }
                case SW -> {
                    next(() -> {
                    });
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_WRITE_WORD)
                            .writeData(get(o0))
                            .writeAddress(get(o1));
                }
                case SH -> {
                    next(() -> {
                    });
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_WRITE_HALF)
                            .writeData(get(o0))
                            .writeAddress(get(o1));
                }
                case SB -> {
                    next(() -> {
                    });
                    getMachine().getBus()
                            .writeControl(CTRL_MEM_WRITE_BYTE)
                            .writeData(get(o0))
                            .writeAddress(get(o1));
                }

                case BEQ -> {
                    if (get(o0) == get(o1))
                        mProgramCounter = o2;
                }
                case BNE -> {
                    if (get(o0) != get(o1))
                        mProgramCounter = o2;
                }
                case BLT, BLTU -> {
                    if (get(o0) < get(o1))
                        mProgramCounter = o2;
                }
                case BGE, BGEU -> {
                    if (get(o0) >= get(o1))
                        mProgramCounter = o2;
                }
                case JAL -> {
                    set(o0, mProgramCounter);
                    mProgramCounter = o1;
                }
                case JALR -> {
                    set(o0, mProgramCounter);
                    mProgramCounter = o2 + get(o1);
                }

                case SLT, SLTU -> set(o0, get(o1) < get(o2) ? 1 : 0);
                case SLTI, SLTIU -> set(o0, get(o1) < o2 ? 1 : 0);

                case SYS -> sys(o0);

                case NULL -> throw new NullException();
                default -> throw new IllegalStateException("Unexpected value: " + inst);
            }
        }

        if (mNext == null)
            getMachine().getBus().writeControl(CTRL_MEM_READ_WORD).writeAddress(nextPC());
    }

    public void set(int i, int word) {
        if (i == 0) return; // Zero hardwired to NULL
        this.mRegisters[i] = word;
    }

    public int get(int i) {
        if (i == 0) return 0; // Zero hardwired to NULL
        return this.mRegisters[i];
    }

    private void backupBus() {
        mBusBackup[0] = getMachine().getBus().readControl();
        mBusBackup[1] = getMachine().getBus().readAddress();
        mBusBackup[2] = getMachine().getBus().readData();
    }

    private void restoreBus() {
        getMachine().getBus()
                .writeControl(mBusBackup[0])
                .writeAddress(mBusBackup[1])
                .writeData(mBusBackup[2]);
    }

    private void next(Runnable next) {
        backupBus();
        mNext = next;
    }

    private void sys(int func) {
        switch (func) {
            case 0x01 -> {
                // a0: fmt
                // a1: argc
                // a*: argv
                final var fmt = getMachine().getMemory().getString(get(A0.ordinal()));
                final var argc = get(A1.ordinal());
                final var argv = new Object[argc];
                for (int i = 0; i < argv.length; i++)
                    argv[i] = get(A2.ordinal() + i);
                System.out.printf(fmt, argv);
            }
        }
    }

    private int nextPC() {
        final var pc = mProgramCounter;
        mProgramCounter += Integer.BYTES;
        return pc;
    }
}
