package io.scriptor.riscvm;

import io.scriptor.riscvm.asm.Assembler;
import io.scriptor.riscvm.asm.VMConfig;
import io.scriptor.riscvm.vm.ExitSignal;
import io.scriptor.riscvm.vm.Machine;

import java.io.BufferedInputStream;

public class RiscVM {

    private final VMConfig mConfig;
    private final Machine mMachine;

    public RiscVM(VMConfig config) {
        mConfig = config;
        mMachine = new Machine(config);
    }

    public Machine getMachine() {
        return mMachine;
    }

    public void resetCPU() {
        mMachine.getCPU().reset();
    }

    public void resetMemory() {
        mMachine.getMemory().reset();
    }

    public void reset() {
        mMachine.reset();
    }

    public void assemble(BufferedInputStream stream) {
        Assembler.assemble(stream, mConfig, mMachine.getMemory().getBuffer());
    }

    public boolean step() {
        try {
            mMachine.cycle();
            return true;
        } catch (ExitSignal e) {
            System.out.println(e.getMessage());
        } catch (Throwable t) {
            System.err.println(t);
        }
        return false;
    }
}
