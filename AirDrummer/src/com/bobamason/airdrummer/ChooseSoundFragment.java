package com.bobamason.airdrummer;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChooseSoundFragment extends Fragment {

	private GridView gridView;
	private Bundle args;
	private Activity activity;

	private DrumKit drumKit;

	private int drumNum;

	private int currentSound;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		args = getArguments();
		drumNum = args.getInt("drum", 0);
		View view = inflater
				.inflate(R.layout.fragment_choose, container, false);
		gridView = (GridView) view.findViewById(R.id.gridview_choose);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		drumKit = ((MainActivity) activity).getDrumKit();
		currentSound = drumKit.getSoundPos(drumNum);
		gridView.setAdapter(new SoundAdapter());
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentSound = position;
				((SoundAdapter) gridView.getAdapter()).refresh();
				drumKit.playSoundPreview(position);
			}
		});
	}

	@Override
	public void onPause() {
		drumKit.setDrumSoundPos(drumNum, currentSound);
		super.onPause();
	}

	private class SoundAdapter extends BaseAdapter {

		LayoutInflater inflater;

		public SoundAdapter() {
			super();
			inflater = (LayoutInflater) activity
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return Constants.SOUND_LABELS.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void refresh() {
			notifyDataSetInvalidated();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View row = convertView;
			if (row == null) {
				row = inflater.inflate(R.layout.grid_item, parent, false);
				holder = new ViewHolder(row);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			holder.text.setText(Constants.SOUND_LABELS[position]);
			int bgID = position == currentSound ? Constants.DEFAULT_DRUM_BGS[drumNum]
					: R.drawable.section_bg;
			holder.bgLayout.setBackgroundResource(bgID);

			return row;
		}

		private class ViewHolder {
			TextView text;
			RelativeLayout bgLayout;

			ViewHolder(View v) {
				text = (TextView) v.findViewById(R.id.grid_item_text);
				bgLayout = (RelativeLayout) v
						.findViewById(R.id.grid_item_relative_layout);
			}
		}
	}
}
