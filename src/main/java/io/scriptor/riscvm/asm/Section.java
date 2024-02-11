package io.scriptor.riscvm.asm;

import io.scriptor.riscvm.RV32IM;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Section {

    public final String name;
    public final Map<String, List<Integer>> symbolUsage = new HashMap<>();
    private final List<Integer> mData = new Vector<>();

    public Section(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append("---------- Section ----------\n");
        builder.append(name).append(": ").append(mData.size() * Integer.BYTES).append('\n');
        for (final var entry : symbolUsage.entrySet())
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');

        return builder.toString();
    }

    public int counter() {
        return mData.size() * Integer.BYTES;
    }

    public void put(ByteBuffer buffer) {
        for (final var i : mData)
            buffer.putInt(i);
    }

    public Section add(int i) {
        mData.add(i);
        return this;
    }

    public Section add(RV32IM i) {
        return add(i.ordinal());
    }

    public Section add(RV32IM.RegisterAlias i) {
        return add(i.ordinal());
    }

    public void addUsage(String symbol) {
        symbolUsage.computeIfAbsent(symbol, key -> new Vector<>()).add(mData.size() * Integer.BYTES);
        mData.add(0);
    }
}
