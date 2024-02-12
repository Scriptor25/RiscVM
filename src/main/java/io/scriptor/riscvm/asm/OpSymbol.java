package io.scriptor.riscvm.asm;

public class OpSymbol extends Operand {

    public final String symbol;

    public OpSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String asSym() {
        return symbol;
    }
}
