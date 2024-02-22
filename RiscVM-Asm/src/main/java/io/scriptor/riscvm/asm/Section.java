package io.scriptor.riscvm.asm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Section {

    public final String name;
    public final Map<String, List<Integer>> usage = new HashMap<>();
    private final ByteBuffer mData;

    public Section(String name, int size) {
        this.name = name;
        this.mData = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append("---------- Section ----------\n");
        builder.append(name).append(": ").append(counter());
        for (final var entry : usage.entrySet())
            builder.append('\n').append(entry.getKey()).append(": ").append(entry.getValue());

        return builder.toString();
    }

    public int counter() {
        return mData.position();
    }

    public void put(ByteBuffer buffer) {
        mData.limit(mData.position());
        mData.position(0);
        while (mData.hasRemaining())
            buffer.put(mData.get());
    }

    public Section putInt(int i) {
        mData.putInt(i);
        return this;
    }

    public Section putShort(short s) {
        mData.putShort(s);
        return this;
    }

    public Section putByte(byte b) {
        mData.put(b);
        return this;
    }

    public void use(String symbol) {
        usage.computeIfAbsent(symbol, key -> new Vector<>()).add(counter());
    }
}
