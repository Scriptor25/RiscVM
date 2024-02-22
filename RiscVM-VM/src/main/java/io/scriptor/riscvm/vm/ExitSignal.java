package io.scriptor.riscvm.vm;

public class ExitSignal extends RuntimeException {

    public ExitSignal(int code) {
        super(String.format("Exit Code %d", code));
    }
}
