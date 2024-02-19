package io.scriptor.riscvm.asm;

import io.scriptor.riscvm.ISA;

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
