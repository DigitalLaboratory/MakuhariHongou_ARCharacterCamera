package com.example.androidtestcamera4;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class GalleryActivity extends Activity {

	private final static String TAG = GalleryActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.activity_gallery);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}
}
