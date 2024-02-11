package io.scriptor.riscvm;

import io.scriptor.riscvm.asm.Assembler;
import io.scriptor.riscvm.vm.Machine;
import io.scriptor.riscvm.vm.NullException;

import static io.scriptor.riscvm.RV32IM.*;
import static io.scriptor.riscvm.RV32IM.RegisterAlias.A0;
import static io.scriptor.riscvm.RV32IM.RegisterAlias.ZERO;

public class RiscVM {

    public static void main(String[] args) {
        final var machine = new Machine(256, 32);

        final var mem = machine.getMemory();
        int ptr = 0;
        mem.setInstruction(0, ADDI, ZERO, ZERO, 0);
        mem.setInstruction(ptr += INST_BYTES, ADDI, A0, A0, 4 * INST_BYTES);
        mem.setInstruction(ptr += INST_BYTES, SYS, 0x01);
        mem.setInstruction(ptr += INST_BYTES, NULL);
        mem.setString(ptr += INST_BYTES, "Hello World!");
        ptr += 0x30;
        mem.setString(ptr, "Hi, my name is Felix Schreiber and I just wrote this!");

        while (true) {
            try {
                machine.cycle();
            } catch (NullException e) {
                break;
            }
        }

        System.out.println(machine);

        new Assembler(ClassLoader.getSystemResourceAsStream("test.s"));
    }
}
