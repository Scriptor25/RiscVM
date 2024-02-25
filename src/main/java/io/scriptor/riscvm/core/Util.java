package io.scriptor.riscvm.core;

import java.util.Optional;

public class Util {

    public static int kb(int n) {
        return n * 1024;
    }

    public static int mb(int n) {
        return kb(n) * 1024;
    }

    public static String unit(int bytes) {
        if (bytes >= mb(1)) return String.format("%d MB", bytes / mb(1));
        if (bytes >= kb(1)) return String.format("%d KB", bytes / kb(1));
        return String.format("%d B", bytes);
    }

    @FunctionalInterface
    public interface IErrorHandler {
        void run() throws Throwable;
    }

    @FunctionalInterface
    public interface IErrorHandlerT<T> {
        T run() throws Throwable;
    }

    public static void handle(IErrorHandler handler) {
        try {
            handler.run();
        } catch (Throwable t) {
            // throw new RuntimeException(t);
            System.err.println(t);
        }
    }

    public static <T> Optional<T> handleT(IErrorHandlerT<T> handler) {
        try {
            return Optional.of(handler.run());
        } catch (Throwable t) {
            // throw new RuntimeException(t);
            System.err.println(t);
            return Optional.empty();
        }
    }
}
