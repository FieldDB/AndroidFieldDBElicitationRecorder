package org.lingsync.elicitation.collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class GalleryView extends Activity {
	private DatumsDbAdapter mDbHelper;
	LinearLayout carouselLayout;
	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST;
	private static final int NEW_SESSION_ID = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		mDbHelper = new DatumsDbAdapter(this);
		mDbHelper.open();

		carouselLayout = (LinearLayout) findViewById(R.id.videoPreviewCarousel);
		
		DeviceDetails mDeviceDetails = new DeviceDetails(this, true, PrivateConstants.TAG);
		Log.v(PrivateConstants.TAG, mDeviceDetails.getCurrentDeviceDetails());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SESSION_LIST_VIEW_ID, 0, R.string.menu_view_session_list);
		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SESSION_LIST_VIEW_ID:
			showSessionList();
			return true;
		case NEW_SESSION_ID:
			createNote();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void showSessionList() {
		Intent i = new Intent(this, SessionListView.class);
		startActivity(i);
	}

	private void createNote() {
		Long id = mDbHelper.createNote("", "", "", "", "", "");
		Intent i = new Intent(this, SessionAccess.class);
		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
		startActivity(i);
	}

	public void populateVideoPreview() {

		// Query for all videos on external storage
		ContentResolver cr = getContentResolver();
		String[] proj = { BaseColumns._ID, MediaStore.Video.Media.TITLE };

		Cursor c = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
				null, null, null);
		if (c.moveToFirst()) {
			carouselLayout.removeAllViews();
			do {
				int id = c.getInt(0);
				int videoTitleIndex = c
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
				String videoTitle = c.getString(videoTitleIndex);
				String[] videoTitleParts = videoTitle.split("[.]");
				String[] videoTitleSubParts = videoTitleParts[0].split("_");

				if (videoTitleSubParts[0].equals("fielddb")) {
					// Get SQL session id (row id)
					Long rowID;
					Bitmap b;
					try {
						rowID = Long.parseLong(videoTitleSubParts[3]);
						b = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
								MediaStore.Video.Thumbnails.MINI_KIND, null);
					} catch (Exception e) {
						Log.v(PrivateConstants.TAG,
								"Found a malformed video file. " + videoTitle);
						continue;
					}

					Cursor note = mDbHelper.fetchNote(rowID);
					;
					if (note == null) {
						continue;
					}

					// Get goal for image label
					String tempGoal;
					String goal;
					String tempDate;
					String imageLabelText;

					try {
						tempGoal = note
								.getString(note
										.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));
						tempDate = note
								.getString(note
										.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5));

						if (tempGoal.length() > 16) {
							goal = tempGoal.substring(0, 15).concat("...");
						} else {
							goal = tempGoal;
						}

						// TODO Format date
						imageLabelText = goal.concat("\n").concat(tempDate);

					} catch (Exception e) {
						continue;
					}

					ImageView thumbnail = new ImageView(this);

					Bitmap roundedThumbnail = getRoundedCornerBitmap(b, 30);
					thumbnail.setImageBitmap(roundedThumbnail);

					Drawable d = getResources().getDrawable(
							R.drawable.image_border);
					thumbnail.setBackground(d);

					thumbnail.setTag(R.id.VIDEO_FILENAME_TAG_KEY, videoTitle);

					// Go to NoteTaking activity on normal click
					thumbnail.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent accessSession = new Intent(v.getContext(),
									SessionAccess.class);
							String[] filePathParts = v
									.getTag(R.id.VIDEO_FILENAME_TAG_KEY)
									.toString().split("[.]");
							String[] filePathSubParts = filePathParts[0]
									.split("_");
							Long rowID = Long.parseLong(filePathSubParts[3]);
							accessSession.putExtra("videoFilename",
									v.getTag(R.id.VIDEO_FILENAME_TAG_KEY)
											.toString());
							accessSession.putExtra(DatumsDbAdapter.KEY_ROWID,
									rowID);
							startActivity(accessSession);
						}
					});

					TextView thumbnailLabel = new TextView(this);

					thumbnailLabel.setText(imageLabelText);

					// Set up layout parameters
					RelativeLayout imageAndTextRelativeLayout = new RelativeLayout(
							this);
					RelativeLayout.LayoutParams main_lp = new RelativeLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					main_lp.addRule(RelativeLayout.CENTER_HORIZONTAL,
							RelativeLayout.TRUE);
					main_lp.addRule(RelativeLayout.CENTER_VERTICAL,
							RelativeLayout.TRUE);

					main_lp.setMargins(20, 20, 20, 20);
					imageAndTextRelativeLayout.setLayoutParams(main_lp);

					thumbnail.setLayoutParams(main_lp);

					RelativeLayout.LayoutParams text_lp = new RelativeLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					text_lp.addRule(RelativeLayout.CENTER_HORIZONTAL,
							RelativeLayout.TRUE);
					text_lp.addRule(RelativeLayout.CENTER_VERTICAL,
							RelativeLayout.TRUE);
					thumbnailLabel.setTextColor(Color.parseColor("#FFFFFF"));
					thumbnailLabel.setTypeface(Typeface.DEFAULT_BOLD);

					// Variables set based on device size
					boolean tabletSize = getResources().getBoolean(
							R.bool.isTablet);
					if (tabletSize) {
						thumbnailLabel.setTextSize(35);
					} else {
						thumbnailLabel.setTextSize(20);
					}
					thumbnailLabel.setShadowLayer(10f, 0, 0,
							Color.parseColor("#000000"));
					thumbnailLabel.setLayoutParams(text_lp);

					// Add individual items to relative layout container
					imageAndTextRelativeLayout.addView(thumbnail);
					imageAndTextRelativeLayout.addView(thumbnailLabel);

					// Add imageAndTextLinearLayout container to linear layout
					// in view
					carouselLayout.addView(imageAndTextRelativeLayout);
				}
			} while (c.moveToNext());
		}
		c.close();
		LinearLayout galleryThumbnails = (LinearLayout) findViewById(R.id.videoPreviewCarousel);
		int numberOfThumbnails = galleryThumbnails.getChildCount();
		if (numberOfThumbnails == 0) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.dialog_welcome);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
		}
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

	@Override
	public void onResume() {
		super.onResume();
		populateVideoPreview();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}
}
