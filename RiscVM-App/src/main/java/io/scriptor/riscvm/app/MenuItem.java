package io.scriptor.riscvm.app;

public record MenuItem(String label, Runnable action, Shortcut shortcut) {
}
