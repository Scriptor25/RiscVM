package io.scriptor.riscvm.app;

import static org.lwjgl.glfw.GLFW.*;

public record Shortcut(boolean isAlt, boolean isCtrl, boolean isSuper, boolean isShift, int keyCode) {

    public boolean matches(boolean isAlt, boolean isCtrl, boolean isSuper, boolean isShift, int keyCode) {
        return this.isAlt == isAlt && this.isCtrl == isCtrl && this.isSuper == isSuper && this.isShift == isShift && this.keyCode == keyCode;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        if (isAlt) builder.append("Alt+");
        if (isCtrl) builder.append("Ctrl");
        if (isSuper) builder.append("Super+");
        if (isShift) builder.append("Shift+");
        builder.append(getKeyName(keyCode));

        return builder.toString();
    }

    private static String getKeyName(int keyCode) {
        final var name = glfwGetKeyName(keyCode, glfwGetKeyScancode(keyCode));
        if (name != null)
            return name.toUpperCase();

        return switch (keyCode) {
            case GLFW_KEY_ESCAPE -> "ESC";
            default -> "UNDEF";
        };
    }
}
