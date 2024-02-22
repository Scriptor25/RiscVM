package io.scriptor.riscvm.core;

public class OpImmediate extends Operand {

    public final int immediate;

    public OpImmediate(int immediate) {
        this.immediate = immediate;
    }

    @Override
    public int asImm() {
        return immediate;
    }
}
