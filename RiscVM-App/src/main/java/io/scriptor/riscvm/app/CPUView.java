package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import io.scriptor.riscvm.core.ISA;

public class CPUView {

    private final int[] mRegisters;

    public CPUView(int[] registers) {
        mRegisters = registers;
    }

    public void show() {
        if (ImGui.begin("CPU")) {
            if (ImGui.beginTable("Registers", 5, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
                ImGui.tableSetupColumn("Register");
                ImGui.tableSetupColumn("Hexadecimal");
                ImGui.tableSetupColumn("Decimal");
                ImGui.tableSetupColumn("ASCII");
                ImGui.tableHeadersRow();

                for (int i = 0; i < mRegisters.length; i++) {
                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%3s:", ISA.RegisterAlias.values()[i + 1]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%08X", mRegisters[i]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%d", mRegisters[i]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%c", mRegisters[i]));

                    ImGui.tableNextColumn();
                }
                ImGui.endTable();
            }
        }
        ImGui.end();
    }
}
