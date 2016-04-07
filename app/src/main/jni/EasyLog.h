#include <Android/log.h>

#define LogD(tag, args...) __android_log_print(ANDROID_LOG_DEBUG, tag, ##args);
#define LogW(tag, args...) __android_log_print(ANDROID_LOG_WARN,  tag, ##args);
#define LogI(tag, args...) __android_log_print(ANDROID_LOG_INDO,  tag, ##args);
#define LogE(tag, args...) __android_log_print(ANDROID_LOG_ERROR, tag, ##args);
