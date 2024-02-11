package io.scriptor.riscvm;

public class ByteUtil {

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
}
