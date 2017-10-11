package com.example.vip.smarthome.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.opengl.GLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by vip on 2017/5/26.
 */

public class AirFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
		   savedInstanceState) {
		TextView textView = new TextView(getActivity());
		textView.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
		textView.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
		textView.setText("暂无显示信息");
		textView.setTextSize(60);
		textView.setTextColor(Color.RED);
		textView.setGravity(Gravity.CENTER);
		return textView;
	}
}
