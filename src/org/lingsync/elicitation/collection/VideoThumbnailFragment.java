package org.lingsync.elicitation.collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	protected String TAG;
	LinearLayout carouselLayout;
	SessionRecorderPublicInterface mCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_video_thumbnails,
				container, false);
		TAG = PrivateConstants.TAG;
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

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (SessionRecorderPublicInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	
	public void updateThumbnails(Context c) {
		carouselLayout.removeAllViews();
		
		// Query for all videos on external storage
		ContentResolver cr = getActivity().getContentResolver();
		String[] proj = { BaseColumns._ID, MediaStore.Video.VideoColumns.TITLE,
				MediaStore.Video.VideoColumns.TAGS };

		Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				proj, null, null, null);

		if (cursor.moveToFirst()) {
			carouselLayout.removeAllViews();
			do {
				int id = cursor.getInt(0);
				int videoTitleIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE);
				String videoTitle = cursor.getString(videoTitleIndex);
				String[] videoTitleParts = videoTitle.split("[.]");
				String[] videoTitleSubParts = videoTitleParts[0].split("_");
				if (videoTitleSubParts[0].equals("fielddb")) {
					Long rowID;
					try {
						rowID = Long.parseLong(videoTitleSubParts[3]);
					} catch (Exception e) {
						Log.v(TAG, "Found a malformed video file." + videoTitle);
						continue;
					}
					if (rowID == currentRowId) {
						Bitmap b;
						b = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
								MediaStore.Video.Thumbnails.MICRO_KIND, null);
						ImageView thumbnail = new ImageView(c);

						// Make thumbnails of uploaded videos rounded
						int videoTagsIndex = cursor
								.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TAGS);
						String externalVideoURL = cursor
								.getString(videoTagsIndex);
						
						if (externalVideoURL != null) {
							b = getRoundedCornerBitmap(b, 40);
							thumbnail.setTag(R.id.VIDEO_URL_TAG_KEY,
									externalVideoURL);
						} else {
							thumbnail.setTag(R.id.VIDEO_URL_TAG_KEY, "");
						}

						thumbnail.setImageBitmap(b);
						thumbnail.setPadding(10, 10, 10, 10);
						thumbnail.setAdjustViewBounds(true);
						thumbnail.setTag(R.id.VIDEO_FILENAME_TAG_KEY,
								videoTitle);

						thumbnail
								.setOnLongClickListener(new OnLongClickListener() {

									// Show video options on long click
									@Override
									public boolean onLongClick(View v) {

										final String filename = v.getTag(
												R.id.VIDEO_FILENAME_TAG_KEY)
												.toString();
										final String url = v.getTag(
												R.id.VIDEO_URL_TAG_KEY)
												.toString();

										AlertDialog.Builder alert = new AlertDialog.Builder(
												v.getContext());
										alert.setTitle(
												R.string.dialog_choose_video_options)
												.setItems(
														R.array.video_options,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {
																if (whichButton == 0) {
																	// Upload
																	// video
																	uploadVideo(
																			filename,
																			url);
																} else if (whichButton == 1) {
																	// Open URL
																	openURL(url);

																} else if (whichButton == 2) {
																	// Share URL
																	shareURL(url);

																} else if (whichButton == 3) {
																	// Delete
																	// video
																	deleteVideo(filename);
																} else if (whichButton == 4) {
																	// Cancel
																	// Do
																	// nothing
																}
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
								PlayVideoFragment playVideoFragment = (PlayVideoFragment) getActivity()
										.getSupportFragmentManager()
										.findFragmentById(
												R.id.playVideoFragment);
								String videoFilename = v.getTag(
										R.id.VIDEO_FILENAME_TAG_KEY).toString();
								playVideoFragment.setVideo(videoFilename);
							}
						});

						carouselLayout.addView(thumbnail);
					}
				}
			} while (cursor.moveToNext());
			
			//Hide thumbnail fragment if there are no thumbnails to show
			int numberOfThumbnails = carouselLayout.getChildCount();
			mCallback.showHideVideoDisplays(numberOfThumbnails);
		}
		cursor.close();
	}

	public void deleteVideo(String fileName) {
		final String filename = fileName;
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(R.string.delete_video);
		alert.setMessage(R.string.dialog_verify_delete_video);

		alert.setNegativeButton(R.string.delete_video,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Find video file in
						// MediaStore
						ContentResolver cr = getActivity().getContentResolver();
						Uri videosUri = MediaStore.Video.Media
								.getContentUri("external");
						String[] projection = { MediaStore.Video.Media._ID };
						Cursor cursor;
						try {
							cursor = cr.query(videosUri, projection,
									MediaStore.Video.VideoColumns.TITLE
											+ " LIKE ?",
									new String[] { filename }, null);

							cursor.moveToFirst();
						} catch (Exception e) {
							return;
						}
						int columnIndex = cursor.getColumnIndex(projection[0]);
						int videoId = cursor.getInt(columnIndex);

						// Delete video and
						// associated thumbnail
						cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
								MediaStore.Video.VideoColumns._ID + " = ?",
								new String[] { "" + videoId });

						cr.delete(
								MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
								MediaStore.Video.Thumbnails.VIDEO_ID + " = ?",
								new String[] { "" + videoId });
						cursor.close();
						updateThumbnails(getActivity());
					}
				});
		alert.setPositiveButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}

	public void uploadVideo(String fileName, String url) {

		Boolean wifiConnected = isConnected(getActivity());

		if (wifiConnected != true) {
			Log.v(TAG, "Not connected to Wifi!");
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.not_connected_to_wifi);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
			return;
		} else if (url == "") {
			// Find video file in MediaStore
			ContentResolver cr = getActivity().getContentResolver();
			Uri videosUri = MediaStore.Video.Media.getContentUri("external");
			String[] projection = { MediaStore.Video.VideoColumns.DATA };
			Cursor cursor;
			try {
				cursor = cr.query(videosUri, projection,
						MediaStore.Video.VideoColumns.TITLE + " LIKE ?",
						new String[] { fileName }, null);

				cursor.moveToFirst();
			} catch (Exception e) {
				return;
			}
			int columnIndex = cursor.getColumnIndex(projection[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();

			Intent uploadVideo = new Intent(getActivity()
					.getApplicationContext(), UploadVideo.class);
			uploadVideo.putExtra(PrivateConstants.EXTRA_FILEPATH, filePath);
			uploadVideo.putExtra(PrivateConstants.EXTRA_FILENAME, fileName);
			getActivity().startService(uploadVideo);
		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.video_already_uploaded);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
			return;
		}
	}

	public void openURL(String url) {
		if (url != "") {
			try {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			} catch (Exception e) {
				Log.v(TAG, "Error opening URL.");
				return;
			}
		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.dialog_please_upload);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
			return;
		}
	}

	public void shareURL(String url) {
		if (url != "") {
			try {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, url);
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			} catch (Exception e) {
				Log.v(TAG, "Error sharing URL.");
				return;
			}
		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.dialog_please_upload);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
			return;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Calling updateThumbnails from here allows it to be called on initial
		// create and after video intent resolution
		updateThumbnails(getActivity());
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	private static boolean isConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
}
