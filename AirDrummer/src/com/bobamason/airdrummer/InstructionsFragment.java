package com.bobamason.airdrummer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InstructionsFragment extends Fragment {

	private InstructionsFragment.OnCompleteListener onCompleteListener = null;

	private Button completeButton;

	private int count = 0;

	private ImageView image;

	private TextView text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_instructions, container,
				false);
		image = (ImageView) view.findViewById(R.id.instructions_imageview);
		text = (TextView) view.findViewById(R.id.instructions_text);
		completeButton = (Button) view
				.findViewById(R.id.instructions_complete_btn);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		completeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View p1) {
				if (onCompleteListener != null) {
					count++;
					if (count == 1) {
						text.setText("By tilting your device up and down you can change the pitch of the sound when you hit the drum."
								+ " This can be disabled in the settings.");

						image.setImageResource(R.drawable.pitch_instruction);
					} else if (count > 1 && onCompleteListener != null) {
						onCompleteListener.complete();
					}
				}
			}
		});
	}

	public void setOnCompletedListener(
			InstructionsFragment.OnCompleteListener onCompleteListener) {
		this.onCompleteListener = onCompleteListener;
	}

	public static abstract class OnCompleteListener {
		OnCompleteListener() {
		}

		public abstract void complete();
	}
}
