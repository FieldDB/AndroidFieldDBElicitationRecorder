package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MyListFragment extends Fragment {
	private OnItemSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_overview,
				container, false);

		String[] imageTitles = { "test1", "test2", "test3", "test4", "test5",
				"test6", "test7", "test8", "test9", "test10", "test11",
				"test12", "test13" };
		// String[] mStrings = new String[imageTitles.length];
		ImageView[] imageViewArray = new ImageView[imageTitles.length];

		for (int i = 0; i < imageTitles.length; i++) {
			imageViewArray[i] = new ImageView(view.getContext());

			int resID = getResources().getIdentifier(imageTitles[i],
					"drawable", view.getContext().getPackageName());
			Drawable myImage = getResources().getDrawable(resID);
			imageViewArray[i].setImageDrawable(myImage);
			imageViewArray[i].setTag(imageTitles[i].toString());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			imageViewArray[i].setLayoutParams(lp);
			imageViewArray[i].setPadding(5, 0, 5, 0);
			imageViewArray[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					updateImage(v);
				}
			});
			imageViewArray[i].setAdjustViewBounds(true);
			((LinearLayout) view.findViewById(R.id.imageCarousel))
					.addView(imageViewArray[i]);
			Log.v("TEST", "" + imageViewArray[i]);

		}

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnItemSelectedListener) {
			listener = (OnItemSelectedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MyListFragment.OnItemSelectedListener");
		}
	}

	public interface OnItemSelectedListener {
		public void onImageSelect(View v);
	}

	public void updateImage(View v) {
		listener.onImageSelect(v);
	}
}
