package com.example.androidtestcamera4;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;


public class GalleryActivity extends Activity {

	private final static String TAG = GalleryActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.setContentView(R.layout.activity_gallery);
	}

	@Override
	public void onStart() {
		super.onStart();
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Log.d(TAG, "onStart()");
	}

	@Override
	public void onResume() {
		super.onResume();
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Log.d(TAG, "onResume()");
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onDestroy();
	}
}
