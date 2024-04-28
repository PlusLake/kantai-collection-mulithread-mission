package main.exception;

import java.util.concurrent.Callable;

public class Exceptions {
    public static <T> T wrap(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
