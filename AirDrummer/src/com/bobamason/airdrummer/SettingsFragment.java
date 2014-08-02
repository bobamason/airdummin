package com.bobamason.airdrummer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	private Button upCountButton, downCountButton;

	Button d1, d2, d3, d4, d5;

	SeekBar spanBar, sensitivityBar;

	private ArrayList<Button> drumButtons = new ArrayList<Button>();

	private TextView drumCountText, spanText, sensitivityText;

	private Switch pitchSwitch;

	private Activity activity;

	private DrumKit drumKit;

	private SettingsListener mSettingsListener = null;

	private Spinner sensorSpinner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, container,
				false);

		downCountButton = (Button) view
				.findViewById(R.id.down_btn_count_settings);
		upCountButton = (Button) view.findViewById(R.id.up_btn_count_settings);
		upCountButton.setOnClickListener(countOnClickListener);
		drumCountText = (TextView) view
				.findViewById(R.id.drum_count_text_settings);
		downCountButton.setOnClickListener(countOnClickListener);

		d1 = (Button) view.findViewById(R.id.drum_btn1);
		d1.setOnClickListener(drumOnClickListener);
		drumButtons.add(0, d1);

		d2 = (Button) view.findViewById(R.id.drum_btn2);
		d2.setOnClickListener(drumOnClickListener);
		drumButtons.add(1, d2);

		d3 = (Button) view.findViewById(R.id.drum_btn3);
		d3.setOnClickListener(drumOnClickListener);
		drumButtons.add(2, d3);

		d4 = (Button) view.findViewById(R.id.drum_btn4);
		d4.setOnClickListener(drumOnClickListener);
		drumButtons.add(3, d4);

		d5 = (Button) view.findViewById(R.id.drum_btn5);
		d5.setOnClickListener(drumOnClickListener);
		drumButtons.add(4, d5);

		pitchSwitch = (Switch) view.findViewById(R.id.pitch_switch);

		spanBar = (SeekBar) view.findViewById(R.id.span_seekbar);
		sensitivityBar = (SeekBar) view.findViewById(R.id.sensitivity_seekbar);

		spanText = (TextView) view.findViewById(R.id.span_text);
		sensitivityText = (TextView) view.findViewById(R.id.sensitivity_text);

		sensorSpinner = (Spinner) view.findViewById(R.id.sensor_spinner);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		drumKit = ((MainActivity) activity).getDrumKit();
		drumCountText.setText(drumKit.getCount() + " drums");

		for (int i = 0; i < drumKit.getCount(); i++) {
			drumButtons.get(i).setVisibility(View.VISIBLE);
		}

		pitchSwitch.setChecked(drumKit.isChangePitch());
		pitchSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton p1,
							boolean checked) {
						drumKit.setChangePitch(checked);
					}
				});

		spanBar.setProgress(drumKit.getSpanProgress());
		spanText.setText("Drum Span: " + drumKit.getSpan() + " degrees");
		spanBar.setOnSeekBarChangeListener(seekListener);

		sensitivityBar.setProgress(drumKit.getThresholdProgress());
		sensitivityText.setText("Drum Hit Sensitivity: "
				+ String.valueOf(drumKit.getThresholdProgress() + 1));
		sensitivityBar.setOnSeekBarChangeListener(seekListener);

		List<Sensor> sensorList = ((MainActivity) activity).getSensorList();
		ArrayList<String> sensors = new ArrayList<String>();

		for (Sensor sensor : sensorList) {
			sensors.add(sensor.getVendor() + " " + sensor.getName());
		}

		sensorSpinner.setSelection(PreferenceManager
				.getDefaultSharedPreferences(activity).getInt(
						Constants.SENSOR_KEY, 0));
		sensorSpinner.setAdapter(new ArrayAdapter<>(activity,
				R.layout.spinner_item, sensors));
		sensorSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						((MainActivity) activity).setSensorPos(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
	}

	public void setSettingsListener(SettingsListener mSettingsListener) {
		this.mSettingsListener = mSettingsListener;
	}

	private void setDrumButtonVisiblity() {
		int i = 0;
		int num = drumKit.getCount();
		for (Button b : drumButtons) {
			i++;
			if (b.getVisibility() == View.VISIBLE) {
				if (i > num) {
					b.setVisibility(View.GONE);
				}
			} else if (b.getVisibility() == View.GONE) {
				if (i <= num) {
					b.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private View.OnClickListener countOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int count = drumKit.getCount();

			if (v.getId() == R.id.down_btn_count_settings && count > 1) {
				drumKit.setDrumCount(count - 1);
			}
			if (v.getId() == R.id.up_btn_count_settings && count < 5) {
				drumKit.setDrumCount(count + 1);
			}
			drumCountText.setText(drumKit.getCount() + " drums");
			setDrumButtonVisiblity();
		}
	};

	private View.OnClickListener drumOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.drum_btn1:
				openChooseSound(0);
				break;
			case R.id.drum_btn2:
				if (drumKit.getCount() > 1)
					openChooseSound(1);
				break;
			case R.id.drum_btn3:
				if (drumKit.getCount() > 2)
					openChooseSound(2);
				break;
			case R.id.drum_btn4:
				if (drumKit.getCount() > 3)
					openChooseSound(3);
				break;
			case R.id.drum_btn5:
				if (drumKit.getCount() > 4)
					openChooseSound(4);
				break;
			default:
				break;
			}
		}

		private void openChooseSound(int d) {
			if (mSettingsListener != null) {
				mSettingsListener.onDrumClicked(d);
			}
		}
	};

	private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekbar, int progress, boolean p3) {
			if (seekbar.getId() == R.id.span_seekbar) {
				drumKit.setSpanProgress(progress);
				spanText.setText("Drum Span: " + drumKit.getSpan() + " degrees");
			} else if (seekbar.getId() == R.id.sensitivity_seekbar) {
				drumKit.setThresholdProgress(progress);
				sensitivityText.setText("Drum Hit Sensitivity: "
						+ String.valueOf(drumKit.getThresholdProgress() + 1));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekbar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
		}
	};

	public static abstract class SettingsListener {

		public SettingsListener() {
		}

		public abstract void onDrumClicked(int drum);
	}
}
