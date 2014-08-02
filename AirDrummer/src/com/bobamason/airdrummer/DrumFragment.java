package com.bobamason.airdrummer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DrumFragment extends Fragment {

	private Button calibrateButton;

	private Activity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_drum, container, false);
		calibrateButton = (Button) view.findViewById(R.id.calibration_button);
		calibrateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View p1) {
				((MainActivity) activity).getDrumKit().doCalibrate();
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) activity).getDrumKit().setRun(true);
	}

	@Override
	public void onPause() {
		((MainActivity) activity).getDrumKit().setRun(false);
		super.onPause();
	}
}
