package com.example.vip.smarthome.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.vip.smarthome.MainActivity;
import com.example.vip.smarthome.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vip on 2017/5/25.
 */

public class LightFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View
	   .OnClickListener {
	private RadioGroup hall, bedroon;
	private SeekBar bedSeekbar;
	private EditText hallTime, bedroonTime, bedTime;
	private Button setTime;
	private int isHallLightOpen, isBedroonLightOpen;
	private Bundle bundle;
	public static final String LIGHT_FLAG = "light";
	public static final String BED_LIGHT_FLAG = "bedLightness";
	public static final String HALL_LIGHT_FLAG = "isHallLightOpen";
	public static final String BEDROOM_LIGHT_FLAG = "isBedroonLightOpen";
	public static final String HALL_TIME_FLAG = "halltime";
	public static final String BEDROOM_TIME_FLAG = "bedroomtime";
	public static final String BED_TIME_FLAG = "bedtime";

	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private OnFragmentListener listener;

	public void setOnFragmentListener(OnFragmentListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
		   savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_light,
			   container,
			   false);
		bundle = new Bundle();
		bundle.putString(MainActivity.FRAGMENT_FLAG, LIGHT_FLAG);
		setTime = (Button) view.findViewById(R.id.set_time);
		setTime.setOnClickListener(this);
		hall = (RadioGroup) view.findViewById(R.id.hall);
		hall.setOnCheckedChangeListener(this);
		bedroon = (RadioGroup) view.findViewById(R.id.bedroom);
		bedroon.setOnCheckedChangeListener(this);
		bedSeekbar = (SeekBar) view.findViewById(R.id.bed);
		hallTime = (EditText) view.findViewById(R.id.hall_time);
		bedroonTime = (EditText) view.findViewById(R.id.bedroom_time);
		bedTime = (EditText) view.findViewById(R.id.bed_time);
		bedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (listener != null) {
					bundle.putInt(BED_LIGHT_FLAG, progress);
					listener.onFragment(bundle);
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		return view;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.hall_open:
				isHallLightOpen = 1;
				bundle.putInt(HALL_LIGHT_FLAG, isHallLightOpen);
				break;
			case R.id.hall_close:
				isHallLightOpen = 0;
				bundle.putInt(HALL_LIGHT_FLAG, isHallLightOpen);
				break;
			case R.id.bedroom_open:
				isBedroonLightOpen = 1;
				bundle.putInt(BEDROOM_LIGHT_FLAG, isBedroonLightOpen);
				break;
			case R.id.bedroom_close:
				isBedroonLightOpen = 0;
				bundle.putInt(BEDROOM_LIGHT_FLAG, isBedroonLightOpen);
				break;
		}
		if (listener != null) {
			listener.onFragment(bundle);
		}
	}


	@Override
	public void onClick(View v) {
		Log.w("tag", "设定定时");
		if (null == listener) return;
		long now = new Date().getTime();
		try {
			int time = (int) (-(now - format.parse(hallTime.getText().toString()
			).getTime()) / 1000 / 60);
			if (time <= 0) {
				throw new Exception();
			}
			bundle.putInt(HALL_TIME_FLAG, time);
			listener.onFragment(bundle);
		} catch (Exception e) {
			Toast.makeText(getActivity(), "大厅灯定时错误", Toast.LENGTH_SHORT).show();
		}

		try {
			int time = (int) (-(now - format.parse(bedroonTime.getText().toString()
			).getTime()) / 1000 / 60);
			if (time <= 0) {
				throw new Exception();
			}
			bundle.putInt(BEDROOM_TIME_FLAG, time);
			listener.onFragment(bundle);
		} catch (Exception e) {
			Log.w("smart", "卧室灯定时格式错误");
			Toast.makeText(getActivity(), "卧室灯定时错误", Toast.LENGTH_SHORT).show();
		}


		try {
			int time = (int) (-(now - format.parse(bedTime.getText().toString()
			).getTime()) / 1000 / 60);
			if (time <= 0) {
				throw new Exception();
			}
			bundle.putInt(BED_TIME_FLAG, time);
			listener.onFragment(bundle);
		} catch (Exception e) {
			Log.w("smart", "床头灯定时格式错误");
			Toast.makeText(getActivity(), "床头灯定时错误", Toast.LENGTH_SHORT).show();
		}


	}


}
