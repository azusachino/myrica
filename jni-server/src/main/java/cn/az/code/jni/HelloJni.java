package cn.az.code.jni;

import cn.az.code.jni.support.JniDeploy;

/**
 * JNI Demo Application
 * 
 * @author az
 * @since 2022-06-27
 */
public class HelloJni {

    static {
        JniDeploy.getInstance().deploy();
        // load the so library
        System.loadLibrary("myrica");
    }

    public static void main(String[] args) {
        HelloJni hj = new HelloJni();
        hj.native_say();
    }

    /**
     * Native Say Hello
     */
    private native void native_say();
}
