package io.scriptor.riscvm.asm;

public record Token(Type type, String value) {

    public enum Type {
        EOF,
        SYMBOL,
        REGISTER,
        DIRECTIVE,
        IMMEDIATE,
        STRING,
        OTHER,
    }

    public Token() {
        this(Type.EOF, null);
    }

    public Token(Type type, int c) {
        this(type, Character.toString(c));
    }
}
