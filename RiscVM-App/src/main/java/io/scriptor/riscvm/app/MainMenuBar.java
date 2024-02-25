package io.scriptor.riscvm.app;

import imgui.ImGui;

import java.util.List;
import java.util.Vector;

import static org.lwjgl.glfw.GLFW.*;

public class MainMenuBar {

    private final List<Menu> mMenus = new Vector<>();

    public MainMenuBar addMenu(String label, MenuItem... items) {
        mMenus.add(new Menu(label, items));
        return this;
    }

    public void show() {
        if (ImGui.beginMainMenuBar()) {
            for (final var menu : mMenus)
                if (ImGui.beginMenu(menu.label())) {
                    for (final var item : menu.items())
                        if (ImGui.menuItem(item.label(), item.shortcut().toString()))
                            item.action().run();
                    ImGui.endMenu();
                }
            ImGui.endMainMenuBar();
        }
    }

    public void onKeyAction(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_RELEASE)
            for (final var menu : mMenus)
                for (final var item : menu.items())
                    if (item.shortcut().matches(
                            (mods & GLFW_MOD_ALT) != 0,
                            (mods & GLFW_MOD_CONTROL) != 0,
                            (mods & GLFW_MOD_SUPER) != 0,
                            (mods & GLFW_MOD_SHIFT) != 0,
                            key))
                        item.action().run();
    }
}
