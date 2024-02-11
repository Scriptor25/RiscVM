package io.scriptor.riscvm.vm;


public class SystemBus extends VMComponent {

    public static final int CTRL_NOP = 0x00;
    public static final int CTRL_MEM_READ_WORD = 0x10;
    public static final int CTRL_MEM_WRITE_WORD = 0x11;
    public static final int CTRL_MEM_READ_HALF = 0x12;
    public static final int CTRL_MEM_WRITE_HALF = 0x13;
    public static final int CTRL_MEM_READ_BYTE = 0x14;
    public static final int CTRL_MEM_WRITE_BYTE = 0x15;

    private int mControlBus;
    private int mAddressBus;
    private int mDataBus;

    public SystemBus(Machine machine) {
        super(machine);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(String.format("Control: %08X%n", mControlBus))
                .append(String.format("Address: %08X%n", mAddressBus))
                .append(String.format("   Data: %08X", mDataBus))
                .toString();
    }

    @Override
    public void cycle() {
        throw new UnsupportedOperationException("the system bus is a static component; its only purpose is to transfer data between other components");
    }

    public int readControl() {
        return this.mControlBus;
    }

    public int readAddress() {
        return this.mAddressBus;
    }

    public int readData() {
        return this.mDataBus;
    }

    public SystemBus writeControl(int word) {
        this.mControlBus = word;
        return this;
    }

    public SystemBus writeAddress(int word) {
        this.mAddressBus = word;
        return this;
    }

    public SystemBus writeData(int word) {
        this.mDataBus = word;
        return this;
    }
}
