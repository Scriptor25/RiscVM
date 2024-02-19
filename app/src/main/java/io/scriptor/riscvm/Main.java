package io.scriptor.riscvm;

import io.scriptor.riscvm.app.RiscApp;
import io.scriptor.riscvm.asm.VMConfig;

import static imgui.app.Application.launch;
import static io.scriptor.riscvm.Util.kb;

public class Main {

    public static void main(String[] args) {
        final var config = new VMConfig(kb(16), "text", "rodata", "data", "bss");
        final var vm = new RiscVM(config);
        launch(new RiscApp(vm));
    }
}
