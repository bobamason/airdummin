package com.bobamason.airdrummer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;
import com.bobamason.airdrummer.DrumKit.*;

public class DrumKit {

	private static final float[] identityMatrix = { 1, 0, 0, 0, 1, 0, 0, 0, 1, };

	private float[] calibrateMatrix = identityMatrix.clone();

	private float[] rotationMatrix = new float[9];

	private float[] gravity = new float[3];

	private volatile float[] angles = new float[3];

	private boolean doCalib = false;

	private boolean firstTime = true;

	private Activity activity;

	private SoundPool soundPool;

	private long delay = 100;

	private long currentMillis;

	private long lastHitMillis;

	private float threshold;

	private float linearAccel;

	private ArrayList<Drum> drums = new ArrayList<Drum>();

	private int drumCount;

	private boolean run = false;

	private SharedPreferences prefs;

	private float span;

	private Paint textPaint;

	private float rad2deg = 180 / (float) Math.PI;

	private SharedPreferences.Editor editor;

	private int previewPosition;

	private float lastZ;

	private boolean changePitch;
	
	private volatile int currentView = 0;
	
	private boolean hitLocked = true;

	private DrumKit.OnDrumHitListener mOnDrumHitListener;

	public DrumKit(Activity act) {
		activity = act;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		editor = prefs.edit();
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(20);
		textPaint.setColor(Color.WHITE);

		soundPool
				.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

					@Override
					public void onLoadComplete(SoundPool soundPool,
							int sampleId, int status) {
						for (Drum d : drums) {
							if (d.getSoundID() == sampleId)
								d.setLoaded(true);
						}
					}
				});
	}

	public void toggleView() {
		if(run)
			currentView = (currentView + 1) % 2;
	}
	
	public synchronized int getViewPos(){
		return currentView;
	}

	public void setChangePitch(boolean changePitch) {
		this.changePitch = changePitch;
		editor.putBoolean(Constants.CHANGE_PITCH_KEY, changePitch);
		editor.apply();
	}

	public boolean isChangePitch() {
		return changePitch;
	}

	public void setSpanProgress(int progress) {
		span = progress * 5f + 10f;
		//drumUI.setSpan(span);
		editor.putFloat(Constants.SPAN_KEY, span);
		editor.apply();
	}

	public float getSpan() {
		return span;
	}

	public int getSpanProgress() {
		return (int) (span - 10) / 5;
	}

	public void setThresholdProgress(int progress) {
		this.threshold = 11f - progress;
		editor.putFloat(Constants.THRESHOLD_KEY, threshold);
		editor.apply();
	}

	public float getThreshold() {
		return threshold;
	}

	public int getThresholdProgress() {
		return (int) (11 - threshold);
	}

	public int getSoundPos(int drumNum) {
		return drums.get(drumNum).getSoundPos();
	}

	public void playSoundPreview(int position) {
		previewPosition = position;
		new Thread(new Runnable() {

			@Override
			public void run() {
				MediaPlayer sound = MediaPlayer.create(activity,
						Constants.SOUND_IDS[previewPosition]);
				long duration = sound.getDuration();
				sound.start();
				long end = System.currentTimeMillis() + duration + 300;
				long current = System.currentTimeMillis();
				while (current < end) {
					current = System.currentTimeMillis();
				}
				String str = sound.toString();
				sound.stop();
				sound.reset();
				sound.release();
				sound = null;
				Log.d("MediaPlayer", str + " released");
			}
		}).start();
	}

	private void setUpDrums() {
		drumCount = drumCount > 5 ? 5 : drumCount;
		for (int i = 0; i < drumCount; i++) {
			int soundPos = prefs.getInt(Constants.DRUM_SOUND_POS_KEY(i),
					Constants.DEFAULT_DRUM_SELECTIONS[i]);
			drums.add(new Drum(soundPos));
		}
	}

	public void setDrumSoundPos(int d, int pos) {
		Drum drum = drums.get(d);
		drum.unload();
		drum.setSoundPos(pos);
		editor.putInt(Constants.DRUM_SOUND_POS_KEY(d), pos);
		editor.apply();
	}

	public int getCount() {
		return drumCount;
	}

	public void setDrumCount(int drumCount) {
		this.drumCount = drumCount;
		//drumUI.setCount(drumCount);
		drums.clear();
		setUpDrums();
		editor.putInt(Constants.NUM_DRUM_KEY, this.drumCount);
		editor.apply();
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public void setSensorEvent(SensorEvent event) {
		if(!run)return;
		int type = event.sensor.getType();

		if (type == Sensor.TYPE_ACCELEROMETER) {
			float alpha = 0.8f;
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			linearAccel = event.values[2] - gravity[2];

		}

		if (type == Sensor.TYPE_ROTATION_VECTOR) {
			if (doCalib || firstTime) {
				SensorManager.getRotationMatrixFromVector(calibrateMatrix,
						event.values);
				doCalib = false;
				firstTime = false;
			}

			SensorManager.getRotationMatrixFromVector(rotationMatrix,
					event.values);

			SensorManager
					.getAngleChange(angles, rotationMatrix, calibrateMatrix);

			angles[0] = angles[0] * rad2deg;
			float min = (-span * drumCount * 0.5f) + (span * 0.5f);
			float max = (span * drumCount * 0.5f) - (span * 0.5f);
			if (angles[0] < min) {
				angles[0] = min;
			} else if (angles[0] > max) {
				angles[0] = max;
			}
			angles[1] = angles[1] * rad2deg;
			if(angles[1] > 0)angles[1] = 0;
			angles[2] = angles[2] * rad2deg;
			if(angles[1] < -10)hitLocked = false;
			lastZ = angles[0];

			currentMillis = System.currentTimeMillis();
			if (soundPool != null && run) {
				boolean isHit = !hitLocked
					&& angles[1] > -10;

				if (isHit) {
					hitLocked = true;
					for (int i = 0; i < drumCount; i++) {
						drums.get(i).hit(i, angles[0]);
					}
					lastHitMillis = currentMillis;
				}
			} else if (soundPool == null) {
				Log.e("SoundPool", "soundpoll is null");
			}
		}
	}

	public synchronized float[] getAngles() {
		return angles.clone();
	}

	public Drum getDrum(int pos) {
		return drums.get(pos);
	}

	public void onResume() {
		drumCount = prefs.getInt(Constants.NUM_DRUM_KEY, 5);
		//span = prefs.getFloat(Constants.SPAN_KEY, 20);
		span = 20;
		threshold = prefs.getFloat(Constants.THRESHOLD_KEY, 6);
		changePitch = prefs.getBoolean(Constants.CHANGE_PITCH_KEY, true);
		setUpDrums();
	}

	public void onPause() {
		soundPool.autoPause();
	}

	public void onDestroy() {
		unloadAllDrums();
		if (soundPool != null)
			soundPool.release();
	}

	private void unloadAllDrums() {
		for (Drum d : drums) {
			d.unload();
		}
	}

	public void doCalibrate() {
		doCalib = true;
	}

	public class Drum {

		private int soundID;

		private boolean loaded = false;

		private int soundPos;

		public Drum(int soundPos) {
			this.soundPos = soundPos;
			int resID = Constants.SOUND_IDS[soundPos];
			soundID = soundPool.load(activity, resID, 1);
		}

		public int getSoundPos() {
			return soundPos;
		}

		public void setSoundPos(int pos) {
			soundPos = pos;
			unload();
			int resID = Constants.SOUND_IDS[pos];
			soundID = soundPool.load(activity, resID, 1);
		}

		public boolean isLoaded() {
			return loaded;
		}

		public void setLoaded(boolean loaded) {
			this.loaded = loaded;
		}

		public int getSoundID() {
			return soundID;
		}

		public void hit(int pos, float angle) {
			float start = (-span * drumCount * 0.5f) + pos * span;
			float end = (-span * drumCount * 0.5f) + (pos + 1) * span;
			if (loaded && angle > start && angle < end) {
				soundPool.play(soundID, 1, 1, 1, 0, 1.0f);
				if(mOnDrumHitListener != null)mOnDrumHitListener.drumHit(pos);
			}
		}

		private float pitch() {
			float a = -angles[1];
			float min = -5;
			float max = 70;
			a = a > max ? max : a;
			a = a < min ? min : a;
			float step = (1.7f - 0.7f) / (max - min);
			return 0.7f + a * step;
		}

		public void unload() {
			if (loaded) {
				soundPool.unload(soundID);
				loaded = false;
			}
		}
	}
	
	public void setOnDrumHitListener(DrumKit.OnDrumHitListener listener){
		mOnDrumHitListener = listener;
	}
	
	public static abstract class OnDrumHitListener{
		public abstract void drumHit(int pos);
	}
}
