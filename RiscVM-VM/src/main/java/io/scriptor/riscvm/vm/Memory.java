package io.scriptor.riscvm.vm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Memory extends VMComponent {

    private final ByteBuffer mData;

    public Memory(Machine machine, int size) {
        super(machine);
        this.mData = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
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

    public void setDWord(int address, long data) {
        mData.putLong(address, data);
    }

    public long getDWord(int address) {
        return mData.getLong(address);
    }

    public String getASCII(int address, int count) {
        final var builder = new StringBuilder();

        mData.position(address);
        for (int i = 0; i < count; i++)
            builder.append((char) mData.get());

        return builder.toString();
    }

    @Override
    public void reset() {
        mData.position(0);
        while (mData.hasRemaining())
            mData.put((byte) 0);
        mData.clear();
    }

    @Override
    public void cycle() {
    }
}
