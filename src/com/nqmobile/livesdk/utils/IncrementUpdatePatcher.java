package com.nqmobile.livesdk.utils;

public class IncrementUpdatePatcher {
	
	static {
        //调用 jni so文件
        System.loadLibrary("Patcher");
    }	
	// 合并差分包的jni方法名声明
    public native int patcher(String old, String newapk, String patch);
}
