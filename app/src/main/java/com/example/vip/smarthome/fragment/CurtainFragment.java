package com.example.vip.smarthome.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class CurtainFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View
	   .OnClickListener {
	private RadioGroup curtainState;
	private EditText curtainTime;
	private Button setTime;
	private Bundle bundle;
	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private OnFragmentListener listener;


	public static final String CURTAIN_FLAG = "curtain";
	public static final String CURTAIN_STATE_FLAG = "curtain_state";
	public static final String CURTAIN_TIME_FLAG = "curtain_time";
	public static final int CURTAIN_LITRE_STATE = 9;
	public static final int CURTAIN_DROP_STATE = 3;
	public static final int CURTAIN_STOP_STATE =6;

	public void setOnFragmentListener(OnFragmentListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
		   savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_curtain, container,
			   false);
		bundle = new Bundle();
		bundle.putString(MainActivity.FRAGMENT_FLAG, CURTAIN_FLAG);
		curtainState = (RadioGroup) view.findViewById(R.id.curtain_state);
		curtainTime = (EditText) view.findViewById(R.id.curtain_time);
		setTime = (Button) view.findViewById(R.id.curtain_set_time);
		curtainState.setOnCheckedChangeListener(this);
		setTime.setOnClickListener(this);
		return view;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int  state = 0;
		switch (checkedId) {
			case R.id.curtain_litre:
				state = CURTAIN_LITRE_STATE;
				break;
			case R.id.curtain_drop:
				state = CURTAIN_DROP_STATE;
				break;
			case R.id.curtain_stop:
				state = CURTAIN_STOP_STATE;
				break;
		}
		bundle.putInt(CURTAIN_STATE_FLAG, state);
		if (listener != null) {
			listener.onFragment(bundle);
		}
	}

	@Override
	public void onClick(View v) {
		if (null == listener) return;
		try {
			int time = (int) (-(new Date().getTime() - format.parse(curtainTime.getText().toString()
			).getTime()) / 1000 / 60);
			if (time <= 0) {
				Toast.makeText(getActivity(), "该时刻已过去或间隔过短", Toast.LENGTH_SHORT).show();
				return;
			}
			bundle.putInt(CURTAIN_TIME_FLAG, time);
			listener.onFragment(bundle);
		} catch (ParseException e) {
			Toast.makeText(getActivity(), "卧室窗帘定时错误", Toast.LENGTH_SHORT).show();
		}
	}


}
