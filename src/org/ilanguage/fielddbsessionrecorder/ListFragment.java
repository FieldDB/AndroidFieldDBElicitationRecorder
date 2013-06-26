package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListFragment extends Fragment {
	PublicInterface mCallback;
	private DatumsDbAdapter mDbHelper;

	LinearLayout carouselLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_overview,
				container, false);

		carouselLayout = (LinearLayout) view
				.findViewById(R.id.thumbnailCarousel);
		updateThumbnailsInFragment(view.getContext());
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PublicInterface) {
			mCallback = (PublicInterface) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet PublicInterface");
		}
	}

	public void updateThumbnailsInFragment(Context c) {
		mDbHelper = new DatumsDbAdapter(c);
		mDbHelper.open();

		File dir = Environment.getExternalStorageDirectory();
		String SD_PATH = dir.getAbsolutePath() + "/FieldDBSessions";
		File file = new File(SD_PATH);
		File allThumbnails[] = file.listFiles();
		ImageView[] imageViewArray = new ImageView[allThumbnails.length];
		TextView[] textViewArray = new TextView[allThumbnails.length];
		// LinearLayout carouselLayout = (LinearLayout) view
		// .findViewById(R.id.thumbnailCarousel);
		// Remove all thumbnails in view before updating
		carouselLayout.removeAllViews();

		for (int i = 0; i < allThumbnails.length; i++) {
			String filePath = allThumbnails[i].getPath();
			Bitmap bmThumbnail;
			bmThumbnail = ThumbnailUtils.createVideoThumbnail(
					allThumbnails[i].getPath(), Thumbnails.MINI_KIND);

			imageViewArray[i] = new ImageView(c);
			LinearLayout.LayoutParams image_lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			image_lp.weight = 1;
			image_lp.gravity = Gravity.CENTER_HORIZONTAL;
			imageViewArray[i].setLayoutParams(image_lp);
			imageViewArray[i].setPadding(10, 10, 10, 10);
			imageViewArray[i].setAdjustViewBounds(true);
			imageViewArray[i].setImageBitmap(bmThumbnail);
			imageViewArray[i].setTag(filePath);
			imageViewArray[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallback.onVideoSelect(v);
				}
			});

			textViewArray[i] = new TextView(c);
			LinearLayout.LayoutParams text_lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			textViewArray[i].setLayoutParams(text_lp);
			textViewArray[i].setTextColor(Color.parseColor("#FFFFFF"));
			textViewArray[i].setPadding(10, 0, 10, 0);

			String[] filePathParts = filePath.split("\\.");
			String[] filePathSubParts = filePathParts[0].split("_");
			Long rowID = Long.parseLong(filePathSubParts[3]);

			// Get goal for image label
			Cursor note = mDbHelper.fetchNote(rowID);

			String temp = note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));

			String text;
			if (temp.length() > 16) {
				text = temp.substring(0, 15).concat("...");
			} else {
				text = temp;
			}
			textViewArray[i].setText(text);

			LinearLayout imageAndTextLinearLayout = new LinearLayout(c);
			LinearLayout.LayoutParams container_lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			imageAndTextLinearLayout.setLayoutParams(container_lp);
			imageAndTextLinearLayout.setOrientation(LinearLayout.VERTICAL);
			imageAndTextLinearLayout.addView(imageViewArray[i]);
			imageAndTextLinearLayout.addView(textViewArray[i]);

			// Add imageAndTextLinearLayout to view
			carouselLayout.addView(imageAndTextLinearLayout);
		}
	}

	// public interface OnItemSelectedListener {
	// public void onVideoSelect(View v);
	// }

	// public void updateVideo(View v) {
	// listener.onVideoSelect(v);
	// }
}
