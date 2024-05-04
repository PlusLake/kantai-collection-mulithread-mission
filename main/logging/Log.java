package main.logging;

import java.util.function.Supplier;

public class Log {
    public static void info(String format, Object... objects) {
        // TODO: design simple logging formats
        System.err.printf(format, objects);
    }

    public static void timer(String name, Runnable runnable) {
        timer(name, () -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T timer(String name, Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        T t = supplier.get();
        long result = System.currentTimeMillis() - start;
        info("%s ended in %dms.\n", name, result);
        return t;
    }
}
