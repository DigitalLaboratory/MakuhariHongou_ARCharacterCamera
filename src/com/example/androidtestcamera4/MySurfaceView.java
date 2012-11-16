package com.example.androidtestcamera4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

	private final static String TAG = MySurfaceView.class.getSimpleName();

	private Context context;
	private Handler handler = new Handler();
	private SurfaceHolder surfaceHolder;
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;

	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		Log.d(TAG, "MySurfaceView: " + this);

		surfaceHolder = this.getHolder();
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		surfaceHolder.addCallback(this);

		MyApplication.characterManager.setCurrentCharacter();

		gestureDetector = new GestureDetector(context, this);
		scaleGestureDetector = new ScaleGestureDetector(context, this);

	}

	private int screenWidth;
	private int screenHeight;

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, String.format("surfaceChanged: %d, %d x %d", format, width, height));
		screenWidth = width;
		screenHeight = height;

		MyApplication.characterManager.setCurrentCharacter();

MyApplication.characterManager.startMoving(screenWidth, screenHeight, screenWidth / (640 / 12));

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated()!");
		surfaceHolder = holder;
		MyApplication.sensors.start();
		MyApplication.sensors.addOrientationListener(orientationListener);
		MyApplication.sensors.addAccelerometerListener(accelerometerListener);
		MyApplication.sensors.addGyroscopeListener(gyroscopeListener);

		if (movingTimer != null) {
			movingTimer.cancel();
			movingTimer.purge();
			movingTimer = null;
		}
		movingTimer = new Timer(true);
		movingTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (MyApplication.characterManager.getCharacterMode() == CharacterManager.MODE_MOVE) {
					if (!MyApplication.characterManager.nextMoving()) {
						// complete
						Log.d(TAG, "moving complete");
						MyApplication.characterManager.stopMoving();
					}
					handler.post(new Runnable() {
						@Override
						public void run() {
							MySurfaceView.this.invalidate();
						}
					});
				}
			}
		}, 100, 100);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed()!");
		movingTimer.cancel();
		movingTimer.purge();
		movingTimer = null;

		MyApplication.sensors.removeGyroscopeListener(gyroscopeListener);
		MyApplication.sensors.removeAccelerometerListener(accelerometerListener);
		MyApplication.sensors.removeOrientationListener(orientationListener);
		MyApplication.sensors.stop();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		onDraw(surfaceHolder);
	}

	private static PorterDuffXfermode porterDuffXfermodeSrc = new PorterDuffXfermode(PorterDuff.Mode.SRC);

	private Bitmap locationBitmap = null;
	private Paint locationPaint = null;
	private Bitmap dateBitmap = null;
	private Paint datePaint = null;

	private void onDraw(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas(null);
		if (canvas != null) {
			try {
				Paint paint = new Paint();
				paint.setXfermode(porterDuffXfermodeSrc);
				paint.setARGB(0, 0, 0, 0);
				canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
				MyApplication.characterManager.draw(canvas);
				if (drawLocation) {
					if (locationBitmap == null) {
						String sLocation = MyApplication.characterManager.getPlace();
						if (sLocation == null) {
							String[] sAddressLines = MyApplication.sensors.getAddressLines();
							if (sAddressLines != null && sAddressLines.length > 0) {
								sLocation = "";
								for (String sAddressLine: sAddressLines) {
									sLocation += sAddressLine + " ";
								}
							}
						}
						if (sLocation != null) {
							locationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
							locationPaint.setColor(Color.WHITE);
							locationPaint.setTextSize(24.0f);
							locationPaint.setShadowLayer(4,  2,  2,  Color.BLACK);
							FontMetrics fontMetrics = locationPaint.getFontMetrics();
							int height = (int)(fontMetrics.bottom - fontMetrics.top);
							int width = (int)locationPaint.measureText(sLocation);
							locationBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
							Canvas canvas2 = new Canvas(locationBitmap);
							canvas2.drawText(sLocation, 0, -fontMetrics.top, locationPaint);
						}
					}
//					canvas.drawText(sAddress, height, canvas.getHeight() - 12 - 24, textPaint);
					if (locationBitmap != null) {
						canvas.drawBitmap(locationBitmap, (int)(locationPaint.getFontMetricsInt().bottom - locationPaint.getFontMetrics().top) + 80, canvas.getHeight() - 12 - 24 + locationPaint.getFontMetrics().top, null);
					}
				}
				if (drawDate) {
					if (dateBitmap == null) {
						String sDate = new SimpleDateFormat(context.getResources().getString(R.string.DATE_FORMAT)).format(new Date());
						datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
						datePaint.setColor(Color.WHITE);
						datePaint.setTextSize(24.0f);
						datePaint.setShadowLayer(4, 2, 2, Color.BLACK);
						FontMetrics fontMetrics = datePaint.getFontMetrics();
						int height = (int)(fontMetrics.bottom - fontMetrics.top);
						int width = (int)datePaint.measureText(sDate);
						if (dateBitmap == null) {
							dateBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
							Canvas canvas2 = new Canvas(dateBitmap);
							canvas2.drawText(sDate, 0, -fontMetrics.top, datePaint);
						}
					}
//					canvas.drawText(sDate, canvas.getWidth() - width - height, canvas.getHeight() - 12 - 12 - 24, textPaint);
					if (dateBitmap != null) {
						canvas.drawBitmap(dateBitmap, canvas.getWidth() - dateBitmap.getWidth() - (int)(datePaint.getFontMetrics().bottom - datePaint.getFontMetrics().top) - 80, canvas.getHeight() - 12 - 12 - 24 + datePaint.getFontMetrics().top, datePaint);
					}
				}
			} finally {
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	private boolean drawLocation = true;
	public void setDrawLocation(boolean flag) {
		drawLocation = flag;
		MySurfaceView.this.invalidate();
	}
	private boolean drawDate = true;
	public void setDrawDate(boolean flag) {
		drawDate = flag;
		MySurfaceView.this.invalidate();
	}

	/**
	 * View's onTouchEvent()
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = scaleGestureDetector.onTouchEvent(event);
		if (scaleGestureDetector.isInProgress()) {
			return result;
		}
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	// GestureDetector.OnGestureListener

	@Override
	public boolean onDown(MotionEvent e) {
		Log.d(TAG, "onDown()");
		return true;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
		Log.d(TAG, "onFling()");
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.d(TAG, "onLongPress()!");
		// 長押し
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
		// event1 ... The first down motion event that started the scrolling.
		// event2 ... The move motion event that triggered the current onScroll.
		switch (MyApplication.characterManager.getCharacterMode()) {
		case CharacterManager.MODE_STOP:
		case CharacterManager.MODE_FLOAT:
			int x = (int)event2.getX();
			int y = (int)event2.getY();
			if (MyApplication.characterManager.hits(x, y)) {
				MyApplication.characterManager.setCharacterCenter(screenWidth, screenHeight, x, y);
				MyApplication.vibrator.vibrate(5);
				MySurfaceView.this.invalidate();
			}
			break;
		case CharacterManager.MODE_MOVE:
			break;
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		Log.d(TAG, "onShowPress()");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		Log.d(TAG, "onSingleTapUp()");
		int x = (int)event.getX();
		int y = (int)event.getY();
		if (MyApplication.characterManager.hits(x, y)) {
			switch (MyApplication.characterManager.getCharacterMode()) {
			case CharacterManager.MODE_STOP:
				MyApplication.characterManager.setCharacterMode(CharacterManager.MODE_FLOAT);
				Toast.makeText(context, "浮遊モード", Toast.LENGTH_SHORT).show();
				break;
			case CharacterManager.MODE_FLOAT:
				MyApplication.characterManager.setCharacterMode(CharacterManager.MODE_STOP);
				Toast.makeText(context, "停止モード", Toast.LENGTH_SHORT).show();
				break;
			case CharacterManager.MODE_MOVE:
				// ignore
				break;
			}
		}
		return true;
	}

	// GestureDetector.OnDoubleTapListener

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		return false;
	}
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return true;
	}
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return true;
	}

	// ScaleGestureDetector.OnScaleGestureListener

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		switch (MyApplication.characterManager.getCharacterMode()) {
		case CharacterManager.MODE_STOP:
			float scaleFactor = detector.getScaleFactor();
			MyApplication.characterManager.setScaleFactor(scaleFactor);
			MyApplication.vibrator.vibrate(5);
			MySurfaceView.this.invalidate();
			break;
		case CharacterManager.MODE_FLOAT:
		case CharacterManager.MODE_MOVE:
			break;
		}
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.d(TAG, "onScaleBegin()");
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.d(TAG, "onScaleEnd()");
	}

	private Timer movingTimer = null;

	private TimerTask movingTimerTask = new TimerTask() {
		@Override
		public void run() {
			if (MyApplication.characterManager.getCharacterMode() == CharacterManager.MODE_MOVE) {
				if (!MyApplication.characterManager.nextMoving()) {
					// complete
					Log.d(TAG, "moving complete");
					MyApplication.characterManager.stopMoving();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						MySurfaceView.this.invalidate();
					}
				});
			}
		}
	};

	private Sensors.OrientationListener orientationListener = new Sensors.OrientationListener() {
		@Override
		public void onEvent(float[] orientationValues) {
//			int[] values = new int[3];
//			values[0] = (int)Math.toDegrees(orientationValues[0]);
//			values[1] = (int)Math.toDegrees(orientationValues[1]);
//			values[2] = (int)Math.toDegrees(orientationValues[2]);
//			Log.d(TAG, String.format("orientation: %d, %d, %d", values[0], values[1], values[2]));
		}
	};

	private float[] previousAccelerometerValues = null;

	private Sensors.AccelerometerListener accelerometerListener = new Sensors.AccelerometerListener() {
		@Override
		public void onEvent(float[] accelerometerValues) {
			if (MyApplication.characterManager.getCharacterMode() == CharacterManager.MODE_FLOAT) {
				if (previousAccelerometerValues != null) {
					float value = accelerometerValues[2] - previousAccelerometerValues[2];
					value /= 10.0f;
					if (value > 1.0f) {
						value = 1.0f; 
					} else if (value < -1.0f) {
						value = -1.0f;
					}
					value += 1.0f;
//					MyApplication.characterManager.setScaleFactor(value);
				}
				previousAccelerometerValues = accelerometerValues.clone();
			}
		}
	};

	private Sensors.GyroscopeListener gyroscopeListener = new Sensors.GyroscopeListener() {
		@Override
		public void onEvent(float[] gyroscopeValues) {
			if (MyApplication.characterManager.getCharacterMode() == CharacterManager.MODE_FLOAT) {
				int[] values = new int[3];
				values[0] = (int)Math.toDegrees(gyroscopeValues[0]) / 3 * 3;
				values[1] = (int)Math.toDegrees(gyroscopeValues[1]) / 3 * 3;
				values[2] = (int)Math.toDegrees(gyroscopeValues[2]) / 3 * 3;
				// 上述 / 2 * 2 は Nexus S 対策。 如何にしても ゴミ 入りぬ。
				// 上限・下限を設く
				if (values[0] < -60) {
					values[0] = -60;
				} else if (values[0] > 60) {
					values[0] = 60;
				}
				if (values[1] < -60) {
					values[1] = -60;
				} else if (values[1] > 60) {
					values[1] = 60;
				}
				if (values[2] < -60) {
					values[2] = -60;
				} else if (values[2] > 60) {
					values[2] = 60;
				}
//				Log.d(TAG, String.format("gyro: %d, %d, %d", values[0], values[1], values[2]));

				// 960px ほどにて * 1.0 の
				double factor = screenWidth / 960.0;
				factor = factor * 960.0 / 800.0;
//				factor = screenHeight / 720.0;
				factor = factor * factor * factor * factor;
				int x = MyApplication.characterManager.getCharacterCenterX() + (int)(values[0] * factor);
				int y = MyApplication.characterManager.getCharacterCenterY() - (int)(values[1] * factor);
				MyApplication.characterManager.setCharacterCenter(screenWidth, screenHeight, x, y);
				MySurfaceView.this.invalidate();
				if (!MyApplication.characterManager.isOnScreen(screenWidth,  screenHeight, x, y)) {
					MyApplication.characterManager.setCurrentCharacter();
					MyApplication.characterManager.setCharacterMode(CharacterManager.MODE_MOVE);
					MyApplication.characterManager.startMoving(screenWidth, screenHeight, screenWidth / (640 / 12));
				}
			}
		}
	};
}
