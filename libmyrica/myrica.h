#include <jni.h>

#ifndef _INCLUDE_MYRICA_HEADER
#define _INCLUDE_MYRICA_HEADER

#ifdef __cplusplus
extern "C"
{
#endif
    /*
     * Class:     cn_az_code_jni_HelloJni
     * Method:    say
     * Signature: ()V
     */
    JNIEXPORT void JNICALL jni_native_say(JNIEnv *, jobject);

#ifdef __cplusplus
}

#endif
#endif