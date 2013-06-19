package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ListFragment extends Fragment {
	private OnItemSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_overview,
				container, false);

		updateThumbnails(view);
		// File dir = Environment.getExternalStorageDirectory();
		// String SD_PATH = dir.getAbsolutePath() + "/FieldDBSessions";
		// File file = new File(SD_PATH);
		// File allThumbnails[] = file.listFiles();
		// ImageView[] imageViewArray = new ImageView[allThumbnails.length];
		// //
		// for (int i = 0; i < allThumbnails.length; i++) {
		// Bitmap bmThumbnail;
		// bmThumbnail = ThumbnailUtils.createVideoThumbnail(
		// allThumbnails[i].getPath(), Thumbnails.MINI_KIND);
		//
		// imageViewArray[i] = new ImageView(view.getContext());
		//
		// LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// imageViewArray[i].setLayoutParams(lp);
		// imageViewArray[i].setPadding(5, 0, 5, 0);
		// imageViewArray[i].setAdjustViewBounds(true);
		// imageViewArray[i].setImageBitmap(bmThumbnail);
		// imageViewArray[i].setTag(allThumbnails[i].getPath());
		// imageViewArray[i].setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// updateVideo(v);
		// }
		// });
		// ((LinearLayout) view.findViewById(R.id.thumbnailCarousel))
		// .addView(imageViewArray[i]);
		// }

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnItemSelectedListener) {
			listener = (OnItemSelectedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet ListFragment.OnItemSelectedListener");
		}
	}

	public void updateThumbnails(View view) {

		File dir = Environment.getExternalStorageDirectory();
		String SD_PATH = dir.getAbsolutePath() + "/FieldDBSessions";
		File file = new File(SD_PATH);
		File allThumbnails[] = file.listFiles();
		ImageView[] imageViewArray = new ImageView[allThumbnails.length];

		int currentNumberOfThumbnails = ((LinearLayout) view
				.findViewById(R.id.thumbnailCarousel)).getChildCount();
		int i = 0;
		if (currentNumberOfThumbnails > 0) {
			i = currentNumberOfThumbnails;
		}
		for (; i < allThumbnails.length; i++) {
			Bitmap bmThumbnail;
			bmThumbnail = ThumbnailUtils.createVideoThumbnail(
					allThumbnails[i].getPath(), Thumbnails.MINI_KIND);

			imageViewArray[i] = new ImageView(view.getContext());

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			imageViewArray[i].setLayoutParams(lp);
			imageViewArray[i].setPadding(5, 0, 5, 0);
			imageViewArray[i].setAdjustViewBounds(true);
			imageViewArray[i].setImageBitmap(bmThumbnail);
			imageViewArray[i].setTag(allThumbnails[i].getPath());
			imageViewArray[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					updateVideo(v);
				}
			});
			((LinearLayout) view.findViewById(R.id.thumbnailCarousel))
					.addView(imageViewArray[i]);
		}
	}

	public interface OnItemSelectedListener {
		public void onVideoSelect(View v);
	}

	public void updateVideo(View v) {
		listener.onVideoSelect(v);
	}
}
