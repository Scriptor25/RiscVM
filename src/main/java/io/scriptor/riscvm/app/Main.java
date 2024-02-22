package io.scriptor.riscvm.app;

import io.scriptor.riscvm.vm.RiscVM;
import io.scriptor.riscvm.vm.VMConfig;

import static imgui.app.Application.launch;
import static io.scriptor.riscvm.core.Util.kb;

public class Main {

    public static void main(String[] args) {
        final var config = new VMConfig(kb(16), "text", "rodata", "data", "bss");
        final var vm = new RiscVM(config);
        launch(new RiscApp(vm));
    }
}
