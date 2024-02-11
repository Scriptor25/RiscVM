package io.scriptor.riscvm.asm;

public record ASMConfig(int machine_memory, int machine_registers, String... order) {
}
