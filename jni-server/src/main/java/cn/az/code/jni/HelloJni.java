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
        System.load("hello-jni.so");
    }

    private native void native_sayHello();

    public static void main(String[] args) {
        HelloJni hj = new HelloJni();
        hj.native_sayHello();
    }
}
