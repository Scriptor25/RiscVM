package io.scriptor.riscvm.asm;

import io.scriptor.riscvm.ISA;
import io.scriptor.riscvm.Instruction;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static io.scriptor.riscvm.ISA.*;
import static io.scriptor.riscvm.Util.handle;
import static io.scriptor.riscvm.Util.handleT;
import static io.scriptor.riscvm.asm.Token.Type.*;

public class Assembler {

    public static void assemble(InputStream stream, VMConfig config, ByteBuffer buffer) {
        final var asm = new Assembler(stream, buffer.capacity());

        asm.next();
        do {
            asm.nextLine();
        } while (asm.notEOF());

        for (final var name : config.sections()) {
            final var section = asm.mSections.computeIfAbsent(name, key -> new Section(key, asm.mMemorySize));
            asm.mSectionsOrder.add(section);
        }

        for (final var section : asm.mSections.values())
            if (!asm.mSectionsOrder.contains(section))
                asm.mSectionsOrder.add(section);

        for (final var section : asm.mSectionsOrder)
            insertSection(buffer, section, asm.mSymbolTable, asm.mSectionsOrder);
    }

    private static void insertSection(ByteBuffer buffer, Section section, Map<String, Symbol> symbolTable, List<Section> sectionsOrder) {
        int start = offsetOf(sectionsOrder, section);

        buffer.position(start);
        section.put(buffer);

        for (final var entry : section.usage.entrySet()) {
            final var symbol = entry.getKey();
            final var offsets = entry.getValue();

            if (!symbolTable.containsKey(symbol))
                throw new IllegalStateException(String.format("undefined symbol '%s'", symbol));

            final var sym = symbolTable.get(symbol);
            final var location = sym.value() + offsetOf(sectionsOrder, sym.section());

            for (final var o : offsets) {
                final var inst = buffer.getInt(start + o);

                final var itype = ISA.values()[Instruction.getOpcode(inst)].itype;
                var i = switch (itype) {
                    case I -> Instruction.fromI(inst);
                    case S -> Instruction.fromS(inst);
                    case U -> Instruction.fromU(inst);
                    default -> throw new IllegalStateException("Unexpected value: " + itype);
                };

                i = switch (itype) {
                    case I -> Instruction.fromI(i.opcode, i.rd, i.rs1, location);
                    case S -> Instruction.fromS(i.opcode, i.rs1, i.rs2, location);
                    case U -> Instruction.fromU(i.opcode, i.rd, location);
                    default -> throw new IllegalStateException("Unexpected value: " + itype);
                };

                buffer.putInt(start + o, i.pack());
            }
        }
    }

    private static int offsetOf(List<Section> sectionsOrder, Section section) {
        int offset = 0;
        for (final var s : sectionsOrder) {
            if (s == section)
                return offset;
            offset += s.counter();
        }
        return offset;
    }

    private static boolean isDigit(int c) {
        return (0x30 <= c && c <= 0x39);
    }

    private static boolean isXDigit(int c) {
        return isDigit(c) || (0x41 <= c && c <= 0x46) || (0x61 <= c && c <= 0x66);
    }

    private static boolean isAlpha(int c) {
        return (0x41 <= c && c <= 0x5A) || (0x61 <= c && c <= 0x7A);
    }

    private static boolean isAlNum(int c) {
        return isDigit(c) || isAlpha(c);
    }

    private final InputStream mStream;
    private final int mMemorySize;
    private Token mToken;

    public final Map<String, Symbol> mSymbolTable = new HashMap<>();
    private final Map<String, Section> mSections = new HashMap<>();
    private final List<Section> mSectionsOrder = new Vector<>();
    private String mSelected = "";

    private Assembler(InputStream stream, int memorySize) {
        mStream = stream;
        mMemorySize = memorySize;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        for (final var entry : mSections.entrySet())
            builder.append(entry.getValue()).append('\n');

        builder.append("---------- Symbols ----------\n");
        for (final var entry : mSymbolTable.entrySet())
            builder.append(entry.getKey()).append(": ").append(entry.getValue().section().name).append("+").append(String.format("%08X", entry.getValue().value())).append('\n');
        builder.append("-----------------------------");

        return builder.toString();
    }

    private int read() {
        return handleT(mStream::read);
    }

    private void mark() {
        mStream.mark(1);
    }

    private void reset() {
        handle(mStream::reset);
    }

    private int escape(int c) {
        return switch (c) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            default -> c;
        };
    }

    private Token next() {
        var c = read();

        while (0x00 <= c && c <= 0x20)
            c = read();

        if (c < 0x00)
            return mToken = new Token();

        if (c == '#') {
            while (0x00 <= c && c != '\n')
                c = read();
            return next();
        }

        if (c == '.') {
            final var next = next();
            if (next.type() != SYMBOL)
                throw new IllegalStateException();
            return mToken = new Token(DIRECTIVE, next.value());
        }

        if (c == '-') {
            final var next = next();
            if (next.type() != IMMEDIATE)
                throw new IllegalStateException();
            return mToken = new Token(IMMEDIATE, -Integer.parseInt(next.value()));
        }

        if (c == '"') {
            final var str = new StringBuilder();
            while (true) {
                c = read();
                if (c < 0x00 || c == '"')
                    break;

                if (c == '\\')
                    c = escape(read());

                str.append((char) c);
            }
            return mToken = new Token(STRING, str.toString());
        }

        if (c == '\'') {
            int chr = read();
            if (chr == '\\')
                chr = escape(read());
            read();

            return mToken = new Token(IMMEDIATE, Integer.toString(chr));
        }

        if (c == '0') {
            mark();
            final var x = read();
            if (x != 'x' && x != 'X')
                reset();
            else {
                final var hex = new StringBuilder();
                while (true) {
                    mark();
                    c = read();
                    if (!isXDigit(c))
                        break;
                    hex.append((char) c);
                }
                reset();
                return mToken = new Token(IMMEDIATE, Integer.valueOf(hex.toString(), 16).toString());
            }
        }

        if (isDigit(c)) {
            final var integer = new StringBuilder().append((char) c);
            while (true) {
                mark();
                c = read();
                if (!isDigit(c))
                    break;
                integer.append((char) c);
            }
            reset();
            return mToken = new Token(IMMEDIATE, integer.toString());
        }

        if (isAlpha(c) || c == '_') {
            final var symbol = new StringBuilder().append((char) c);
            while (true) {
                mark();
                c = read();
                if (!isAlNum(c) && c != '_')
                    break;
                symbol.append((char) c);
            }
            reset();

            final var sym = symbol.toString();

            if (sym.matches("\\b(x(\\d+))\\b"))
                return mToken = new Token(REGISTER, symbol.substring(1));

            for (final var alias : ISA.RegisterAlias.values())
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
        String symbol;
        if (at(SYMBOL)) {
            symbol = getAndNext().value();
            if (nextIfAt(":"))
                nextSymbol(symbol);
            else {
                nextInstruction(symbol);
                return;
            }
        }

        if (at(DIRECTIVE)) nextDirective();
        else nextInstruction(expectAndNext(SYMBOL).value());
    }

    private Section section() {
        return mSections.computeIfAbsent(mSelected, key -> new Section(key, mMemorySize));
    }

    private void nextSymbol(String symbol) {
        mSymbolTable.put(symbol, new Symbol(section(), section().counter()));
    }

    private void nextInstruction(String symbol) {
        final var inst = ISA.valueOf(symbol.toUpperCase());

        if (nextPseudoInstruction(inst))
            return;

        final var ops = new Operand[inst.operands.length];
        for (int i = 0; i < ops.length; i++) {
            ops[i] = nextOperand(i > 0);
            if (ops[i] instanceof OpSymbol) {
                section().use(ops[i].asSym());
                ops[i] = new OpImmediate(0);
            }
        }

        final var instruction = inst.toInstruction(ops);
        section().putInt(instruction.pack());
    }

    private boolean nextPseudoInstruction(ISA instruction) {
        switch (instruction) {
            case LI -> {
                final var rd = nextOperand(false);
                final var imm = nextOperand(true);
                section().putInt(ADDI.toInstruction(rd, new OpRegister(RegisterAlias.ZERO), imm).pack());
                return true;
            }
            case LA -> {
                final var rd = nextOperand(false);
                final var sym = nextOperand(true);
                section().use(sym.asSym());
                section().putInt(ADDI.toInstruction(rd, new OpRegister(RegisterAlias.ZERO), new OpImmediate(0)).pack());
                return true;
            }
            case MV -> {
                final var rd = nextOperand(false);
                final var rs1 = nextOperand(true);
                section().putInt(ADDI.toInstruction(rd, rs1, new OpImmediate(0)).pack());
                return true;
            }
            case BEQZ -> {
                final var rs1 = nextOperand(false);
                final var sym = nextOperand(true);
                section().use(sym.asSym());
                section().putInt(BEQ.toInstruction(rs1, new OpRegister(RegisterAlias.ZERO), new OpImmediate(0)).pack());
                return true;
            }
            case BNEZ -> {
                final var rs1 = nextOperand(false);
                final var imm = nextOperand(true);
                section().putInt(BNE.toInstruction(rs1, new OpRegister(RegisterAlias.ZERO), imm).pack());
                return true;
            }
            case JR -> {
                final var sym = nextOperand(false);
                section().use(sym.asSym());
                section().putInt(JAL.toInstruction(new OpRegister(RegisterAlias.RA), new OpImmediate(0)).pack());
                return true;
            }
            case J -> {
                final var sym = nextOperand(false);
                section().use(sym.asSym());
                section().putInt(JAL.toInstruction(new OpRegister(RegisterAlias.ZERO), new OpImmediate(0)).pack());
                return true;
            }
            case RET -> {
                section().putInt(JALR.toInstruction(new OpRegister(RegisterAlias.ZERO), new OpRegister(RegisterAlias.RA), new OpImmediate(0)).pack());
                return true;
            }
            case SEQZ, SNEZ, SLTZ, SGTZ -> throw new IllegalStateException();
            case NOP -> {
                section().putInt(ADDI.toInstruction(new OpRegister(RegisterAlias.ZERO), new OpRegister(RegisterAlias.ZERO), new OpImmediate(0)).pack());
                return true;
            }
            case PUSH -> {
                final var rs1 = nextOperand(false);
                section().putInt(SW.toInstruction(rs1, new OpRegister(RegisterAlias.SP), new OpImmediate(0)).pack());
                section().putInt(SUBI.toInstruction(new OpRegister(RegisterAlias.SP), new OpRegister(RegisterAlias.SP), new OpImmediate(4)).pack());
                return true;
            }
            case POP -> {
                final var rd = nextOperand(false);
                section().putInt(ADDI.toInstruction(new OpRegister(RegisterAlias.SP), new OpRegister(RegisterAlias.SP), new OpImmediate(4)).pack());
                section().putInt(LW.toInstruction(rd, new OpRegister(RegisterAlias.SP), new OpImmediate(0)).pack());
                return true;
            }
        }
        return false;
    }

    private void nextDirective() {
        final var directive = expectAndNext(DIRECTIVE).value().toLowerCase();

        switch (directive) {
            case "section" -> mSelected = expectAndNext(DIRECTIVE).value();
            case "word" -> {
                do {
                    final var aWord = Integer.parseInt(expectAndNext(IMMEDIATE).value());
                    section().putInt(aWord);
                } while (nextIfAt(","));
            }
            case "half" -> {
                do {
                    final var aHalf = Short.parseShort(expectAndNext(IMMEDIATE).value());
                    section().putShort(aHalf);
                } while (nextIfAt(","));
            }
            case "byte" -> {
                do {
                    final var aByte = Byte.parseByte(expectAndNext(IMMEDIATE).value());
                    section().putByte(aByte);
                } while (nextIfAt(","));
            }
            case "string", "ascii", "asciz" -> {
                final var str = expectAndNext(STRING).value();

                for (int i = 0; i < str.length(); i++)
                    section().putByte((byte) str.charAt(i));
            }
            case "skip" -> {
                final var offset = Integer.parseInt(expectAndNext(IMMEDIATE).value());
                for (int i = 0; i < offset; i++)
                    section().putByte((byte) 0);
            }
            case "set" -> {
                final var symbol = expectAndNext(SYMBOL).value();
                expectAndNext(",");
                final var value = Integer.parseInt(expectAndNext(IMMEDIATE).value());
                mSymbolTable.computeIfAbsent(symbol, key -> new Symbol(section(), 0)).value(value);
            }
            default -> throw new IllegalStateException(String.format("undefined directive '%s'", directive));
        }
    }

    private Operand nextOperand(boolean comma) {
        if (comma) expectAndNext(",");

        if (at(SYMBOL)) {
            final var symbol = getAndNext().value();
            return new OpSymbol(symbol);
        }

        if (at(IMMEDIATE)) {
            final var integer = Integer.parseInt(getAndNext().value());
            return new OpImmediate(integer);
        }

        if (at(REGISTER)) {
            final var register = Integer.parseInt(getAndNext().value());
            return new OpRegister(register);
        }

        throw new IllegalStateException(String.format("undefined operand type '%s'", mToken));
    }
}
