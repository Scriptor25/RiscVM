package io.scriptor.riscvm.vm;

import io.scriptor.riscvm.asm.Assembler;

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
        Assembler.assemble(stream, mMachine.getMemory().getBuffer(), mConfig.sections());
    }

    public boolean step() {
        try {
            mMachine.cycle();
            return true;
        } catch (ExitSignal e) {
            System.out.println(e.getMessage());
        } catch (BreakPoint b) {
            System.out.println("Reached Break Point");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        return false;
    }
}
