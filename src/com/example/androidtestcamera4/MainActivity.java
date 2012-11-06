package com.example.androidtestcamera4;

import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {

	private final static String TAG = MainActivity.class.getSimpleName();
	private Handler handler = new Handler();

	private Button buttonTake;
	private Button buttonFind;
	private Button buttonLook;
	private Button buttonOptions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		this.setContentView(R.layout.activity_main);

		// generate Sensors object
		MyApplication.sensors = new Sensors(this);
		// generate vibrator
		MyApplication.vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
		// generate character manager
		MyApplication.characterManager = new CharacterManager(this);

		buttonTake = (Button)this.findViewById(R.id.buttonTake);
		buttonFind = (Button)this.findViewById(R.id.buttonFind);
		buttonLook = (Button)this.findViewById(R.id.buttonLook);
		buttonOptions = (Button)this.findViewById(R.id.buttonOptions);

		buttonTake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CameraActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
		buttonFind.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("AR Character Camera")
				.setMessage("探せ")
				.setPositiveButton("OK", null)
				.show();
			}
		});
		buttonLook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("AR Character Camera")
				.setMessage("見よ") 
				.setPositiveButton("OK", null)
				.show();
			}
		});
		buttonOptions.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("AR Character Camera")
				.setMessage("under construction")
				.setPositiveButton("OK", null)
				.show();
			}
		});
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
	}
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()");
	}
	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
	}
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}
}
