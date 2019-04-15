package utils;


public class Debug {
    public static void dp(Object o, String message) {
        System.out.println("debug: Thread#" + Thread.currentThread().getId() + " [" + o.getClass().getSimpleName() + ":" + message + "]");
    }
}
