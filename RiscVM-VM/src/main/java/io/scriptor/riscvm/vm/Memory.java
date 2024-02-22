package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.core.Instruction;
import io.scriptor.riscvm.core.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Memory extends VMComponent {

    private static final int N = 16;

    private final ByteBuffer mData;

    public Memory(Machine machine, int size) {
        super(machine);
        this.mData = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder()
                .append("Size: ").append(Util.unit(mData.capacity()));

        for (int i = 0; i < mData.capacity(); i += N) {
            builder.append(String.format("%n%08X: ", i));
            for (int j = 0; j < N; j++)
                builder.append(String.format("%02X ", mData.get(i + j)));

            builder.append('|');
            for (int j = 0; j < N; j++) {
                final var c = mData.get(i + j);
                builder.append(String.format("%c", 0x20 <= c && c <= 0x7E ? c : '.'));
            }
            builder.append('|');

            builder.append("| ");
            for (int j = 0; j < N; j += 4) {
                final var c = mData.getInt(i + j);
                builder.append(String.format("%16s ", Instruction.valueOf(c)));
            }
            builder.append('|');

            int same = 0;
            while (checkSegments(i, i + (1 + same) * N))
                same++;

            if (same > 0) {
                i += same * N;
                builder.append(String.format("%n.%08X", i + N - 1));
            }
        }

        return builder.toString();
    }

    private boolean checkSegments(int segment0, int segment1) {
        if (segment0 < 0 || segment1 < 0 || segment0 + N - 1 >= mData.capacity() || segment1 + N - 1 >= mData.capacity())
            return false;
        for (int i = 0; i < N; i++)
            if (mData.get(segment0 + i) != mData.get(segment1 + i))
                return false;
        return true;
    }

    public void reset() {
        mData.position(0);
        while (mData.hasRemaining())
            mData.put((byte) 0);
        mData.clear();
    }

    public ByteBuffer getBuffer() {
        return mData;
    }

    public void setByte(int address, byte data) {
        mData.put(address, data);
    }

    public byte getByte(int address) {
        return mData.get(address);
    }

    public void setHalf(int address, short data) {
        mData.putShort(address, data);
    }

    public short getHalf(int address) {
        return mData.getShort(address);
    }

    public void setWord(int address, int data) {
        mData.putInt(address, data);
    }

    public int getWord(int address) {
        return mData.getInt(address);
    }

    public String getASCII(int address, int count) {
        final var builder = new StringBuilder();

        mData.position(address);
        for (int i = 0; i < count; i++)
            builder.append((char) mData.get());

        return builder.toString();
    }
}
