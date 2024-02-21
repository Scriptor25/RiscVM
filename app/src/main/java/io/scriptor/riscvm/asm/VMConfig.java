package io.scriptor.riscvm.asm;

public class VMConfig {

    private final int memory;
    private final String[] sections;

    public VMConfig(int memory, String... sections) {
        this.memory = memory;
        this.sections = sections;
    }

    public int memory() {
        return memory;
    }

    public String[] sections() {
        return sections;
    }
}
