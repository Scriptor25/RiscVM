package io.scriptor.riscvm.vm;

public class ALU {

    private final CPU mCPU;

    public ALU(CPU cpu) {
        mCPU = cpu;
    }

    public void cycle() {
        switch (mCPU.opcode()) {
            case AND -> mCPU.setrd(mCPU.getrs1() & mCPU.getrs2());
            case OR -> mCPU.setrd(mCPU.getrs1() | mCPU.getrs2());
            case XOR -> mCPU.setrd(mCPU.getrs1() ^ mCPU.getrs2());
            case ANDI -> mCPU.setrd(mCPU.getrs1() & mCPU.imm());
            case ORI -> mCPU.setrd(mCPU.getrs1() | mCPU.imm());
            case XORI -> mCPU.setrd(mCPU.getrs1() ^ mCPU.imm());
            case SLL -> mCPU.setrd(mCPU.getrs1() << mCPU.getrs2());
            case SRL -> mCPU.setrd(mCPU.getrs1() >>> mCPU.getrs2());
            case SRA -> mCPU.setrd(mCPU.getrs1() >> mCPU.getrs2());
            case SLLI -> mCPU.setrd(mCPU.getrs1() << mCPU.imm());
            case SRLI -> mCPU.setrd(mCPU.getrs1() >>> mCPU.imm());
            case SRAI -> mCPU.setrd(mCPU.getrs1() >> mCPU.imm());
            case ADD -> mCPU.setrd(mCPU.getrs1() + mCPU.getrs2());
            case SUB -> mCPU.setrd(mCPU.getrs1() - mCPU.getrs2());
            case ADDI -> mCPU.setrd(mCPU.getrs1() + mCPU.imm());
            case SUBI -> mCPU.setrd(mCPU.getrs1() - mCPU.imm());
            case MUL -> mCPU.setrd(mCPU.getrs1() * mCPU.getrs2());
            case DIV -> mCPU.setrd(mCPU.getrs1() / mCPU.getrs2());
            case REM -> mCPU.setrd(mCPU.getrs1() % mCPU.getrs2());

            case LW -> mCPU.setrd(mCPU.getMachine().getMemory().getWord(mCPU.getrs1() + mCPU.imm()));
            case LH -> mCPU.setrd(mCPU.getMachine().getMemory().getHalf(mCPU.getrs1() + mCPU.imm()));
            case LB -> mCPU.setrd(mCPU.getMachine().getMemory().getByte(mCPU.getrs1() + mCPU.imm()));
            case SW -> mCPU.getMachine().getMemory().setWord(mCPU.getrs2() + mCPU.imm(), mCPU.getrs1());
            case SH -> mCPU.getMachine().getMemory().setHalf(mCPU.getrs2() + mCPU.imm(), (short) mCPU.getrs1());
            case SB -> mCPU.getMachine().getMemory().setByte(mCPU.getrs2() + mCPU.imm(), (byte) mCPU.getrs1());

            case BEQ -> {
                if (mCPU.getrs1() == mCPU.getrs2())
                    mCPU.programCounter(mCPU.imm());
            }
            case BNE -> {
                if (mCPU.getrs1() != mCPU.getrs2())
                    mCPU.programCounter(mCPU.imm());
            }
            case BLT -> {
                if (mCPU.getrs1() < mCPU.getrs2())
                    mCPU.programCounter(mCPU.imm());
            }
            case BGE -> {
                if (mCPU.getrs1() >= mCPU.getrs2())
                    mCPU.programCounter(mCPU.imm());
            }
            case JAL -> {
                mCPU.setrd(mCPU.programCounter());
                mCPU.programCounter(mCPU.imm());
            }
            case JALR -> {
                mCPU.setrd(mCPU.programCounter());
                mCPU.programCounter(mCPU.getrs1() + mCPU.imm());
            }

            case SLT -> mCPU.setrd(mCPU.getrs1() < mCPU.getrs2() ? 1 : 0);
            case SLTI -> mCPU.setrd(mCPU.getrs1() < mCPU.imm() ? 1 : 0);

            case ECALL -> mCPU.ecall();
            case EBREAK -> mCPU.ebreak();

            default -> throw new RuntimeException();
        }
    }
}
