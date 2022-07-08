package cn.az.jni;

import java.util.Objects;

/**
 * Jni Instance
 * Registered in Jni_env
 */
public class DemoJni {

    private static DemoJni INSTANCE = null;

    /**
     * System.loadLibrary in the native function
     */
    static {
        System.out.println("Library Path is ".concat(System.getProperty("java.library.path")));
        // the so name extract `lib` and `.so`
        System.loadLibrary("demo_jni");
    }

    private DemoJni() {
    }

    /**
     * Get Singleton
     * 
     * @return instance
     */
    public static DemoJni getInstance() {
        synchronized (DemoJni.class) {
            if (Objects.isNull(INSTANCE)) {
                synchronized (DemoJni.class) {
                    INSTANCE = new DemoJni();
                }
            }
        }
        return INSTANCE;
    }

    public native void native_say();
}