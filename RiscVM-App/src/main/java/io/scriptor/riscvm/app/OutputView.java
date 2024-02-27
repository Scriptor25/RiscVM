package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutputView extends ByteArrayOutputStream {

    private final ImGuiTextFilter mFilter = new ImGuiTextFilter();
    private final ImBoolean mAutoScroll = new ImBoolean(true);
    private final ByteArrayOutputStream mStream = new ByteArrayOutputStream();

    public ByteArrayOutputStream getStream() {
        return mStream;
    }

    public void show() {
        if (!ImGui.begin("Output")) {
            ImGui.end();
            return;
        }

        // Options menu
        if (ImGui.beginPopup("Options")) {
            ImGui.checkbox("Auto-scroll", mAutoScroll);
            ImGui.endPopup();
        }
        // Main window
        if (ImGui.button("Options"))
            ImGui.openPopup("Options");
        ImGui.sameLine();
        boolean clear = ImGui.button("Clear");
        ImGui.sameLine();
        boolean copy = ImGui.button("Copy");
        ImGui.sameLine();
        mFilter.draw("Filter", -100.0f);
        ImGui.separator();
        ImGui.beginChild("scrolling", 0, 0, false, ImGuiWindowFlags.HorizontalScrollbar);
        if(clear)
            mStream.reset();
        if (copy)
            ImGui.logToClipboard();
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        if (mFilter.isActive()) {
            mStream.toString()
                    .lines()
                    .filter(mFilter::passFilter)
                    .forEach(ImGui::textUnformatted);
        } else {
            mStream.toString()
                    .lines()
                    .forEach(ImGui::textUnformatted);
        }
        ImGui.popStyleVar();
        if (mAutoScroll.get() && ImGui.getScrollY() >= ImGui.getScrollMaxY())
            ImGui.setScrollHereY(1.0f);
        ImGui.endChild();
        ImGui.end();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }
}
