package com.bobamason.airdrummer;

import java.util.List;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

import android.app.FragmentTransaction;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements
		SensorEventListener {

	private SensorManager mSensorManager;

	private static final String DRUM_FRAG = "drumfrag1";

	private static final String SETTINGS_FRAG = "settingdfrag1";

	private static final String CHOOSE_SOUND_FRAG = "chooseSoundFrag";

	private static final String deviceId = "C9605E52BE66F514E81CC5AF54C72EBE";

	private android.app.FragmentManager fragmentManager;

	private DrumKit drumKit;

	private ActionBar actionBar;

	private SharedPreferences prefs;

	private boolean firstTime;

	private Sensor mRotationVectorSensor;

	private List<Sensor> sensorList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fragmentManager = getFragmentManager();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		firstTime = prefs.getBoolean("firsttime1", true);

		mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		drumKit = new DrumKit(this);

		actionBar = getSupportActionBar();
		//actionBar.setIcon(new ColorDrawable(Color.TRANSPARENT));
		actionBar.setHomeAsUpIndicator(R.drawable.ic_action_previous_item);

		if (firstTime)
			addInstructionsFragment();
		else
			addDrumFragment();

//		AdView adView = (AdView) this.findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().addTestDevice(deviceId)
//				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
//		adView.loadAd(adRequest);
	}

	private void addInstructionsFragment() {
		InstructionsFragment fragment = new InstructionsFragment();
		fragment.setOnCompletedListener(new InstructionsFragment.OnCompleteListener() {
			@Override
			public void complete() {
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(R.id.fragment_holder, new DrumFragment(),
						DRUM_FRAG);
				transaction.commit();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("firsttime1", false);
				editor.apply();
			}
		});
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.fragment_holder, fragment);
		transaction.commit();
	}

	private void addDrumFragment() {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.fragment_holder, new DrumFragment(), DRUM_FRAG);
		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_layout, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		int fragcount = fragmentManager.getBackStackEntryCount();
		if (fragcount > 0) {
			if (fragcount == 1) {
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setTitle(R.string.app_name);
			} else if (fragcount == 2) {
				actionBar.setTitle(R.string.settings);
			}
			fragmentManager.popBackStack();
		} else
			super.onBackPressed();
	}

	@Override
	public boolean onSupportNavigateUp() {
		int fragcount = fragmentManager.getBackStackEntryCount();
		if (fragcount > 0) {
			if (fragcount == 1) {
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setTitle(R.string.app_name);
			} else if (fragcount == 2) {
				actionBar.setTitle(R.string.settings);
			}
			fragmentManager.popBackStack();
			return true;
		} else
			return super.onSupportNavigateUp();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings
				&& (fragmentManager.findFragmentByTag(SETTINGS_FRAG) == null || !fragmentManager
						.findFragmentByTag(SETTINGS_FRAG).isVisible())) {
			if (fragmentManager.findFragmentByTag(CHOOSE_SOUND_FRAG) != null
					&& fragmentManager.findFragmentByTag(CHOOSE_SOUND_FRAG)
							.isVisible()) {
				fragmentManager.popBackStack();
				actionBar.setTitle(R.string.settings);
				return true;
			}
			FragmentTransaction transaction = fragmentManager
					.beginTransaction();
			SettingsFragment frag = new SettingsFragment();
			frag.setSettingsListener(new SettingsFragment.SettingsListener() {

				@Override
				public void onDrumClicked(int drum) {
					Bundle args = new Bundle();
					args.putInt("drum", drum);
					ChooseSoundFragment frag = new ChooseSoundFragment();
					frag.setArguments(args);
					FragmentTransaction trans = fragmentManager
							.beginTransaction();
					trans.setCustomAnimations(android.R.animator.fade_in,
							android.R.animator.fade_out,
							android.R.animator.fade_in,
							android.R.animator.fade_out);
					trans.replace(R.id.fragment_holder, frag, CHOOSE_SOUND_FRAG);
					trans.addToBackStack(null);
					trans.commit();
					actionBar.setTitle(R.string.choose_sound);
				}
			});
			transaction.setCustomAnimations(android.R.animator.fade_in,
					android.R.animator.fade_out, android.R.animator.fade_in,
					android.R.animator.fade_out);
			transaction.replace(R.id.fragment_holder, frag, SETTINGS_FRAG);
			transaction.addToBackStack(null);
			transaction.commit();
			actionBar.setTitle(R.string.settings);
			actionBar.setDisplayHomeAsUpEnabled(true);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	public void setSensorPos(int pos) {
		if (pos != prefs.getInt(Constants.SENSOR_KEY, 0)) {
			mSensorManager.unregisterListener(this, mRotationVectorSensor);
			mRotationVectorSensor = sensorList.get(pos);
			if (mRotationVectorSensor != null) {
				mSensorManager.registerListener(this, mRotationVectorSensor,
						SensorManager.SENSOR_DELAY_GAME);
				Editor editor = prefs.edit();
				editor.putInt(Constants.SENSOR_KEY, pos);
				editor.apply();
			}
		}
	}

	public List<Sensor> getSensorList() {
		return sensorList;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
			sensorList = mSensorManager
					.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
			mRotationVectorSensor = sensorList.get(prefs.getInt(
					Constants.SENSOR_KEY, 0));
			mSensorManager.registerListener(this, mRotationVectorSensor,
					SensorManager.SENSOR_DELAY_GAME);
		} else {
			Log.e("sensor", "no rotation vector sensor is available");
		}
		drumKit.onResume();
	}

	@Override
	protected void onPause() {
		drumKit.onPause();
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		drumKit.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		drumKit.setSensorEvent(event);
	}

	@Override
	public void onAccuracyChanged(Sensor p1, int p2) {
		// TODO: Implement this method
	}

	public DrumKit getDrumKit() {
		return drumKit;
	}
}
