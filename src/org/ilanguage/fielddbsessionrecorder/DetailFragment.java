package org.ilanguage.fielddbsessionrecorder;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DetailFragment extends Fragment {
	ImageView Display;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_detail,
				container, false);
		return view;
	}

	public void setImage(int item_id, String tag) {
		// TODO Auto-generated method stub
		Log.v("METHOD", "setImage called.");
		int resID = getResources().getIdentifier(tag , "drawable", getView().getContext().getPackageName());
		Display = (ImageView) getView().findViewById(R.id.IVDisplay);
		Display.setImageResource(resID);
	}

}
