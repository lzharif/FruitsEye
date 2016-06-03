LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
#OPENCVROOT_ANDROID is a system environment variable that points toe OpenCv-android-sdk
#example: OPENCVROOT_ANDROID = D\:\\OpenCV-android-sdk
OPENCVROOT:= $(OPENCVROOT_ANDROID)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include D:\\OpenCV-android-sdk\\sdk\\native\\jni\\OpenCV.mk



LOCAL_SRC_FILES := capturefruitactivity.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := ampas

include $(BUILD_SHARED_LIBRARY)