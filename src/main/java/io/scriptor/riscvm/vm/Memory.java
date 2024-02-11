package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.ByteUtil;
import io.scriptor.riscvm.RV32IM;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static io.scriptor.riscvm.vm.SystemBus.*;

public class Memory extends VMComponent {

    private final ByteBuffer mData;

    public Memory(Machine machine, int size) {
        super(machine);
        this.mData = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder()
                .append("Size: ").append(ByteUtil.unit(mData.capacity()));

        for (int i = 0; i < mData.capacity(); i += 8) {
            builder.append(String.format("%n%08X: ", i));
            for (int j = 0; j < 8; j++)
                builder.append(String.format("%02X ", mData.get(i + j)));

            builder.append('|');
            for (int j = 0; j < 8; j++) {
                final var c = mData.get(i + j);
                builder.append(String.format("%c", 0x20 <= c && c <= 0x7E ? c : '.'));
            }
            builder.append('|');

            int same = 0;
            while (checkSegments(i, i + (1 + same) * 8))
                same++;

            if (same > 0) {
                i += same * 8;
                builder.append(String.format("%n.%08X", i + 7));
            }
        }

        return builder.toString();
    }

    @Override
    public void cycle() {
        switch (getMachine().getBus().readControl()) {
            case CTRL_MEM_READ_WORD:
                getMachine().getBus().writeData(getWord(getMachine().getBus().readAddress()));
                break;
            case CTRL_MEM_WRITE_WORD:
                setWord(getMachine().getBus().readAddress(), getMachine().getBus().readData());
                break;
            case CTRL_MEM_READ_HALF:
                getMachine().getBus().writeData(getHalf(getMachine().getBus().readAddress()));
                break;
            case CTRL_MEM_WRITE_HALF:
                setHalf(getMachine().getBus().readAddress(), (short) getMachine().getBus().readData());
                break;
            case CTRL_MEM_READ_BYTE:
                getMachine().getBus().writeData(getByte(getMachine().getBus().readAddress()));
                break;
            case CTRL_MEM_WRITE_BYTE:
                setByte(getMachine().getBus().readAddress(), (byte) getMachine().getBus().readData());
                break;
        }
    }

    private boolean checkSegments(int segment0, int segment1) {
        if (segment0 < 0 || segment1 < 0 || segment0 + 7 >= mData.capacity() || segment1 + 7 >= mData.capacity())
            return false;
        for (int i = 0; i < 8; i++)
            if (mData.get(segment0 + i) != mData.get(segment1 + i))
                return false;
        return true;
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

    public void setInstruction(int address, int inst, int o0, int o1, int o2) {
        setWord(address, inst);
        setWord(address + 4, o0);
        setWord(address + 8, o1);
        setWord(address + 12, o2);
    }

    public void setInstruction(int address, RV32IM inst, int o0, int o1, int o2) {
        setInstruction(address, inst.ordinal(), o0, o1, o2);
    }

    public void setInstruction(int address, RV32IM inst, RV32IM.RegisterAlias r0, RV32IM.RegisterAlias r1, RV32IM.RegisterAlias r2) {
        setInstruction(address, inst.ordinal(), r0.ordinal(), r1.ordinal(), r2.ordinal());
    }

    public void setInstruction(int address, RV32IM inst) {
        setInstruction(address, inst.ordinal(), 0, 0, 0);
    }

    public void setInstruction(int address, RV32IM inst, int i0) {
        setInstruction(address, inst.ordinal(), i0, 0, 0);
    }

    public void setInstruction(int address, RV32IM inst, RV32IM.RegisterAlias r0, RV32IM.RegisterAlias r1, int i2) {
        setInstruction(address, inst.ordinal(), r0.ordinal(), r1.ordinal(), i2);
    }

    public void setInstruction(int address, RV32IM inst, RV32IM.RegisterAlias r0, int i1) {
        setInstruction(address, inst.ordinal(), r0.ordinal(), i1, 0);
    }

    public void setString(int address, String str) {
        for (int i = 0; i < str.length(); i++)
            mData.putChar(address + i, str.charAt(i));
        mData.putChar(address + str.length(), '\00');
    }

    public String getString(int address) {
        final var builder = new StringBuilder();
        while (true) {
            final var c = (char) getByte(address++);
            if (c == '\00')
                break;
            builder.append(c);
        }
        return builder.toString();
    }
}
