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
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;

public class SessionAccess extends FragmentActivity {
	private static final int VIDEO_GALLERY_VIEW_ID = Menu.FIRST;
	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST + 1;
	private static final int NEW_VIDEO_ID = Menu.FIRST + 2;
	private static final int RECORD_VIDEO = 0;
	private EditText mRow_IDText;
	private BroadcastReceiver receiver;
	private DeviceDetails mDeviceDetails;
	private Boolean D = true;
	protected String TAG = PrivateConstants.TAG;


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

		String deviceDetails = getHardwareDetails();
		String time = String.valueOf(System.currentTimeMillis());
		String video_filename = "fielddb_session_" + time + "_" + rowID
				+ ".3gp";
		ContentValues values = new ContentValues(2);
		values.put(MediaStore.Video.Media.DESCRIPTION, deviceDetails);
		values.put(MediaStore.Video.Media.TITLE, video_filename);
		cameraVideoURI = getContentResolver().insert(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraVideoURI);
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
			ContentResolver cr = getContentResolver();

			Cursor cursor;

			String[] projection = { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.SIZE, MediaStore.Video.Media.TITLE };
			try {
				cursor = cr.query(cameraVideoURI, projection, null, null, null);
			} catch (Exception e) {
				return;
			}

			int column_index_data = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			int videoTitleIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
			cursor.moveToFirst();
			String recordedVideoFilePath = cursor.getString(column_index_data);
			String videoTitle = cursor.getString(videoTitleIndex);
			Uri fileUri = Uri.parse(recordedVideoFilePath);
			cursor.close();
			// Broadcast to media scanner that new file is present so that
			// thumbnails will be updated
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					fileUri));

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
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

}
