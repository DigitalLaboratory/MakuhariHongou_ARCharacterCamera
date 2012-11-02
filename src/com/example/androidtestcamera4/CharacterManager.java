package com.example.androidtestcamera4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;


public class CharacterManager {

	private final static String TAG = CharacterManager.class.getSimpleName();

	private Context context;

	public CharacterManager(Context context) {
		this.context = context;
		getCharacterInfos();
	}

	private List<Map<String, Object>> characterInfoMapList = null;

	/**
	 * get character informations
	 */
	private void getCharacterInfos() {
		try {
			InputStream inputStream = context.getResources().getAssets().open("characters.json");
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
				try {
					characterInfoMapList = JSON.decode(inputStreamReader);
					Log.d(TAG, String.format("%d", characterInfoMapList.size()));
				} finally {
					inputStreamReader.close();
				}
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public Map<String, Object> getCharacterInfo() {
		return null;
	}

	// current character (specific pose)

	// assets/characters/NAME/char123/NN.png ... for thumbnails
	// assets/characters/NAME/char300/NN.png ... for display
	// assets/characters/NAME/char700/NN.png ... for printer
	// assets/characters/NAME/logo/01.png ... for printer
	// assets/characters/NAME/logo/02.png ... for display
	private int characterIndex = 0;
	private int poseIndex = 0;
	private Bitmap bitmap = null;
	private Bitmap scaledBitmap = null;
	private int centerX;
	private int centerY;
	private float scaleFactor = 1.0f;

	public void setCurrentCharacter() {
		characterIndex = 0;
		poseIndex = 0;
		String sPath = "characters/" + characterInfoMapList.get(characterIndex).get("path") + "/cha300/" + String.format("%02d.png", poseIndex + 1);
		InputStream is;
		try {
			is = context.getResources().getAssets().open(sPath);
			bitmap = BitmapFactory.decodeStream(is);
			scaledBitmap = bitmap.copy(bitmap.getConfig(), false);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		centerX = 640;
		centerY = 480;
	}

	public Bitmap getCharacterBitmap() {
//		return bitmap;
		return scaledBitmap;
	}
	public int getCharacterCenterX() {
		return centerX;
	}
	public int getCharacterCenterY() {
		return centerY;
	}
	public void setCharacterCenter(int x, int y) {
		if (bitmap != null) {
			centerX = x;
			centerY = y;
		}
	}

	/**
	 * draw current character bitmap
	 * @param canvas is Canvas object
	 */
	public void draw(Canvas canvas) {
		if (bitmap != null) {
			if (centerX + (scaledBitmap.getWidth() >> 1) < 0
					|| centerX - (scaledBitmap.getWidth() >> 1) > canvas.getWidth()
					|| centerY + (scaledBitmap.getHeight() >> 1) < 0
					|| centerY - (scaledBitmap.getHeight() >> 1) > canvas.getHeight()) {
				// is not necessary to draw
				return;
			}
			Paint paint = new Paint();
			paint.setColor(Color.WHITE); // why white?
			Rect rect = new Rect();
			canvas.drawBitmap(scaledBitmap, centerX - (scaledBitmap.getWidth() >> 1), centerY - (scaledBitmap.getHeight() >> 1), paint);
		}
	}

	public boolean hits(int x, int y) {
		return bitmap != null
				&& centerX - (scaledBitmap.getWidth() >> 1) <= x && x <= centerX + (scaledBitmap.getWidth() >> 1)
				&& centerY - (scaledBitmap.getHeight() >> 1) <= y && y <= centerY + (scaledBitmap.getHeight() >> 1);
	}

	public final static int MODE_STOP = 0; // 画面にて 停れり
	public final static int MODE_FLOAT = 1; // 画面にて 浮遊せり
	public final static int MODE_MOVE = 2; // 画面にて 移れり
	private int characterMode = MODE_STOP;
	public void setCharacterMode(int characterMode) {
		this.characterMode = characterMode;
	}
	public int getCharacterMode() {
		return characterMode;
	}

	public void setScaleFactor(float scaleFactor) {
		if (bitmap != null) {
			this.scaleFactor *= scaleFactor;
			scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * this.scaleFactor), (int)(bitmap.getHeight() * this.scaleFactor), false);
		}
	}
}
