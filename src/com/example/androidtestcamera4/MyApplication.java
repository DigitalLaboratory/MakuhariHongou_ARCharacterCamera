package com.example.androidtestcamera4;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;


public class MyApplication extends Application {

	private final static String TAG = MyApplication.class.getSimpleName();

	public static MyApplication instance = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		instance = this;
		sensors = new Sensors(this);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d(TAG, "onLowMemory()");
		Toast.makeText(this, "Low Memory!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTerminate() {
		Log.d(TAG, "onTerminate()");
		super.onTerminate();
	}

	public static Sensors sensors = null;
	public static Vibrator vibrator = null;
	public static CharacterManager characterManager = null;

	public static int undergroundCharacterIndex = 0;
}
