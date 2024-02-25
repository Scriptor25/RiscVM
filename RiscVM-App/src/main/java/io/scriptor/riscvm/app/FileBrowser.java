package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;

import java.io.File;
import java.util.Arrays;

public class FileBrowser {

    private final ImBoolean mOpen = new ImBoolean();
    private File mDirectory = new File(".").getAbsoluteFile();
    private File mFile;

    public void open() {
        mOpen.set(true);
    }

    public void close() {
        mOpen.set(false);
    }

    public File getFile() {
        return mFile;
    }

    private static String toBytes(long bytes) {
        if (bytes < 1024L) return String.format("%d B", bytes);
        if (bytes < 1024L * 1024L) return String.format("%d KB", bytes / 1024L);
        if (bytes < 1024L * 1024L * 1024L) return String.format("%d MB", bytes / (1024L * 1024L));
        if (bytes < 1024L * 1024L * 1024L * 1024L) return String.format("%d GB", bytes / (1024L * 1024L * 1024L));
        return String.format("%d TB", bytes / (1024L * 1024L * 1024L * 1024L));
    }

    public void show() {
        if (!mOpen.get())
            return;

        boolean back = false;
        boolean close = false;
        File into = null;

        if (ImGui.begin("File Browser", mOpen)) {
            final var files = mDirectory.listFiles();
            if (files != null) {
                final var entries = Arrays.stream(files).sorted((a, b) -> {
                    int weight = 0;
                    if (a.isFile()) weight++;
                    if (b.isFile()) weight--;
                    return weight;
                }).toList();

                // name | size | ...
                if (ImGui.beginTable("##entries", 3, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
                    ImGui.tableSetupColumn("Name");
                    ImGui.tableSetupColumn("Size");
                    ImGui.tableSetupColumn("");
                    ImGui.tableHeadersRow();

                    if (mDirectory.getParentFile() != null) {
                        if (ImGui.tableNextColumn()) {
                            ImGui.pushStyleColor(ImGuiCol.Button, 0x00000000);
                            back = ImGui.button("..");
                            ImGui.popStyleColor();
                        }

                        ImGui.tableNextColumn();
                        ImGui.tableNextColumn();
                    }

                    int i = 0;
                    for (final var entry : entries) {
                        ImGui.pushID(i);

                        int col = entry.isHidden() ? 0x77eeeeee : 0xffeeeeee;

                        if (ImGui.tableNextColumn()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, col);
                            ImGui.pushStyleColor(ImGuiCol.Button, 0x00000000);
                            if (ImGui.button(entry.getName())) {
                                if (entry.isFile()) {
                                    mFile = entry;
                                    close = true;
                                } else if (entry.isDirectory()) {
                                    into = entry;
                                }
                            }
                            ImGui.popStyleColor(2);
                        }

                        if (ImGui.tableNextColumn()) {
                            if (entry.isFile())
                                ImGui.textColored(col, String.format("%6s", toBytes(entry.length())));
                        }

                        ImGui.tableNextColumn();

                        ImGui.popID();
                    }
                    ImGui.endTable();
                }
            }
        }
        ImGui.end();

        if (back)
            mDirectory = mDirectory.getParentFile();
        if (close)
            mOpen.set(false);
        if (into != null)
            mDirectory = into;
    }
}
