package com.example.androidtestcamera4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
	private SurfaceHolder surfaceHolder;
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;

	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

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
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		onDraw(surfaceHolder);
	}

	private static PorterDuffXfermode porterDuffXfermodeSrc = new PorterDuffXfermode(PorterDuff.Mode.SRC);

	private void onDraw(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas(null);
		if (canvas != null) {
			try {
				Paint paint = new Paint();
				paint.setXfermode(porterDuffXfermodeSrc);
				paint.setARGB(0, 0, 0, 0);
				canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
				MyApplication.characterManager.draw(canvas);
			} finally {
				holder.unlockCanvasAndPost(canvas);
			}
		}
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
			int x = (int)Math.floor(event2.getX());
			int y = (int)Math.floor(event2.getY());
			if (MyApplication.characterManager.hits(x, y)) {
				MyApplication.characterManager.setCharacterCenter(x, y);
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
	public boolean onSingleTapUp(MotionEvent e) {
		Log.d(TAG, "onSingleTapUp()");
		return true;
	}

	// GestureDetector.OnDoubleTapListener

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		int x = (int)Math.floor(event.getX());
		int y = (int)Math.floor(event.getY());
		if (MyApplication.characterManager.hits(x, y)) {
			switch (MyApplication.characterManager.getCharacterMode()) {
			case CharacterManager.MODE_STOP:
				MyApplication.characterManager.setCharacterMode(CharacterManager.MODE_FLOAT);
				Toast.makeText(context, "STOP -> FLOAT", Toast.LENGTH_SHORT).show();
				break;
			case CharacterManager.MODE_FLOAT:
				MyApplication.characterManager.setCharacterMode(CharacterManager.MODE_STOP);
				Toast.makeText(context, "FLOAT -> STOP", Toast.LENGTH_SHORT).show();
				break;
			case CharacterManager.MODE_MOVE:
				// ignore
				break;
			}
		}
		return true;
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
}
