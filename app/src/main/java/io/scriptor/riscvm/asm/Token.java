package io.scriptor.riscvm.asm;

public class Token {

    public enum Type {
        EOF,
        SYMBOL,
        REGISTER,
        DIRECTIVE,
        IMMEDIATE,
        STRING,
        OTHER,
    }

    private final Type type;
    private final String value;

    public Token() {
        this(Type.EOF, null);
    }

    public Token(Type type, int c) {
        this(type, Character.toString((char) c));
    }

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type type() {
        return type;
    }

    public String value() {
        return value;
    }
}
