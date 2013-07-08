package org.lingsync.elicitation.collection;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.EasyTracker;

public class SessionAccess extends FragmentActivity implements
		SessionRecorderPublicInterface {
	private static final int VIDEO_GALLERY_VIEW_ID = Menu.FIRST;
	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST + 1;
	private static final int NEW_VIDEO_ID = Menu.FIRST + 2;
	private static final int RECORD_VIDEO = 0;
	private EditText mRow_IDText;
	private BroadcastReceiver receiver;
	private DeviceDetails mDeviceDetails;
	private Boolean D = true;
	public String TAG = PrivateConstants.TAG;

	VideoThumbnailFragment videoThumbnailFragment;
	String rowID;
	Uri cameraVideoURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_access);

		mRow_IDText = (EditText) findViewById(R.id.row_id);
		rowID = mRow_IDText.getText().toString();

		// Listen for video upload completion
		IntentFilter filter = new IntentFilter();
		filter.addAction(PrivateConstants.VIDEO_UPLOADED);

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.v(TAG, "Video upload complete.");
				videoThumbnailFragment = (VideoThumbnailFragment) getSupportFragmentManager()
						.findFragmentById(R.id.videoThumbnailFragment);
				if (videoThumbnailFragment.isInLayout()) {
					videoThumbnailFragment.updateThumbnails(context);
				}
			}
		};

		registerReceiver(receiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, VIDEO_GALLERY_VIEW_ID, 0, R.string.menu_view_video_gallery);
		menu.add(0, SESSION_LIST_VIEW_ID, 0, R.string.menu_view_session_list);
		menu.add(0, NEW_VIDEO_ID, 0, R.string.menu_new_video);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case VIDEO_GALLERY_VIEW_ID:
			showVideoGallery();
			return true;
		case SESSION_LIST_VIEW_ID:
			showSessionList();
			return true;
		case NEW_VIDEO_ID:
			recordVideo();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void showVideoGallery() {
		Intent i = new Intent(this, GalleryView.class);
		startActivity(i);
	}

	public void showSessionList() {
		Intent i = new Intent(this, SessionListView.class);
		startActivity(i);
	}

	public void recordVideo() {
		if (rowID.isEmpty()) {
			rowID = mRow_IDText.getText().toString();
		}

		if (rowID == null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.notification);
			alert.setMessage(R.string.dialog_please_save);

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

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, RECORD_VIDEO);

	}

	public String getHardwareDetails() {
		if (mDeviceDetails == null) {
			mDeviceDetails = new DeviceDetails(this, D, TAG);
		}
		String deviceType = mDeviceDetails.getCurrentDeviceDetails();
		return deviceType;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RECORD_VIDEO && resultCode != 0
				&& data.getData() != null) {

			String videoID = data.getData().getLastPathSegment();

			ContentResolver cr = getContentResolver();
			Cursor cursor;
			String[] projection = { BaseColumns._ID,
					MediaStore.Video.Media.DATA,
					MediaStore.Video.VideoColumns.TITLE,
					MediaStore.Video.VideoColumns._ID };
			try {
				cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						projection, MediaStore.Video.VideoColumns._ID
								+ " LIKE ?", new String[] { videoID }, null);
			} catch (Exception e) {
				return;
			}

			cursor.moveToFirst();

			ContentValues values = new ContentValues(3);

			// Declare values
			String deviceDetails = getHardwareDetails();
			String time = String.valueOf(System.currentTimeMillis());
			String videoTitle = "fielddb_session_" + time + "_" + rowID
					+ ".3gp";
			String videoFilePath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Video.Media.DATA));

			// Set values
			values.put(MediaStore.Video.VideoColumns.TITLE, videoTitle);
			values.put(MediaStore.Video.VideoColumns.DESCRIPTION, deviceDetails);

			values.put(MediaStore.Video.Media.DATA, videoFilePath);

			Uri videoFileUri = Uri
					.withAppendedPath(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							""
									+ cursor.getInt(cursor
											.getColumnIndex(MediaStore.Video.Media._ID)));

			cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
			cursor.close();

			new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoFileUri);

			// Set new video to be played in fragment
			PlayVideoFragment playVideoFragment = (PlayVideoFragment) getSupportFragmentManager()
					.findFragmentById(R.id.playVideoFragment);
			playVideoFragment.setVideo(videoTitle);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	// Public interface functions

	public void showHideVideoDisplays(int numberOfThumbnails) {
		PlayVideoFragment playVideoFragment = (PlayVideoFragment) getSupportFragmentManager()
				.findFragmentById(R.id.playVideoFragment);
		FragmentManager fm = getSupportFragmentManager();
		LinearLayout videoThumbnailFragmentContainer = (LinearLayout) findViewById(R.id.videoThumbnailFragmentContainer);
		if (numberOfThumbnails < 1) {
			videoThumbnailFragmentContainer.setVisibility(LinearLayout.GONE);
			fm.beginTransaction().hide(playVideoFragment).commit();
		} else {
			fm.beginTransaction().show(playVideoFragment).commit();
			videoThumbnailFragmentContainer.setVisibility(LinearLayout.VISIBLE);
		}
	}

}
