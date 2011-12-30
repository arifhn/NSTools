# Copyright 2011 Crossbones Software

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PACKAGE_NAME := NSTools

# disable proguard to make easy debugging
#LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

# sign apk with nstools key
LOCAL_CERTIFICATE := nstools

include $(BUILD_PACKAGE)
