package cn.az.jni;

/**
 * Main Class
 */
public class DemoMain {

    public static void main(String[] args) {
        DemoJni.getInstance().native_say();
    }
}
