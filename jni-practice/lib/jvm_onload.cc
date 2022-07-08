#include <jni.h>
#include <demo_jni.h>
#include <iostream>

JavaVM *pj_jni_jvm_ = NULL;
jobject pj_class_loader_;

static const char *jni_clazz_impl_ = "cn/az/jni/DemoJni";

static JNINativeMethod jni_methods_[] = {
    {(char *)"native_say", (char *)"()V", (void *)jni_native_say}};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods)
{
    jclass clazz = env->FindClass(className);
    if (clazz == NULL)
    {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
    {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{

    std::cout << "jvm OnLoad" << std::endl;

    // obtain vm object
    pj_jni_jvm_ = vm;

    // create new jni env obj
    JNIEnv *jni_env = nullptr;
    jint result = vm->GetEnv(reinterpret_cast<void **>(&jni_env), JNI_VERSION_1_8);

    // register native methods
    jint ret = registerNativeMethods(jni_env, jni_clazz_impl_, jni_methods_, sizeof(jni_methods_) / sizeof(jni_methods_[0]));
    if (JNI_TRUE == ret)
        std::cout << "native methods registered" << std::endl;

    // notify JVM the JNI Version
    return JNI_VERSION_1_8;
}

JNIEXPORT jint JNICALL OnUnload(JavaVM *vm, void *reserved)
{
    std::cout << "jvm OnUnload" << std::endl;
    return JNI_VERSION_1_8;
}