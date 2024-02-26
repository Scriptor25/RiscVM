package io.scriptor.riscvm.vm;

public abstract class VMComponent {

    private final Machine mMachine;

    protected VMComponent(Machine machine) {
        this.mMachine = machine;
    }

    public Machine getMachine() {
        return this.mMachine;
    }

    public abstract void reset();

    public abstract void cycle();
}
