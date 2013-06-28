package ca.ilanguage.fielddbsessionrecorder;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoGridFragment extends Fragment {
	private DatumsDbAdapter mDbHelper;
	private Long currentRowId;
	LinearLayout carouselLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_video_grid, container,
				false);

		currentRowId = (savedInstanceState == null || savedInstanceState
				.getSerializable(DatumsDbAdapter.KEY_ROWID) == null) ? null
				: (Long) savedInstanceState
						.getSerializable(DatumsDbAdapter.KEY_ROWID);
		if (currentRowId == null) {
			Bundle extras = getActivity().getIntent().getExtras();
			currentRowId = extras != null ? extras
					.getLong(DatumsDbAdapter.KEY_ROWID) : null;
		}

		carouselLayout = (LinearLayout) view
				.findViewById(R.id.thumbnailCarousel);

		return view;
	}

	public void updateThumbnails(Context c) {
		mDbHelper = new DatumsDbAdapter(c);
		mDbHelper.open();

		File dir = Environment.getExternalStorageDirectory();
		String SD_PATH = dir.getAbsolutePath() + "/FieldDBSessions";
		File file = new File(SD_PATH);
		File allVideos[] = file.listFiles();
		ArrayList<File> allThumbnails = new ArrayList<File>();

		for (int j = 0; j < allVideos.length; j++) {
			String filePath = allVideos[j].getPath();
			String[] filePathParts = filePath.split("\\.");
			String[] filePathSubParts = filePathParts[0].split("_");
			Long rowID = Long.parseLong(filePathSubParts[3]);
			if (rowID == currentRowId) {
				allThumbnails.add(allVideos[j]);
			}
		}

		if (allThumbnails.size() < 1) {
			((PublicInterface) this.getActivity()).hideVideoGridFragment(true);
			return;
		} else {
			((PublicInterface) this.getActivity()).hideVideoGridFragment(false);
		}

		ImageView[] imageViewArray = new ImageView[allThumbnails.size()];
		TextView[] textViewArray = new TextView[allThumbnails.size()];
		// Remove all thumbnails in view before updating
		carouselLayout.removeAllViews();

		for (int i = 0; i < allThumbnails.size(); i++) {
			String filePath = allThumbnails.get(i).getPath();
			Bitmap bmThumbnail;
			bmThumbnail = ThumbnailUtils.createVideoThumbnail(allThumbnails
					.get(i).getPath(), Thumbnails.MICRO_KIND);

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

			imageViewArray[i].setOnLongClickListener(new OnLongClickListener() {

				// Delete video on long click
				@Override
				public boolean onLongClick(View v) {
					final String filename = v.getTag().toString();
					AlertDialog.Builder alert = new AlertDialog.Builder(v
							.getContext());
					alert.setTitle(R.string.delete_video);
					alert.setMessage(R.string.dialog_verify_delete_video);

					alert.setPositiveButton(R.string.delete_video,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									File file = new File(filename);
									file.delete();
									updateThumbnails(getActivity());
								}
							});

					alert.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.cancel();
								}
							});
					AlertDialog alertDialog = alert.create();
					alertDialog.show();
					return true;
				}
			});

			// Play video in PlayVideo activity on normal click
			imageViewArray[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent playVideo = new Intent(getActivity(),
							PlayVideo.class);
					playVideo.putExtra("videoFilename", v.getTag().toString());
					startActivity(playVideo);
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

	public interface PublicInterface {
		public void hideVideoGridFragment(Boolean hide);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Calling updateThumbnails from here allows it to be called on initial
		// create and after video intent resolution
		updateThumbnails(getActivity());
	}
}
