package io.scriptor.riscvm;

public class ErrorUtil {

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
            throw new RuntimeException(t);
        }
    }

    public static <T> T handleT(IErrorHandlerT<T> handler) {
        try {
            return handler.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
