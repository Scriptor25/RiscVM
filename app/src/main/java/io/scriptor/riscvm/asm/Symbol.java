package io.scriptor.riscvm.asm;

public final class Symbol {
    private final Section mSection;
    private int mValue;

    public Symbol(Section section, int value) {
        mSection = section;
        mValue = value;
    }

    public Section section() {
        return mSection;
    }

    public int value() {
        return mValue;
    }

    public void value(int value) {
        mValue = value;
    }
}
