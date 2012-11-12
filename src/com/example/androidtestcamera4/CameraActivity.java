package com.example.androidtestcamera4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


public class CameraActivity extends Activity {

	private final static String TAG = CameraActivity.class.getSimpleName();

	private LinearLayout linearLayoutMySurfaceView;
	private MySurfaceView mySurfaceView;
	private LinearLayout linearLayoutSurfaceViewPreview;
	private SurfaceView surfaceViewPreview;
	private LinearLayout linearLayoutButtons;

	private ToggleButton buttonFlash;
	private Button buttonMenu;
	private ToggleButton buttonLocation;
	private ToggleButton buttonDay;
	private Button buttonThumbnail;
	private Button buttonShutter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");

		// remove title-bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_camera);
		// remove status-bar
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		linearLayoutMySurfaceView = (LinearLayout)this.findViewById(R.id.linearLayoutMySurfaceView);
		mySurfaceView = (MySurfaceView)this.findViewById(R.id.mySurfaceView);

		linearLayoutSurfaceViewPreview = (LinearLayout)this.findViewById(R.id.linearLayoutSurfaceViewPreview);
		surfaceViewPreview = (SurfaceView)this.findViewById(R.id.surfaceViewPreview);
		SurfaceHolder surfaceHolder = surfaceViewPreview.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			// deprecated となるも これ 無くば XPERIA にて 落つ。
		surfaceHolder.addCallback(surfaceHolderCallback);

		surfaceViewPreview.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (camera != null) {
						MyApplication.vibrator.vibrate(5);
						camera.autoFocus(autoFocusCallback);
					}
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					break;
				}
				return false;
			}
		});

		linearLayoutButtons = (LinearLayout)this.findViewById(R.id.linearLayoutButtons);

		buttonFlash = (ToggleButton)this.findViewById(R.id.buttonFlash);
		buttonMenu = (Button)this.findViewById(R.id.buttonMenu);
		buttonLocation = (ToggleButton)this.findViewById(R.id.buttonLocation);
		buttonDay = (ToggleButton)this.findViewById(R.id.buttonDay);
		buttonThumbnail = (Button)this.findViewById(R.id.buttonThumbnail);
		buttonShutter = (Button)this.findViewById(R.id.buttonShutter);

		buttonFlash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (camera != null) {
					Camera.Parameters parameters = camera.getParameters();
					parameters.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameters);
				}
				MyApplication.vibrator.vibrate(5);
			}
		});
		this.registerForContextMenu(buttonMenu);
		buttonMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyApplication.vibrator.vibrate(5);
				CameraActivity.this.openContextMenu(buttonMenu);
			}
		});
		buttonLocation.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MyApplication.vibrator.vibrate(5);
				mySurfaceView.setDrawLocation(isChecked);
			}
		});
		buttonDay.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MyApplication.vibrator.vibrate(5);
				mySurfaceView.setDrawDate(isChecked);
			}
		});
		buttonThumbnail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MyApplication.vibrator.vibrate(5);
				Intent intent = new Intent(CameraActivity.this, GalleryActivity.class);
				CameraActivity.this.startActivity(intent);
			}
		});
		buttonShutter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyApplication.vibrator.vibrate(5);
				if (camera != null) {
					camera.takePicture(null, null, pictureCallback);
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
	}

	private Camera camera;

	private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "holder:" + holder);
			try {
				if (camera != null) {
					camera.reconnect();
				} else {
					camera = Camera.open();
				}
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.d(TAG, String.format("surfaceChanged() %d, %d %d", format, width, height));
			if (camera != null) {
				camera.stopPreview();
				try {
					Camera.Parameters parameters = camera.getParameters();

					List<Camera.Size> sizeList0 = parameters.getSupportedPictureSizes();
					for (Camera.Size size: sizeList0) {
						Log.d(TAG, String.format("picture: %d, %d", size.width, size.height));
					}
					Camera.Size optimalPictureSize = getOptimalPictureSize2(sizeList0, width, height);
					if (optimalPictureSize != null) {
						parameters.setPictureSize(optimalPictureSize.width, optimalPictureSize.height);
					}

					List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
					for (Camera.Size size: sizeList) {
						Log.d(TAG, String.format("preview: %d, %d", size.width, size.height));
					}
					Camera.Size optimalPreviewSize = getOptimalPreviewSize2(sizeList, width, height);
					if (optimalPreviewSize != null) {
						parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);

						ViewGroup.LayoutParams layoutParams = surfaceViewPreview.getLayoutParams();
						layoutParams.width = optimalPreviewSize.width;
						layoutParams.height = optimalPreviewSize.height;
						surfaceViewPreview.setLayoutParams(layoutParams);
						linearLayoutSurfaceViewPreview.requestLayout();

						// set layout parameters of the character view
						{
							ViewGroup.LayoutParams layoutParams2 = mySurfaceView.getLayoutParams();
							layoutParams2.width = optimalPreviewSize.width;
							layoutParams2.height = optimalPreviewSize.height;
							mySurfaceView.setLayoutParams(layoutParams2);
							mySurfaceView.invalidate();
							linearLayoutMySurfaceView.requestLayout();
						}

						camera.setParameters(parameters);
						Log.d(TAG, String.format("setParameters: %d, %d", optimalPreviewSize.width, optimalPreviewSize.height));
						Toast.makeText(CameraActivity.this, String.format("%d x %d", optimalPreviewSize.width, optimalPreviewSize.height), Toast.LENGTH_SHORT).show();
					}

				} finally {
					try {
						camera.startPreview();
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
	};

	private Camera.Size getOptimalPreviewSize2(List<Camera.Size> sizeList, int width0, int height0) {
		double ratio0 = (double)width0 / height0;
		double nearestAreaDiff = Double.MAX_VALUE;
		double nearestRatio = Double.MAX_VALUE;
		Camera.Size nearestSize = null;
		for (Camera.Size size: sizeList) {
			if (size.width == width0 && size.height == height0) {
				nearestSize = size;
				break;
			}
			if (size.width > width0 || size.height > height0) {
				continue;
			}
			double areaDiff = (double)width0 * height0 - (double)size.width * size.height;
			if (nearestAreaDiff > areaDiff) {
				nearestAreaDiff = areaDiff;
				nearestSize = size;
				nearestRatio = (double)nearestSize.width / nearestSize.height;
			} else if (nearestAreaDiff == areaDiff) {
				double ratio = (double)size.width / size.height;
				if (Math.abs(ratio - ratio0) < Math.abs(nearestRatio - ratio0)) {
					nearestSize = size;
					nearestRatio = ratio;
					nearestAreaDiff = areaDiff;
				}
			}
		}
		return nearestSize;
	}

	private Camera.Size getOptimalPictureSize2(List<Camera.Size> sizeList, int width0, int height0) {
		double ratio0 = (double)width0 / height0;
		double nearestAreaDiff = Double.MAX_VALUE;
		double nearestRatio = Double.MAX_VALUE;
		Camera.Size nearestSize = null;
		for (Camera.Size size: sizeList) {
			if (size.width == width0 && size.height == height0) {
				nearestSize = size;
				break;
			}
			if (size.width > width0 || size.height > height0) {
				continue;
			}
			double areaDiff = (double)width0 * height0 - (double)size.width * size.height;
			if (nearestAreaDiff > areaDiff) {
				nearestAreaDiff = areaDiff;
				nearestSize = size;
				nearestRatio = (double)nearestSize.width / nearestSize.height;
			} else if (nearestAreaDiff == areaDiff) {
				double ratio = (double)size.width / size.height;
				if (Math.abs(ratio - ratio0) < Math.abs(nearestRatio - ratio0)) {
					nearestSize = size;
					nearestRatio = ratio;
					nearestAreaDiff = areaDiff;
				}
			}
		}
		return nearestSize;
	}

	/**
	 * 写真画像の合成
	 * @param data
	 * @return 合成後の jpeg image
	 */
	private byte[] modifyPicture(byte[] data) {
		Bitmap pictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		// pictureBitmap は immutable なれば copy して mutable なる bitmap を作る。
		Bitmap newBitmap = null;
		try {
			newBitmap = pictureBitmap.copy(pictureBitmap.getConfig(), true);
		} catch (OutOfMemoryError e) {
			Log.d(TAG, e.getMessage(), e);
			new AlertDialog.Builder(CameraActivity.this)
			.setTitle("Error")
			.setMessage("Out of memory!!!")
			.setPositiveButton("OK", null)
			.show();
			return null;
		}
		Canvas canvas = new Canvas(newBitmap);
		Bitmap characterBitmap = MyApplication.characterManager.getCharacterScaledBitmap();
		if (characterBitmap != null) {
			int centerX = MyApplication.characterManager.getCharacterCenterX();
			int centerY = MyApplication.characterManager.getCharacterCenterY();
			Rect sourceRect = new Rect(centerX - characterBitmap.getWidth() / 2, centerY - characterBitmap.getHeight() / 2, centerX + characterBitmap.getWidth() / 2, centerY + characterBitmap.getHeight() / 2);
			// pictureBitmap size に合せる
			centerX = centerX * pictureBitmap.getWidth() / mySurfaceView.getWidth();
			centerY = centerY * pictureBitmap.getHeight() / mySurfaceView.getHeight();
			Rect destinationRect = new Rect(sourceRect);
			destinationRect.left = destinationRect.left * pictureBitmap.getHeight() / mySurfaceView.getHeight();
			destinationRect.top = destinationRect.top * pictureBitmap.getHeight() / mySurfaceView.getHeight();
			destinationRect.right = destinationRect.right * pictureBitmap.getHeight() / mySurfaceView.getHeight();
			destinationRect.bottom = destinationRect.bottom * pictureBitmap.getHeight() / mySurfaceView.getHeight();
			characterBitmap = MyApplication.characterManager.getCharacterBitmapForPrinting();
			sourceRect = new Rect(0, 0, characterBitmap.getWidth(), characterBitmap.getHeight());
			canvas.drawBitmap(characterBitmap, sourceRect, destinationRect, null);
			characterBitmap.recycle();
		}

			// charater logo -> left top
			// もと 640x480 中 277x56 を想定
		Bitmap logoBitmap = MyApplication.characterManager.getLogoBitmapForPrinting();
		if (logoBitmap != null) {
			Rect sourceRect = new Rect(0, 0, logoBitmap.getWidth(), logoBitmap.getHeight());
			int margin = canvas.getWidth() * 10 / 640;
			Rect destinationRect = new Rect(sourceRect);
			destinationRect.left = margin + 0;
			destinationRect.top = margin + 0;
			destinationRect.right = margin + canvas.getWidth() * 277 / 640;
			destinationRect.bottom = margin + destinationRect.right * 56 / 277;
			canvas.drawBitmap(logoBitmap, sourceRect, destinationRect, null);
		}

		if (buttonLocation.isChecked()) {
			// place -> left bottom
			String[] sAddressLines = MyApplication.sensors.getAddressLines();
			if (sAddressLines != null && sAddressLines.length > 0) {
				// one line?
				String sAddress = "";
				for (String sAddressLine: sAddressLines) {
					sAddress += sAddressLine + " ";
				}
				Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				textPaint.setColor(Color.WHITE);
				textPaint.setTextSize(24.0f);
				textPaint.setShadowLayer(4, 2, 2, Color.BLACK);
				FontMetrics fontMetrics = textPaint.getFontMetrics();
				int height = (int)(fontMetrics.bottom - fontMetrics.top);
				int width = (int)textPaint.measureText(sAddress);
				canvas.drawText(sAddress, height, canvas.getHeight() - 12 - 24, textPaint);
			}
		}

		if (buttonDay.isChecked()) {
			// date -> right bottom
			String sDate = new SimpleDateFormat(this.getResources().getString(R.string.DATE_FORMAT)).format(new Date());
			Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			textPaint.setColor(Color.WHITE);
			textPaint.setTextSize(24.0f);
			textPaint.setShadowLayer(4, 2, 2, Color.BLACK);
			FontMetrics fontMetrics = textPaint.getFontMetrics();
			int height = (int)(fontMetrics.bottom - fontMetrics.top);
			int width = (int)textPaint.measureText(sDate);
			canvas.drawText(sDate, canvas.getWidth() - width - height, (int)(canvas.getHeight() - 12 - 24 - 24), textPaint);
		}
		if (characterBitmap != null) {
			// copyright notice -> right bottom
			String sNotice = (String)MyApplication.characterManager.getCharacterInfo().get("copyright");
			if (sNotice != null && !sNotice.equals("")) {
				Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				textPaint.setColor(Color.WHITE);
				textPaint.setTextSize(16.0f);
				textPaint.setShadowLayer(4, 2, 2, Color.BLACK);
				FontMetrics fontMetrics = textPaint.getFontMetrics();
				int height = (int)(fontMetrics.bottom - fontMetrics.top);
				int width = (int)textPaint.measureText(sNotice);
				canvas.drawText(sNotice, canvas.getWidth() - width - height, canvas.getHeight() - 12, textPaint);
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		newBitmap.recycle();
		newBitmap = null;
		pictureBitmap.recycle();
		pictureBitmap = null;
		return baos.toByteArray();
	}

	private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				MyApplication.vibrator.vibrate(50);
				Toast.makeText(CameraActivity.this, String.format("data: %d bytes", data.length), Toast.LENGTH_SHORT).show();
				// add character
				data = modifyPicture(data);
				if (data == null) {
					return;
				}
				// save to file
				String sFileName = "IMG_" + (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()) + ".jpg";
				String sPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
				{
					File file = new File(sPath);
					if (!file.exists()) {
						file.mkdirs();
					}
				}
				sPath += "/" + sFileName;
				Uri uri = null;
				try {
					FileOutputStream fos = new FileOutputStream(sPath);
					try {
						fos.write(data);
						fos.flush();
					} finally {
						fos.close();
					}
					uri = Uri.fromFile(new File(sPath));
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
				// jump!
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "image/jpeg");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				CameraActivity.this.startActivity(Intent.createChooser(intent, "Select application"));

			} finally {

				try {
					camera.startPreview();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	};

	private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			MyApplication.vibrator.vibrate(5);
			if (success) {
				Toast.makeText(CameraActivity.this, "focused!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(CameraActivity.this, "focus failed!", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private final static int CONTEXT_MENU_ID_PICTURE_SIZE = 0;
	private final static int CONTEXT_MENU_ID_DUMMY1 = 1;
	private final static int CONTEXT_MENU_ID_DUMMY2 = 2;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		menu.setHeaderTitle("Settings");
		menu.add(0, CONTEXT_MENU_ID_PICTURE_SIZE, 0, "Picture Size");
		menu.add(0, CONTEXT_MENU_ID_DUMMY1, 0, "dummy1");
		menu.add(0, CONTEXT_MENU_ID_DUMMY2, 0, "dummy2");
	}

	private List<Camera.Size> pictureSizeList = null;
	private int checkedPictureSizeIndex = -1;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_ID_PICTURE_SIZE:
			if (camera != null) {
				Camera.Parameters parameters = camera.getParameters();
				pictureSizeList = parameters.getSupportedPictureSizes();
				String[] sSizes = new String[pictureSizeList.size()];
				Camera.Size currentSize = parameters.getPictureSize();
				checkedPictureSizeIndex = -1;
				for (int i = 0;  i < sSizes.length;  ++i) {
					Camera.Size size = pictureSizeList.get(i);
					sSizes[i] = String.format("%d × %d", size.width, size.height);
					if (size.width == currentSize.width && size.height == currentSize.height) {
						checkedPictureSizeIndex = i;
					}
				}
				new AlertDialog.Builder(CameraActivity.this)
				.setTitle("Select picture size")
				.setSingleChoiceItems(sSizes, checkedPictureSizeIndex, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkedPictureSizeIndex = which;
					}
				})
				.setCancelable(true)
				.setPositiveButton("Set", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MyApplication.vibrator.vibrate(5);
						if (checkedPictureSizeIndex >= 0) {
							Camera.Size size = pictureSizeList.get(checkedPictureSizeIndex);
							Camera.Parameters parameters = camera.getParameters();
							parameters.setPictureSize(size.width, size.height);
							Toast.makeText(CameraActivity.this, String.format("set picture size: %d x %d", size.width, size.height), Toast.LENGTH_SHORT).show();
							camera.setParameters(parameters);
						}
					}
				})
				.show();
			}
			return true;
		case CONTEXT_MENU_ID_DUMMY1:
			Toast.makeText(CameraActivity.this, "dummy1 selected.", Toast.LENGTH_SHORT).show();
			return true;
		case CONTEXT_MENU_ID_DUMMY2:
			Toast.makeText(CameraActivity.this, "dummy2 selected.", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, keyEvent);
		}
		Log.d(TAG, "onKeyDown()");
		CameraActivity.this.finish();
		return true;
	}
}
