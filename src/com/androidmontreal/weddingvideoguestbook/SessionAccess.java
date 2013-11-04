package com.androidmontreal.weddingvideoguestbook;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.EasyTracker;

public class SessionAccess extends FragmentActivity implements
		SessionRecorderPublicInterface {
	private static final int RECORD_VIDEO = 0;
	private EditText mRow_IDText;
	private DeviceDetails mDeviceDetails;
	private Uri mFileUri;
	private Boolean D = true;
	public String TAG = PrivateConstants.TAG;
	private String mVideosFolder;

	Boolean isRegistered = false;
	String rowID;
	Uri cameraVideoURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_access);
		
		// Create videos folder if not already present
		mVideosFolder = Environment.getExternalStorageDirectory() + "/"
				+ PrivateConstants.DATA_KEYWORD;
		(new File(mVideosFolder)).mkdirs();

		mRow_IDText = (EditText) findViewById(R.id.row_id);
		rowID = mRow_IDText.getText().toString();
	}

	// Listen for video upload completion
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			VideoThumbnailFragment videoThumbnailFragment = (VideoThumbnailFragment) getSupportFragmentManager()
					.findFragmentById(R.id.videoThumbnailFragment);
			if (videoThumbnailFragment.isInLayout()) {
				videoThumbnailFragment.updateThumbnails(context);
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.video_menu, menu);
	    return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_new_video:
			recordVideo();
			break;
		case R.id.action_video_gallery:
//			showVideoGallery();
			this.onBackPressed();
			break;

		default:
			break;
		}

		// return super.onMenuItemSelected(featureId, item);
		return true;
		
	}

	private void showVideoGallery() {
		Intent i = new Intent(this, GalleryActivity.class);
		startActivity(i);
		//TODO this should call finish otherwise we are stacking up activities
	}

	public void showSessionList() {
		Intent i = new Intent(this, SessionListView.class);
		startActivity(i);
		//TODO this should call finish otherwise we are stacking up activities
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
		String time = String.valueOf(System.currentTimeMillis());

		// Test to see if FieldDB video folder exists; if not, create it
		(new File(mVideosFolder)).mkdirs();

		String videoTitle = mVideosFolder + "/" + PrivateConstants.DATA_KEYWORD
				+ PrivateConstants.DELIMITER + time
				+ PrivateConstants.DELIMITER + rowID + ".3gp";

		File f = new File(videoTitle);
		mFileUri = Uri.fromFile(f);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
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

		if (requestCode == RECORD_VIDEO && resultCode == Activity.RESULT_OK) {

			String videoID = data.getData().getLastPathSegment();

			//TODO is this intended to insert into the media scanner by hand? 
//			ContentResolver cr = getContentResolver();
//			Cursor cursor;
//			String[] projection = { BaseColumns._ID,
//					MediaStore.Video.Media.DATA,
//					MediaStore.Video.VideoColumns.TITLE,
//					MediaStore.Video.VideoColumns._ID };
//			try {
//				cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//						projection, MediaStore.Video.VideoColumns._ID
//								+ " LIKE ?", new String[] { videoID }, null);
//			} catch (Exception e) {
//				return;
//			}

//			cursor.moveToFirst();

//			ContentValues values = new ContentValues(3);

//			String deviceDetails = getHardwareDetails();

//			// Set values
//			values.put(MediaStore.Video.VideoColumns.TITLE, videoID);
//			values.put(MediaStore.Video.VideoColumns.DESCRIPTION, deviceDetails);

//			values.put(MediaStore.Video.Media.DATA, data.getData().getPath());

//			cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
//			cursor.close();

			if (data.getData() != null) {
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(data.getData());
				sendBroadcast(intent);
			}else{
				Log.e(TAG, "No video was returned.");
				return;
			}


			// Set new video to be played in fragment
			PlayVideoFragment playVideoFragment = (PlayVideoFragment) getSupportFragmentManager()
					.findFragmentById(R.id.playVideoFragment);
			playVideoFragment.setVideo(videoID);
			
			Intent uploadVideo = new Intent(getApplicationContext()
					.getApplicationContext(), UploadVideo.class);
			String filePath = data.getData().getPath();
			String fileName = videoID;
			uploadVideo.putExtra(PrivateConstants.EXTRA_FILENAME, fileName);
			uploadVideo.putExtra(PrivateConstants.EXTRA_FILEPATH, filePath);
			getApplicationContext().startService(uploadVideo);
		}
	}

	@Override
	public void onDestroy() {
		if (isRegistered == true) {
			unregisterReceiver(receiver);
			isRegistered = false;
		}
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PrivateConstants.VIDEO_UPLOADED);
		registerReceiver(receiver, filter);
		isRegistered = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (isRegistered == true) {
			unregisterReceiver(receiver);
			isRegistered = false;
		}
		super.onPause();
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
