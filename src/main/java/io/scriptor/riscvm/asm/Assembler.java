package io.scriptor.riscvm.asm;

import io.scriptor.riscvm.RV32IM;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static io.scriptor.riscvm.ErrorUtil.handle;
import static io.scriptor.riscvm.ErrorUtil.handleT;
import static io.scriptor.riscvm.RV32IM.*;
import static io.scriptor.riscvm.RV32IM.RegisterAlias.*;
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

    public final Map<String, Symbol> mSymbolTable = new HashMap<>();
    private final Map<String, Section> mSections = new HashMap<>();
    private final List<Section> mOrder = new Vector<>();
    private String mSelected = "";

    public Assembler(InputStream stream, ASMConfig config, ByteBuffer buffer) {
        mStream = stream;

        next();
        do {
            nextLine();
        } while (notEOF());
        handle(mStream::close);

        for (final var name : config.order()) {
            final var section = mSections.computeIfAbsent(name, Section::new);
            mOrder.add(section);
        }

        for (final var section : mOrder)
            insertSection(buffer, section);
    }

    private int offsetOf(Section section) {
        int offset = 0;
        for (final var s : mOrder) {
            if (s == section)
                return offset;
            offset += s.counter();
        }
        return offset;
    }

    private void insertSection(ByteBuffer buffer, Section section) {
        int start = offsetOf(section);

        buffer.position(start);
        section.put(buffer);

        for (final var entry : section.symbolUsage.entrySet()) {
            final var symbol = entry.getKey();
            final var offsets = entry.getValue();

            if (!mSymbolTable.containsKey(symbol))
                throw new IllegalStateException(String.format("undefined symbol '%s'", symbol));

            final var sym = mSymbolTable.get(symbol);
            final var location = sym.offset() + offsetOf(sym.section());

            for (final var o : offsets)
                buffer.putInt(start + o, location);
        }
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        for (final var entry : mSections.entrySet())
            builder.append(entry.getValue()).append('\n');

        builder.append("---------- Symbols ----------\n");
        for (final var entry : mSymbolTable.entrySet())
            builder.append(entry.getKey()).append(": ").append(entry.getValue().section().name).append("+").append(String.format("%08X", entry.getValue().offset())).append('\n');

        return builder.toString();
    }

    private Section section() {
        return mSections.computeIfAbsent(mSelected, Section::new);
    }

    private int read() {
        return handleT(mStream::read);
    }

    private void mark(int limit) {
        mStream.mark(limit);
    }

    private void reset() {
        handle(mStream::reset);
    }

    private Token next() {
        var c = read();

        while (0x00 <= c && c <= 0x20)
            c = read();

        if (c < 0x00)
            return mToken = new Token();

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
            return mToken = new Token(STRING, str.toString());
        }

        if (c == '.') {
            final var next = next();
            if (next.type() != SYMBOL)
                throw new IllegalStateException();
            return mToken = new Token(DIRECTIVE, next.value());
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
                return mToken = new Token(INTEGER, Integer.valueOf(hex.toString(), 16).toString());
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
            return mToken = new Token(INTEGER, integer.toString());
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
                return mToken = new Token(REGISTER, symbol.substring(1));

            for (final var alias : RV32IM.RegisterAlias.values())
                if (sym.equalsIgnoreCase(alias.name()))
                    return mToken = new Token(REGISTER, Integer.toString(alias.ordinal()));

            return mToken = new Token(SYMBOL, sym);
        }

        return mToken = new Token(OTHER, c);
    }

    private boolean notEOF() {
        return mToken == null || mToken.type() != EOF;
    }

    private Token getAndNext() {
        final var token = mToken;
        next();
        return token;
    }

    private boolean at(Token.Type type) {
        return mToken != null && notEOF() && mToken.type() == type;
    }

    private boolean at(String value) {
        return mToken != null && notEOF() && mToken.value().equals(value);
    }

    private boolean nextIfAt(String value) {
        if (at(value)) {
            next();
            return true;
        }
        return false;
    }

    private Token expect(Token.Type type) {
        if (at(type)) return mToken;
        throw new IllegalStateException(String.format("expected type '%s' but got '%s'", type, mToken));
    }

    private Token expect(String value) {
        if (at(value)) return mToken;
        throw new IllegalStateException(String.format("expected value '%s' but got '%s'", value, mToken));
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
        mSymbolTable.put(symbol, new Symbol(section(), section().counter()));
    }

    private void nextInstruction(String symbol) {
        final var instruction = RV32IM.valueOf(symbol.toUpperCase());

        if (instruction.pseudo) {
            nextPseudoInstruction(instruction);
            return;
        }

        section().add(instruction);

        int i = 0;
        for (; i < instruction.operands.length; i++) nextOperand(i > 0);
        for (; i < 3; i++) section().add(0);
    }

    private void nextPseudoInstruction(RV32IM instruction) {
        switch (instruction) {
            case MV -> {
                section().add(ADDI);
                nextOperand(false);
                nextOperand(true);
                section().add(0);
            }
            case LI, LA -> {
                section().add(ADDI);
                nextOperand(false);
                section().add(ZERO);
                nextOperand(true);
            }
            case BEQZ -> {
                section().add(BEQ);
                nextOperand(false);
                section().add(ZERO);
                nextOperand(true);
            }
            case BNEZ -> {
                section().add(BNE);
                nextOperand(false);
                section().add(ZERO);
                nextOperand(true);
            }
            case J -> {
                section().add(JAL);
                section().add(ZERO);
                nextOperand(false);
                section().add(0);
            }
            case JR -> {
                section().add(JAL);
                section().add(RA);
                nextOperand(false);
                section().add(0);
            }
            case RET -> {
                section().add(JALR);
                section().add(ZERO);
                section().add(RA);
                section().add(0);
            }
            case SEQZ -> {
                section().add(SLTIU);
                nextOperand(false);
                nextOperand(true);
                section().add(1);
            }
            case SNEZ -> {
                section().add(SLTU);
                nextOperand(false);
                section().add(ZERO);
                nextOperand(true);
            }
            case SLTZ -> {
                section().add(SLT);
                nextOperand(false);
                nextOperand(true);
                section().add(ZERO);
            }
            case SGTZ -> {
                section().add(SLT);
                nextOperand(false);
                section().add(ZERO);
                nextOperand(true);
            }
            case NOP -> {
                section().add(ADDI);
                section().add(ZERO);
                section().add(ZERO);
                section().add(0);
            }
            case PUSH -> {
                section().add(SW);
                nextOperand(false);
                section().add(SP);
                section().add(0);

                section().add(SUBI);
                section().add(SP);
                section().add(SP);
                section().add(4);
            }
            case POP -> {
                section().add(ADDI);
                section().add(SP);
                section().add(SP);
                section().add(4);

                section().add(LW);
                nextOperand(false);
                section().add(SP);
                section().add(0);
            }
        }
    }

    private void nextDirective(String symbol) {
        final var directive = expectAndNext(DIRECTIVE).value().toLowerCase();

        switch (directive) {
            case "section" -> mSelected = expectAndNext(DIRECTIVE).value();
            case "word" -> {
                final var word = Integer.parseInt(expectAndNext(INTEGER).value());
                section().add(word);
            }
            case "string" -> {
                final var str = expectAndNext(STRING).value();

                final var buffer = ByteBuffer.allocateDirect(((str.length() / 4) + 1) * 4).order(ByteOrder.nativeOrder());
                int i = 0;
                for (; i < str.length(); i++)
                    buffer.put((byte) str.charAt(i));
                for (; i < buffer.capacity(); i++)
                    buffer.put((byte) '\00');

                buffer.position(0);
                while (buffer.hasRemaining())
                    section().add(buffer.getInt());
            }
            case "skip" -> {
                final var offset = Integer.parseInt(expectAndNext(INTEGER).value());
                for (int i = 0; i < offset; i += 4)
                    section().add(0);
            }
            default -> throw new IllegalStateException(String.format("undefined directive '%s'", directive));
        }
    }

    private void nextOperand(boolean comma) {
        if (comma) expectAndNext(",");

        if (at(SYMBOL)) {
            final var symbol = getAndNext().value();
            section().addUsage(symbol);
        } else if (at(REGISTER) || at(INTEGER)) {
            final var value = Integer.parseInt(getAndNext().value());
            section().add(value);
        } else
            throw new IllegalStateException(String.format("undefined operand type '%s'", mToken));
    }
}
