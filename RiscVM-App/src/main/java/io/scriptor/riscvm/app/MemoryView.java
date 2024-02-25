package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import io.scriptor.riscvm.core.Instruction;

import java.nio.ByteBuffer;

public class MemoryView {

    private static final int N = 16;
    private static final String[] MEMORY_FORMATS = {"RAW", "INSTRUCTIONS"};
    private int mSelectedMemoryFormat = 0;

    private final ByteBuffer mBuffer;

    public MemoryView(ByteBuffer buffer) {
        mBuffer = buffer;
    }

    public void show(int pc) {
        if (ImGui.begin("Memory")) {
            if (ImGui.beginCombo("##memory_format", MEMORY_FORMATS[mSelectedMemoryFormat])) {
                for (int i = 0; i < MEMORY_FORMATS.length; i++) {
                    boolean isSelected = mSelectedMemoryFormat == i;
                    if (ImGui.selectable(MEMORY_FORMATS[i], isSelected))
                        mSelectedMemoryFormat = i;
                    if (isSelected)
                        ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }

            switch (mSelectedMemoryFormat) {
                case 0 -> showRaw(mBuffer, pc);
                case 1 -> showInst(mBuffer, pc);
            }
        }
        ImGui.end();
    }

    private void showRaw(ByteBuffer buffer, int pc) {
        if (ImGui.beginTable("Memory", N + 3, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
            for (int i = 0; i < ImGui.tableGetColumnCount() - 1; i++)
                ImGui.tableSetupColumn(i == 0 ? "Address" : i == N + 1 ? "ASCII" : String.format("%01X", i - 1));
            ImGui.tableHeadersRow();

            buffer.position(0);
            for (int i = 0; buffer.hasRemaining(); i += N) {
                if (ImGui.tableNextColumn()) {
                    final var text = String.format("%08X:", i);
                    if (i / N == pc / N) ImGui.textColored(0xff0000ff, text);
                    else ImGui.text(text);
                }

                for (int j = 0; j < N; j++) {
                    final var b = buffer.get();
                    final var text = String.format("%02X", b);
                    if (ImGui.tableNextColumn()) {
                        if ((i + j) >= pc && (i + j) <= pc + 3) ImGui.textColored(0xff0000ff, text);
                        else if (b == 0) ImGui.textColored(0x55ffffff, text);
                        else ImGui.text(text);
                    }
                }

                if (ImGui.tableNextColumn()) {
                    buffer.position(buffer.position() - N);
                    final var builder = new StringBuilder();
                    for (int j = 0; j < N; j++) {
                        final var c = buffer.get();
                        builder.append(String.format("%c", 0x20 <= c && c <= 0x7E ? c : '.'));
                    }

                    ImGui.text(builder.toString());
                }

                ImGui.tableNextColumn();
            }
            ImGui.endTable();
        }
    }

    private void showInst(ByteBuffer buffer, int pc) {
        final var n = N / 4;
        if (ImGui.beginTable("Memory", n + 2, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
            for (int i = 0; i < ImGui.tableGetColumnCount() - 1; i++)
                ImGui.tableSetupColumn(i == 0 ? "Address" : String.format("%01X", (i - 1) * 4));
            ImGui.tableHeadersRow();

            buffer.position(0);
            for (int i = 0; buffer.hasRemaining(); i += N) {
                if (ImGui.tableNextColumn()) {
                    final var text = String.format("%08X:", i);
                    if (i / N == pc / N) ImGui.textColored(0xff0000ff, text);
                    else ImGui.text(text);
                }

                for (int j = 0; j < N; j += 4) {
                    final var c = buffer.getInt();
                    if (ImGui.tableNextColumn()) {
                        final var inst = Instruction.valueOf(c);
                        final var text = String.format("%26s", inst == null ? "" : inst);
                        if (i + j == pc) ImGui.textColored(0xff0000ff, text);
                        else ImGui.text(text);
                    }
                }

                ImGui.tableNextColumn();
            }
            ImGui.endTable();
        }
    }
}
