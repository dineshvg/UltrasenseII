#include <jni.h>
#include <string>

extern "C"
jstring
Java_dinesh_fraunhofer_emk_de_ultrasenseii_StartActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
