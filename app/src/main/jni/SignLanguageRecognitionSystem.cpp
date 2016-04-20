
#include "EasyLog.h" 
#include "System_Manager/System_Manager.h"
#include "tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService.h"

#define TAG "RecognitionService"

extern "C"
{
	System_Manager slr_system;

	/*
	* Class:     com_example_signlanguagerecognitionsystem_RecognitionService
	* Method:    initialize
	* Signature: ()V
	*/
	JNIEXPORT void JNICALL Java_tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService_initialize (JNIEnv *enc, jobject obj)
	{
		slr_system.m_SLRM.SetFeatureType("GLOVE");
		
		slr_system.m_SLRM.Reset();
		int n = slr_system.m_SLRM.m_database.m_signMotionFileName.size();
		LogD(TAG, "Database size: %d", n);
	}
	
	/*
	* Class:     com_example_signlanguagerecognitionsystem_RecognitionService
	* Method:    reset
	* Signature: ()V
	*/
	JNIEXPORT void JNICALL Java_tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService_reset (JNIEnv *enc, jobject )
	{
		slr_system.m_SLRM.IsRecognition = false;
		slr_system.m_SLRM.m_candidateWord = "";
		slr_system.m_SLRM.Reset();
		slr_system.motionDataQ[0].clear();
	}
	
	/*
	* Class:     com_example_signlanguagerecognitionsystem_RecognitionService
	* Method:    pass_data
	* Signature: (F)V
	*/
	JNIEXPORT void JNICALL Java_tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService_pass_1data (JNIEnv *env, jobject obj, jfloatArray dataL, jfloatArray dataR)
	{
		// left - 0, right - 1
		// Push left [0]
		jfloat *x = env->GetFloatArrayElements(dataL, NULL);
		MotionDatastru motionDatastruL = {{x[0],x[1],x[2],x[3],x[4],x[5],x[6],x[7],x[8],x[9]}};
		motionDatastruL._rotation = Quaternion(x[10],x[11],x[12],x[13]);
		motionDatastruL._acc = Vector3(x[14],x[15],x[16]);
		motionDatastruL._rssi = x[17];
		slr_system.motionDataQ[0].push_back(motionDatastruL);
		env->ReleaseFloatArrayElements(dataL, x, 0);
		
		// Push right [1]
		x = env->GetFloatArrayElements(dataR, NULL);
		MotionDatastru motionDatastruR = {{x[0],x[1],x[2],x[3],x[4],x[5],x[6],x[7],x[8],x[9]}};
		motionDatastruR._rotation = Quaternion(x[10],x[11],x[12],x[13]);
		motionDatastruR._acc = Vector3(x[14],x[15],x[16]);
		motionDatastruR._rssi = x[17];
		slr_system.motionDataQ[1].push_back(motionDatastruR);
		env->ReleaseFloatArrayElements(dataR, x, 0);
	}
	
	/*
	* Class:     com_example_signlanguagerecognitionsystem_RecognitionService
	* Method:    run_recognize
	* Signature: ()V
	*/
	JNIEXPORT jboolean JNICALL Java_tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService_run_1recognize (JNIEnv *enc, jobject obj)
	{
		jboolean ok = false;
		
		if (slr_system.motionDataQ[0].empty())
			LogD(TAG, "Data[0] is empty.");
		if (slr_system.motionDataQ[1].empty())
			LogD(TAG, "Data[1] is empty.");
		
		while (!slr_system.motionDataQ[0].empty() && !slr_system.motionDataQ[1].empty())
		{
			slr_system.m_SLRM.RecognizeProcessing(slr_system.motionDataQ[0].at(0), slr_system.motionDataQ[1].at(0));
			slr_system.motionDataQ[0].pop_front();
			slr_system.motionDataQ[1].pop_front();
			if (slr_system.m_SLRM.IsRecognition)
			{
				ok = true;
				slr_system.m_SLRM.IsRecognition = false;
			}
		}
		return ok;
	}
	
	JNIEXPORT jbyteArray JNICALL Java_tw_edu_ntust_jojllman_wearableapplication_BLE_RecognitionService_getString (JNIEnv *env, jobject obj)
	{
		jbyteArray arr = env->NewByteArray(slr_system.m_SLRM.m_candidateWord.length());
		env->SetByteArrayRegion(arr,0,slr_system.m_SLRM.m_candidateWord.length(),(jbyte*)slr_system.m_SLRM.m_candidateWord.c_str());
		return arr;
	}
}

