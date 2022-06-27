#include <jni.h>
#include <stdio.h>
#include <hello-jni.h>

JNIEXPORT void JNICALL Java_cn_az_code_jni_HelloJni_sayHello(JNIEnv *env, jobject thisObj)
{
    printf("hello world with jni!\n");
    return;
}