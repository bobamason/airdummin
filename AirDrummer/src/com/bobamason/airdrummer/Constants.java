package com.bobamason.airdrummer;

public class Constants {

	public static final int[] SOUND_IDS = { R.raw.bell, R.raw.crash,
			R.raw.hihat1, R.raw.hihat2, R.raw.hit1, R.raw.hit2, R.raw.hit3,
			R.raw.kick1, R.raw.kick2, R.raw.kick3, R.raw.kick4, R.raw.kick5,
			R.raw.rim1, R.raw.rim2, R.raw.snare1, R.raw.snare2, R.raw.snare3,
			R.raw.snare4, R.raw.snare5, R.raw.tom1, R.raw.tom2, R.raw.tom3,
			R.raw.tom4, R.raw.tom5 };

	public static final String[] SOUND_LABELS = { "Bell", "Crash", "Hihat 1",
			"Hithat 2", "Hit 1", "Hit 2", "Hit 3", "Kick 1", "Kick 2",
			"Kick 3", "Kick 4", "Kick 5", "Rim 1", "Rim 2", "Snare 1",
			"Snare 2", "Snare 3", "Snare 4", "Snare 5", "Tom 1", "Tom 2",
			"Tom 3", "Tom 4", "Tom 5" };

	public static final String NUM_DRUM_KEY = "numOfDrums";

	public static final String THRESHOLD_KEY = "threshold";

	public static final String SENSOR_KEY = "sensor";

	public static final String SPAN_KEY = "span";

	public static final String CHANGE_PITCH_KEY = "pitch";

	public static String DRUM_SOUND_POS_KEY(int drum) {
		return "drumSound" + String.valueOf(drum);
	};

	public static final int[] DEFAULT_DRUM_SELECTIONS = { 4, 10, 23, 20, 12 };

	public static final int[] DEFAULT_DRUM_COLORS = { 0xff0099cc, 0xff669900,
			0xffff8800, 0xffcc0000, 0xff9933cc };
}
