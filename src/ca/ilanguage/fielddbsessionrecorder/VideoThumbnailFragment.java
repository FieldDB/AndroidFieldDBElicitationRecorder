package ca.ilanguage.fielddbsessionrecorder;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class VideoThumbnailFragment extends Fragment {
	private Long currentRowId;
	LinearLayout carouselLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_video_thumbnails,
				container, false);

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
		carouselLayout.removeAllViews();

		// Query for all videos on external storage
		ContentResolver cr = getActivity().getContentResolver();
		String[] proj = { BaseColumns._ID, MediaStore.Video.Media.TITLE };

		Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				proj, null, null, null);

		if (cursor.moveToFirst()) {
			carouselLayout.removeAllViews();
			do {
				int id = cursor.getInt(0);
				int videoTitleIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
				String videoTitle = cursor.getString(videoTitleIndex);
				String[] videoTitleParts = videoTitle.split("[.]");
				String[] videoTitleSubParts = videoTitleParts[0].split("_");
				if (videoTitleSubParts[0].equals("fielddb")) {
					Long rowID;
					try {
						rowID = Long.parseLong(videoTitleSubParts[3]);
					} catch (Exception e) {
						Log.v("TEST", "Found a malformed video file."
								+ videoTitle);
						continue;
					}
					if (rowID == currentRowId) {
						Bitmap b;
						b = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
								MediaStore.Video.Thumbnails.MICRO_KIND, null);
						ImageView thumbnail = new ImageView(c);
						thumbnail.setImageBitmap(b);
						thumbnail.setPadding(10, 10, 10, 10);
						thumbnail.setAdjustViewBounds(true);
						thumbnail.setTag(videoTitle);

						thumbnail
								.setOnLongClickListener(new OnLongClickListener() {

									// Delete video on long click
									@Override
									public boolean onLongClick(View v) {
										final String filename = v.getTag()
												.toString();
										AlertDialog.Builder alert = new AlertDialog.Builder(
												v.getContext());
										alert.setTitle(R.string.delete_video);
										alert.setMessage(R.string.dialog_verify_delete_video);

										alert.setPositiveButton(
												R.string.delete_video,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														// Find video file in
														// MediaStore
														ContentResolver cr = getActivity()
																.getContentResolver();
														Uri videosUri = MediaStore.Video.Media
																.getContentUri("external");
														String[] projection = { MediaStore.Video.Media._ID };
														Cursor cursor;
														try {
															cursor = cr
																	.query(videosUri,
																			projection,
																			MediaStore.Video.VideoColumns.TITLE
																					+ " LIKE ?",
																			new String[] { filename },
																			null);

															cursor.moveToFirst();
														} catch (Exception e) {
															return;
														}
														int columnIndex = cursor
																.getColumnIndex(projection[0]);
														int videoId = cursor
																.getInt(columnIndex);

														// Delete video and
														// associated thumbnail
														cr.delete(
																MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
																MediaStore.Video.VideoColumns._ID
																		+ " = ?",
																new String[] { ""
																		+ videoId });

														cr.delete(
																MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
																MediaStore.Video.Thumbnails.VIDEO_ID
																		+ " = ?",
																new String[] { ""
																		+ videoId });
														cursor.close();
														updateThumbnails(getActivity());
													}
												});

										alert.setNegativeButton(
												R.string.cancel,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.cancel();
													}
												});
										AlertDialog alertDialog = alert
												.create();
										alertDialog.show();
										return true;
									}
								});

						// Play video in PlayVideo activity on normal click
						thumbnail.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								PlayVideoFragment playVideoFragment = (PlayVideoFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.playVideoFragment);
								String videoFilename = v.getTag().toString();
								playVideoFragment.setVideo(videoFilename);
							}
						});

						carouselLayout.addView(thumbnail);
					}
				}
			} while (cursor.moveToNext());
		} 
		cursor.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Calling updateThumbnails from here allows it to be called on initial
		// create and after video intent resolution
		updateThumbnails(getActivity());
	}
}
