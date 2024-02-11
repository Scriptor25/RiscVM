package io.scriptor.riscvm.asm;

import io.scriptor.riscvm.RV32IM;

import java.io.InputStream;

import static io.scriptor.riscvm.ErrorUtil.handle;
import static io.scriptor.riscvm.ErrorUtil.handleT;
import static io.scriptor.riscvm.asm.Token.Type.*;

public class Assembler {

    private static boolean isdigit(int c) {
        return (0x30 <= c && c <= 0x39);
    }

    private static boolean isxdigit(int c) {
        return isdigit(c) || (0x41 <= c && c <= 0x46) || (0x61 <= c && c <= 0x66);
    }

    private static boolean isalpha(int c) {
        return (0x41 <= c && c <= 0x5A) || (0x61 <= c && c <= 0x7A);
    }

    private static boolean isalnum(int c) {
        return isdigit(c) || isalpha(c);
    }

    private final InputStream mStream;
    private Token mToken;

    public Assembler(InputStream stream) {
        this.mStream = stream;

        next();
        do {
            nextLine();
        } while (notEOF());

        handle(this.mStream::close);
    }

    private int read() {
        return handleT(this.mStream::read);
    }

    private void mark(int limit) {
        this.mStream.mark(limit);
    }

    private void reset() {
        handle(this.mStream::reset);
    }

    private Token next() {
        var c = read();

        while (0x00 <= c && c <= 0x20)
            c = read();

        if (c < 0x00)
            return this.mToken = new Token();

        if (c == ';') {
            while (0x00 <= c && c != '\n')
                c = read();
            return next();
        }

        if (c == '"') {
            final var str = new StringBuilder();
            while (true) {
                c = read();
                if (c < 0x00 || c == '"')
                    break;
                str.append((char) c);
            }
            return this.mToken = new Token(STRING, str.toString());
        }

        if (c == '.') {
            final var next = next();
            if (next.type() != SYMBOL)
                throw new IllegalStateException();
            return this.mToken = new Token(DIRECTIVE, next.value());
        }

        if (c == '0') {
            mark(1);
            final var x = read();
            if (x != 'x' && x != 'X')
                reset();
            else {
                final var hex = new StringBuilder();
                while (true) {
                    mark(1);
                    c = read();
                    if (!isxdigit(c))
                        break;
                    hex.append((char) c);
                }
                reset();
                return this.mToken = new Token(INTEGER, Integer.valueOf(hex.toString(), 16).toString());
            }
        }

        if (isdigit(c)) {
            final var integer = new StringBuilder().append((char) c);
            while (true) {
                mark(1);
                c = read();
                if (!isdigit(c))
                    break;
                integer.append((char) c);
            }
            reset();
            return this.mToken = new Token(INTEGER, integer.toString());
        }

        if (isalpha(c)) {
            final var symbol = new StringBuilder().append((char) c);
            while (true) {
                mark(1);
                c = read();
                if (!isalnum(c))
                    break;
                symbol.append((char) c);
            }
            reset();

            final var sym = symbol.toString();

            if (sym.matches("\\b(x(\\d+))\\b"))
                return this.mToken = new Token(REGISTER, symbol.substring(1));

            for (final var alias : RV32IM.RegisterAlias.values())
                if (sym.equalsIgnoreCase(alias.name()))
                    return this.mToken = new Token(REGISTER, Integer.toString(alias.ordinal()));

            return this.mToken = new Token(SYMBOL, sym);
        }

        return this.mToken = new Token(OTHER, c);
    }

    private boolean notEOF() {
        return this.mToken == null || this.mToken.type() != EOF;
    }

    private Token getAndNext() {
        final var token = mToken;
        next();
        return token;
    }

    private boolean at(Token.Type type) {
        return this.mToken != null && notEOF() && this.mToken.type() == type;
    }

    private boolean at(String value) {
        return this.mToken != null && notEOF() && this.mToken.value().equals(value);
    }

    private boolean nextIfAt(String value) {
        if (at(value)) {
            next();
            return true;
        }
        return false;
    }

    private Token expect(Token.Type type) {
        if (at(type)) return this.mToken;
        throw new IllegalStateException();
    }

    private Token expect(String value) {
        if (at(value)) return this.mToken;
        throw new IllegalStateException();
    }

    private Token expectAndNext(Token.Type type) {
        final var token = expect(type);
        next();
        return token;
    }

    private Token expectAndNext(String value) {
        final var token = expect(value);
        next();
        return token;
    }

    private void nextLine() {
        String symbol = null;
        if (at(SYMBOL)) {
            symbol = getAndNext().value();
            if (nextIfAt(":"))
                nextSymbol(symbol);
            else {
                nextInstruction(symbol);
                return;
            }
        }

        if (at(DIRECTIVE)) nextDirective(symbol);
        else nextInstruction(expectAndNext(SYMBOL).value());
    }

    private void nextSymbol(String symbol) {
        System.out.printf("%s:%n", symbol);
    }

    private void nextInstruction(String symbol) {
        final var instruction = RV32IM.valueOf(symbol.toUpperCase());

        System.out.printf("%s ", instruction);
        for (int i = 0; i < instruction.operands.length; i++) {
            if (i > 0) {
                expectAndNext(",");
                System.out.print(", ");
            }
            nextOperand();
        }
        System.out.println();
    }

    private void nextDirective(String symbol) {
        final var directive = expectAndNext(DIRECTIVE).value().toLowerCase();

        switch (directive) {
            case "section" -> {
                final var section = expectAndNext(DIRECTIVE).value();
                System.out.printf(".section .%s%n", section);
            }
            case "string" -> {
                final var str = expectAndNext(STRING).value();
                System.out.printf(".string \"%s\"%n", str);
            }
            case "skip" -> {
                final var offset = expectAndNext(INTEGER).value();
                System.out.printf(".skip %s%n", offset);
            }
            default -> throw new IllegalStateException();
        }
    }

    private void nextOperand() {
        if (at(SYMBOL)) {
            final var symbol = getAndNext().value();
            System.out.printf("%s", symbol);
            return;
        }
        if (at(REGISTER)) {
            final var register = getAndNext().value();
            System.out.printf("x%s", register);
            return;
        }
        if (at(INTEGER)) {
            final var integer = getAndNext().value();
            System.out.printf("%s", integer);
            return;
        }
    }
}
