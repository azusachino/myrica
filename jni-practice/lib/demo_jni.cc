#include <demo_jni.h>
#include <iostream>

extern "C" JNIEXPORT void JNICALL jni_native_say(JNIEnv *, jobject)
{
    std::cout << "Hello Demo Jni" << std::endl;
}