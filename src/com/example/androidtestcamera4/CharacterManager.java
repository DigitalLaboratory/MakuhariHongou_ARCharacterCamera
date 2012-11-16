package com.example.androidtestcamera4;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.arnx.jsonic.JSON;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
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

	public List<Map<String, Object>> getCharacterInfoMapList() {
		return characterInfoMapList;
	}

	public Map<String, Object> getCharacterInfo() {
		if (characterInfoMapList != null) {
			if (characterIndex >= 0) {
				return characterInfoMapList.get(characterIndex);
			}
		}
		return null;
	}

	public String getPlace() {
		if (characterInfoMapList != null) {
			if (characterIndex >= 0) {
				Map<String, Object> characterInfoMap = characterInfoMapList.get(characterIndex);
				if (characterInfoMap.get("place") != null) {
					return (String)characterInfoMap.get("place");
				}
			}
		}
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
	private Bitmap logoBitmap = null;
	private int centerX;
	private int centerY;
	private float scaleFactor = 0.9f;

	public void setCurrentCharacter() {
		characterIndex = MyApplication.undergroundCharacterIndex;
		boolean logoOnly = characterInfoMapList.get(characterIndex).get("logoonly") != null;
		String sPath = null;
		for (;  ;  ) {
			if (logoOnly) {
				break;
			}
			poseIndex = (int)Math.floor(Math.random() * 12);
			sPath = "characters/" + characterInfoMapList.get(characterIndex).get("path") + "/cha300/" + String.format("%02d.png", poseIndex + 1);
			try {
				InputStream is = context.getResources().getAssets().open(sPath);
				is.close();
			} catch (IOException e) {
				continue;
			}
			break;
		}
		InputStream is;
		try {
			if (!logoOnly) {
				is = context.getResources().getAssets().open(sPath);
				try {
					bitmap = BitmapFactory.decodeStream(is);
				} finally {
					is.close();
				}
			}
			setScaleFactor(1.0f);

			sPath = "characters/" + characterInfoMapList.get(characterIndex).get("path") + "/logo/02.png";
			is = context.getResources().getAssets().open(sPath);
			try {
				logoBitmap = BitmapFactory.decodeStream(is);
			} finally {
				is.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public Bitmap getLogoBitmapForPrinting() {
		Bitmap logoBitmap2 = null;
		if (logoBitmap != null) {
			String sPath = "characters/" + characterInfoMapList.get(characterIndex).get("path") + "/logo/01.png";
			InputStream is;
			try {
				is = context.getResources().getAssets().open(sPath);
				try {
					logoBitmap2 = BitmapFactory.decodeStream(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				logoBitmap2 = null;
			}
		}
		return logoBitmap2;
	}

	public Bitmap getCharacterBitmapForPrinting() {
		Bitmap characterBitmap = null;
		if (bitmap != null) {
			String sPath = "characters/" + characterInfoMapList.get(characterIndex).get("path") + "/cha700/" + String.format("%02d.png", poseIndex + 1);
			InputStream is;
			try {
				is = context.getResources().getAssets().open(sPath);
				try {
					characterBitmap = BitmapFactory.decodeStream(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				characterBitmap = null;
			}
		}
		return characterBitmap;
	}

	public Bitmap getCharacterScaledBitmap() {
		return scaledBitmap;
	}
	public int getCharacterCenterX() {
		return centerX;
	}
	public int getCharacterCenterY() {
		return centerY;
	}
	public void setCharacterCenter(int screenWidth, int screenHeight, int x, int y) {
		if (bitmap != null) {
			centerX = x;
			centerY = y;
			// scaleFactor を如何に定むるか
		}
	}

	/**
	 * draw current character bitmap
	 * @param canvas is Canvas object
	 */
	public void draw(Canvas canvas) {
		if (bitmap != null) {
			// キャラクターの描画
			if (centerX + (scaledBitmap.getWidth() >> 1) < 0
					|| centerX - (scaledBitmap.getWidth() >> 1) > canvas.getWidth()
					|| centerY + (scaledBitmap.getHeight() >> 1) < 0
					|| centerY - (scaledBitmap.getHeight() >> 1) > canvas.getHeight()) {
				// is not necessary to draw
				return;
			}
			Paint paint = new Paint();
			paint.setColor(Color.WHITE); // why white?
			canvas.drawBitmap(scaledBitmap, centerX - (scaledBitmap.getWidth() >> 1), centerY - (scaledBitmap.getHeight() >> 1), paint);
		}
		if (logoBitmap != null) {
			// ロゴの描画
			Rect sourceRect = new Rect(0, 0, logoBitmap.getWidth(), logoBitmap.getHeight());
			Rect destinationRect = new Rect();
			destinationRect.left = 10 + 0 + 80;
			destinationRect.top = 10 + 0;
			destinationRect.right = 10 + canvas.getWidth() * 277 / 640 + 80;
			destinationRect.bottom = 10 + (destinationRect.right - 90)* 56 / 227;
			canvas.drawBitmap(logoBitmap, sourceRect, destinationRect, null);
		}
		if (bitmap != null || logoBitmap != null) {
			String sNotice = (String)characterInfoMapList.get(characterIndex).get("copyright");
			if (sNotice != null && !sNotice.equals("")) {
				Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				textPaint.setColor(Color.WHITE);
				textPaint.setTextSize(16.0f);
				textPaint.setShadowLayer(4, 2, 2, Color.BLACK);
				FontMetrics fontMetrics = textPaint.getFontMetrics();
				int height = (int)(fontMetrics.bottom - fontMetrics.top);
				int width = (int)textPaint.measureText(sNotice);
				canvas.drawText(sNotice, canvas.getWidth() - width - height - 80, canvas.getHeight() - 12, textPaint);
			}
		}
	}

	public boolean hits(int x, int y) {
		return bitmap != null
				&& centerX - (scaledBitmap.getWidth() >> 1) <= x && x <= centerX + (scaledBitmap.getWidth() >> 1)
				&& centerY - (scaledBitmap.getHeight() >> 1) <= y && y <= centerY + (scaledBitmap.getHeight() >> 1);
	}

	public boolean isOnScreen(int screenWidth, int screenHeight, int x, int y) {
		return bitmap != null
				&& x + (scaledBitmap.getWidth() >> 1) > 0
				&& x - (scaledBitmap.getWidth() >> 1) < screenWidth
				&& y + (scaledBitmap.getHeight() >> 1) > 0
				&& y - (scaledBitmap.getHeight() >> 1) < screenHeight;
	}

	public final static int MODE_STOP = 0; // 画面にて 停れり
	public final static int MODE_FLOAT = 1; // 画面にて 浮遊せり
	public final static int MODE_MOVE = 2; // 画面にて 移れり
	private int characterMode = MODE_STOP;
	public void setCharacterMode(int characterMode) {
		Log.d(TAG, "setCharacterMode:" + characterMode);
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

	private int targetX;
	private int targetY;
	private int movingSpeed;

	public void startMoving(int screenWidth, int screenHeight, int movingSpeed) {
		Log.d(TAG, "startMoving!");
		targetX = screenWidth >> 1;
		targetY = screenHeight >> 1;
		this.movingSpeed = movingSpeed;

		stopMoving();
		setCharacterMode(MODE_MOVE);
	}

	public void stopMoving() {
		setCharacterMode(MODE_FLOAT);
	}

	public boolean nextMoving() {
		if (centerX == targetX && centerY == targetY) {
			return false;
		}
		double distance = Math.hypot(targetX - centerX, targetY - centerY);
		if (distance <= movingSpeed) {
			centerX = targetX;
			centerY = targetY;
		} else {
			centerX -= (centerX - targetX) * movingSpeed / distance;
			centerY -= (centerY - targetY) * movingSpeed / distance;
		}
		return true;
	}
}
