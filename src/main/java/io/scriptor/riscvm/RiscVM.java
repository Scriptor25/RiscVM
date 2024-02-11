package io.scriptor.riscvm;

import io.scriptor.riscvm.asm.ASMConfig;
import io.scriptor.riscvm.asm.Assembler;
import io.scriptor.riscvm.vm.Machine;
import io.scriptor.riscvm.vm.NullException;

import static io.scriptor.riscvm.ByteUtil.kb;

public class RiscVM {

    public static void main(String[] args) {
        final var config = new ASMConfig(kb(16), 32, "text", "data", "stack");

        final var machine = new Machine(config);
        final var mem = machine.getMemory();

        final var asm = new Assembler(ClassLoader.getSystemResourceAsStream("fib.s"), config, mem.getBuffer());
        // System.out.println(asm);
        // System.out.println(machine);

        int count = 0;
        while (true) {
            try {
                count++;
                machine.cycle();
            } catch (NullException e) {
                break;
            }
        }

        System.out.printf("Execution took %d cycles%n", count);
    }
}
