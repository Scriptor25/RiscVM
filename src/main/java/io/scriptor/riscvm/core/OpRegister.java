package io.scriptor.riscvm.core;

public class OpRegister extends Operand {

    public final int register;

    public OpRegister(int register) {
        this.register = register;
    }

    public OpRegister(ISA.RegisterAlias register) {
        this.register = register.ordinal();
    }

    @Override
    public int asReg() {
        return register;
    }
}
