package com.example.vip.smarthome.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
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

public class FanFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View
	   .OnClickListener {
	private RadioGroup fanSpeed;
	private EditText fanTime;
	private Button setTime;
	private Bundle bundle;
	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private OnFragmentListener listener;

	public static final String FAN_FLAG = "fan";
	public static final String FAN_SPEED_FLAG = "fan_speed";
	public static final String FAN_TIME_FLAG = "fan_time";
	public static final int FAN_QUICK_SPEED = 10;
	public static final int FAN_SLOW_SPEED = 60;
	public static final int FAN_MEDIUM_SPEED = 30;
	public static final int FAN_STOP =100;


	public void setOnFragmentListener(OnFragmentListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
		   savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fan, container,
			   false);
		bundle = new Bundle();
		bundle.putString(MainActivity.FRAGMENT_FLAG, FAN_FLAG);
		fanSpeed = (RadioGroup) view.findViewById(R.id.fan_speed);
		fanTime = (EditText) view.findViewById(R.id.fan_time);
		setTime = (Button) view.findViewById(R.id.fan_set_time);
		fanSpeed.setOnCheckedChangeListener(this);
		setTime.setOnClickListener(this);
		Log.w("fragment","onCreateView");
		return view;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int speed = 0;
		switch (checkedId) {
			case R.id.fan_quick:
				speed = FAN_QUICK_SPEED;
				break;
			case R.id.fan_medium:
				speed = FAN_MEDIUM_SPEED;
				break;
			case R.id.fan_slow:
				speed = FAN_SLOW_SPEED;
				break;
			case R.id.fan_stop:
				speed=FAN_STOP;
				break;
		}
		bundle.putInt(FAN_SPEED_FLAG, speed);
		if (listener != null) {
			listener.onFragment(bundle);
		}
	}

	@Override
	public void onClick(View v) {
		if (null == listener) return;
		try {
			int time = (int) (-(new Date().getTime() - format.parse(fanTime.getText().toString()
			).getTime()) / 1000 / 60);
			if (time <= 0) {
				Toast.makeText(getActivity(), "该时刻已过去或间隔过短", Toast.LENGTH_SHORT).show();
				return;
			}
			bundle.putInt(FAN_TIME_FLAG, time);
			listener.onFragment(bundle);
		} catch (ParseException e) {
			Toast.makeText(getActivity(), "大厅电风扇定时格式错误", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Log.w("fragment","onAttach");
	}

	@Override
	public void onAttachFragment(Fragment childFragment) {
		super.onAttachFragment(childFragment);
		Log.w("fragment","onAttachFragment");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w("fragment","onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w("fragment","onDestroy");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.w("fragment","onDestroyView");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.w("fragment","onDetach");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.w("fragment","onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.w("fragment","onResume");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.w("fragment","onStart");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.w("fragment","onStop");
	}

}
