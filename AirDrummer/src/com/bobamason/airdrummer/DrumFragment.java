package com.bobamason.airdrummer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.*;
import android.os.*;

public class DrumFragment extends Fragment {

	private Button calibrateButton, viewButton;

	private Activity activity;

	private GLSurface surface;
	
	private TextView textView;

	private Handler handler;

	private DrumKit drumkit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_drum, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		drumkit = ((MainActivity) activity).getDrumKit();
		handler = new Handler();
		surface = (GLSurface) activity.findViewById(R.id.surface);
		textView = (TextView) activity.findViewById(R.id.calibration_text);
		calibrateButton = (Button) activity.findViewById(R.id.calibration_button);
		calibrateButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1) {
					drumkit.doCalibrate();
					textView.setText("recalibrated");
					if(textView.getVisibility() == View.GONE && ((MainActivity) activity).getDrumKit().isRun()){
						textView.setVisibility(View.VISIBLE);
					new Thread(new Runnable(){

							@Override
							public void run() {
								handler.postDelayed(new Runnable(){

										@Override
										public void run() {
											textView.setVisibility(View.GONE);
										}
									}, 1200);
							}
						}).start();
						}
				}
			});
			
		viewButton = (Button) activity.findViewById(R.id.view_button);
		viewButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1) {
					drumkit.toggleView();
					textView.setText("View " + (drumkit.getViewPos() + 1));
					if(textView.getVisibility() == View.GONE && ((MainActivity) activity).getDrumKit().isRun()){
						textView.setVisibility(View.VISIBLE);
						new Thread(new Runnable(){

								@Override
								public void run() {
									handler.postDelayed(new Runnable(){

											@Override
											public void run() {
												textView.setVisibility(View.GONE);
											}
										}, 1200);
								}
							}).start();
					}
				}
			});
	}

	@Override
	public void onResume() {
		super.onResume();
		surface.onResume();
	}

	@Override
	public void onPause() {
		surface.onPause();
		((MainActivity) activity).getDrumKit().setRun(false);
		super.onPause();
	}
}
