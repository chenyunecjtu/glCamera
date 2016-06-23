LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)

LOCAL_MODULE    := xiaoyao
LOCAL_SRC_FILES := YUVConvert.cpp \





LOCAL_LDLIBS :=  -lz -ldl -lm

LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -DNO_CRYPTO
LOCAL_CFLAGS += -fexceptions -march=armv7-a -mfloat-abi=softfp -mfpu=neon
LOCAL_LDFLAGS += -Wl,--fix-cortex-a8 -llog -lz -lm -lOpenMAXAL -landroid  #-lmediandk
LOCALC_FLAGS := -fPIC
LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true
#LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)