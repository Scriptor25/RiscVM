package io.scriptor.riscvm.asm;

public abstract class Operand {

    public int asReg() {
        throw new UnsupportedOperationException();
    }

    public int asImm() {
        throw new UnsupportedOperationException();
    }

    public String asSym() {
        throw new UnsupportedOperationException();
    }
}
