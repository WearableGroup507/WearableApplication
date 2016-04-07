LOCAL_PATH := $(call my-dir)
PARENT_PATH := $(call my-dir)\..

include $(CLEAR_VARS)

LOCAL_MODULE := SignLanguageRecognitionSystem
LOCAL_CFLAGS := -std=c++11 -frtti -fexceptions -I$(PARENT_PATH)/include/

LOCAL_SRC_FILES := \
	SignLanguageRecognitionSystem.cpp \
	$(LOCAL_PATH)\IO_Module\IO_Module.cpp \
	$(LOCAL_PATH)\KD_Tree\kdtree2.cpp \
	$(LOCAL_PATH)\KD_Tree\kdtree2.hpp \
	$(LOCAL_PATH)\LazyNeighborhoodGraph\LazyNeighborGraph.cpp \
	$(LOCAL_PATH)\Math\AABB2D.cpp \
	$(LOCAL_PATH)\Math\AABB3D.cpp \
	$(LOCAL_PATH)\Math\AxisAlignedBox.cpp \
	$(LOCAL_PATH)\Math\Math.cpp \
	$(LOCAL_PATH)\Math\Matrix3.cpp \
	$(LOCAL_PATH)\Math\Matrix4.cpp \
	$(LOCAL_PATH)\Math\Plane.cpp \
	$(LOCAL_PATH)\Math\Polygon2D.cpp \
	$(LOCAL_PATH)\Math\Quaternion.cpp \
	$(LOCAL_PATH)\Math\Vector2.cpp \
	$(LOCAL_PATH)\Math\Vector3.cpp \
	$(LOCAL_PATH)\Math\Vector4.cpp \
	$(LOCAL_PATH)\OLNG\OLNG.cpp \
	$(LOCAL_PATH)\SignMotionData\SignMotionData.cpp \
	$(LOCAL_PATH)\SLR_Manager\SLR_Manager.cpp \
	$(LOCAL_PATH)\System_Manager\System_Manager.cpp \
	$(LOCAL_PATH)\WeightGenerator\WeightGenerator.cpp \
	$(LOCAL_PATH)\Glove\GloveData.cpp \

LOCAL_LDLIBS := \
	-llog \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_atomic-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_chrono-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_context-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_date_time-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_exception-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_filesystem-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_graph-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_iostreams-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_c99-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_c99f-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_c99l-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_tr1-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_tr1f-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_math_tr1l-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_prg_exec_monitor-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_program_options-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_random-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_regex-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_serialization-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_system-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_test_exec_monitor-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_thread-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_timer-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_unit_test_framework-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_wave-gcc-mt-1_53.a \
	-L$(PARENT_PATH)/libs/$(TARGET_ARCH_ABI)/boost_wserialization-gcc-mt-1_53.a \

LOCAL_C_INCLUDES += \

include $(BUILD_SHARED_LIBRARY)
