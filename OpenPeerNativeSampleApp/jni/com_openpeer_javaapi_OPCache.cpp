#include "com_openpeer_javaapi_OPStackMessageQueue.h"
#include "openpeer/core/ICache.h"
#include "openpeer/core/ILogger.h"

#include "globals.h"

using namespace openpeer::core;

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_openpeer_javaapi_OPCache
 * Method:    setup
 * Signature: (Lcom/openpeer/javaapi/OPCacheDelegate;)V
 */
JNIEXPORT void JNICALL Java_com_openpeer_javaapi_OPCache_setup
(JNIEnv *, jclass, jobject)
{
	cachePtr = ICache::singleton();
	ICache::setup(cacheDelegatePtr);
}

/*
 * Class:     com_openpeer_javaapi_OPCache
 * Method:    singleton
 * Signature: ()Lcom/openpeer/javaapi/OPCache;
 */
JNIEXPORT jobject JNICALL Java_com_openpeer_javaapi_OPCache_singleton
(JNIEnv *env, jclass)
{
	jclass cls;
	jmethodID method;
	jobject object;
	JNIEnv *jni_env = 0;

	jni_env = getEnv();
	if(jni_env)
	{
		cls = findClass("com/openpeer/javaapi/OPCache");
		method = env->GetMethodID(cls, "<init>", "()V");
		object = env->NewObject(cls, method);

		cachePtr = ICache::singleton();

	}
	return object;
}

/*
 * Class:     com_openpeer_javaapi_OPCache
 * Method:    fetch
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_openpeer_javaapi_OPCache_fetch
(JNIEnv *env, jobject, jstring cookieNamePath)
{
	jstring ret;
	String cookieNamePathString;
	cookieNamePathString = env->GetStringUTFChars(cookieNamePath, NULL);
	if (cookieNamePathString == NULL) {
		return ret;
	}

	if (cachePtr)
	{

		ret =  env->NewStringUTF(cachePtr->fetch(cookieNamePathString).c_str());
	}

	return ret;
}

/*
 * Class:     com_openpeer_javaapi_OPCache
 * Method:    store
 * Signature: (Ljava/lang/String;Landroid/text/format/Time;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_openpeer_javaapi_OPCache_store
(JNIEnv *env, jobject, jstring cookieNamePath, jobject expires, jstring str)
{
	jclass cls;
	jmethodID method;
	jobject object;
	JNIEnv *jni_env = 0;

	String cookieNamePathString;
	cookieNamePathString = env->GetStringUTFChars(cookieNamePath, NULL);
	if (cookieNamePathString == NULL) {
		return;
	}

	Time t;
	jni_env = getEnv();

	cls = findClass("android/text/format/Time");
	if(jni_env->IsInstanceOf(expires, cls) == JNI_TRUE)
	{
		jmethodID timeMethodID   = jni_env->GetMethodID(cls, "toMillis", "(Z)J");
		long longValue = (long) jni_env->CallIntMethod(expires, timeMethodID, false);
		t = boost::posix_time::from_time_t(longValue/1000) + boost::posix_time::millisec(longValue % 1000);
	}

	String strString;
	strString = env->GetStringUTFChars(str, NULL);
	if (strString == NULL) {
		return;
	}

	if (cachePtr)
	{

		cachePtr->store(cookieNamePathString, t, strString);
	}
}

/*
 * Class:     com_openpeer_javaapi_OPCache
 * Method:    clear
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_openpeer_javaapi_OPCache_clear
(JNIEnv *env, jobject, jstring cookieNamePath)
{
	String cookieNamePathString;
	cookieNamePathString = env->GetStringUTFChars(cookieNamePath, NULL);
	if (cookieNamePathString == NULL) {
		return;
	}
	if (cachePtr)
	{

		cachePtr->clear(cookieNamePathString);
	}
}
#ifdef __cplusplus
}
#endif
