package io.scriptor.riscvm;

import io.scriptor.riscvm.asm.Assembler;
import io.scriptor.riscvm.asm.LinkerConfig;
import io.scriptor.riscvm.vm.ExitSignal;
import io.scriptor.riscvm.vm.Machine;

import static io.scriptor.riscvm.Util.kb;

public class RiscVM {

    public static void main(String[] args) {
        final var config = new LinkerConfig(kb(16), "text", "rodata", "data", "bss");

        final var machine = new Machine(config);
        final var mem = machine.getMemory();

        final var asm = new Assembler(ClassLoader.getSystemResourceAsStream("fib.s"), config, mem.getBuffer());
        //System.out.println(asm);
        //System.out.println(mem);

        while (true) {
            try {
                machine.cycle();
            } catch (ExitSignal e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }
}
