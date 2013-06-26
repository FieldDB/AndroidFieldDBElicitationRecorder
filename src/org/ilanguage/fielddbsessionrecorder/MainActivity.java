package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends FragmentActivity implements PublicInterface {
	private static final int NEW_SESSION_ID = Menu.FIRST;
	private static final int NEW_VIDEO_ID = Menu.FIRST + 1;
	private static final int SETTINGS_ID = Menu.FIRST + 2;
	private static final int CAMERA_VID_REQUEST = 1337;

	private File videosFolder;

	VideoFragment videoFragment;
	DetailFragment detailFragment;
	ListFragment listFragment;

	private DatumsDbAdapter mDbHelper;

	// private EditText mCouch_IDText;
	private EditText mRow_IDText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Create video folder if it does not already exist
		videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdir();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			Log.v("TEST", "savedInstanceState is null.");
		} else {
			Log.v("TEST", "savedInstanceState is NOT null.");
		}
		
		
		//TODO Fix portrait mode.
		
		
		// Get fragments
		detailFragment = (DetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.detailFragment);
		videoFragment = (VideoFragment) getSupportFragmentManager()
				.findFragmentById(R.id.videoFragment);
		listFragment = (ListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.listFragment);

		// populateFields(test);
		mDbHelper = new DatumsDbAdapter(this);

		if (detailFragment != null && detailFragment.isInLayout()) {
			// Get pointers to EditText fields if they are in the view
			// mCouch_IDText = (EditText) findViewById(R.id.couch_id);
			mRow_IDText = (EditText) findViewById(R.id.row_id);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (listFragment != null && listFragment.isInLayout()) {
			super.onCreateOptionsMenu(menu);
			menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
			menu.add(0, NEW_VIDEO_ID, 0, R.string.menu_new_video);
			menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);
			return true;
		} else {
			return true;
		}

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case NEW_SESSION_ID:
			createSession();
			return true;
		case NEW_VIDEO_ID:
			recordVideo();
			return true;
		case SETTINGS_ID:
			goToSettings();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createSession() {
		if (detailFragment == null || !detailFragment.isInLayout()) {
			setContentView(R.layout.activity_main);
			detailFragment = (DetailFragment) getSupportFragmentManager()
					.findFragmentById(R.id.detailFragment);
		}
		mDbHelper.open();
		long id = mDbHelper.createNote("", "", "", "", "", "");
		showSessionInfo();
		populateFields(id);
		return;
	}

	private void recordVideo() {
		onClickCamera();
		return;
	}

	private void goToSettings() {
		return;
	}

	public void onClickCamera() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

		mRow_IDText = (EditText) findViewById(R.id.row_id);
		String rowID;
		if ((mRow_IDText != null)
				&& !(mRow_IDText.getText().toString()).equals("")) {
			// Save any changes to session info before starting video intent
			updateSessionInfo();
			rowID = mRow_IDText.getText().toString();
		} else {
			showSelectSessionDialog();
			return;
		}

		String time = String.valueOf(System.currentTimeMillis());
		String video_filename = "fielddb_session_" + time + "_" + rowID
				+ ".mp4";

		File video = new File(videosFolder, video_filename);

		Uri uriSavedVideo = Uri.fromFile(video);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);

		startActivityForResult(cameraIntent, CAMERA_VID_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_VID_REQUEST) {
			if (data.getData() != null) {
				if (detailFragment != null && detailFragment.isInLayout()) {
					VideoView vid = (VideoView) findViewById(R.id.IVDisplay);

					MediaController mediaController = new MediaController(this);
					mediaController.setAnchorView(vid);
					vid.setMediaController(mediaController);

					Uri mVideoUri = data.getData();
					vid.setVideoURI(mVideoUri);

					String uriToString = mVideoUri.toString();
					String[] uriParts = uriToString.split("\\.");
					String[] uriSubParts = uriParts[0].split("_");
					// long videoID = Long.parseLong(uriSubParts[2]);

					long rowID = Long.parseLong(uriSubParts[3]);
					populateFields(rowID);
					updateThumbnails();
				}
			}
		}
	}

	public void showSelectSessionDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.notification);

		alert.setMessage(R.string.session_required);

		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
		return;
	}

	// PublicInterface methods
	@Override
	public void onVideoSelect(View v) {
		String uriToString = v.getTag().toString();
		String[] uriParts = uriToString.split("\\.");
		String[] uriSubParts = uriParts[0].split("_");
		// long videoID = Long.parseLong(uriSubParts[2]);
		long rowID = Long.parseLong(uriSubParts[3]);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			updateSessionInfo();
		} else {
			setContentView(R.layout.activity_detail);

		}
		if (videoFragment == null) {
			videoFragment = (VideoFragment) getSupportFragmentManager()
					.findFragmentById(R.id.videoFragment);
		}
		if (detailFragment == null) {
			detailFragment = (DetailFragment) getSupportFragmentManager()
					.findFragmentById(R.id.detailFragment);
		}
		showSessionInfo();
		populateFields(rowID);
		videoFragment.setVideo(v.getTag().toString());
	}

	@Override
	public void updateThumbnails() {
		if (listFragment != null && listFragment.isInLayout()) {
			listFragment.updateThumbnailsInFragment(this);
		}
	}

	// Private method pointers to fragment methods
	private void updateSessionInfo() {
		if (detailFragment != null && detailFragment.isInLayout()) {
			detailFragment.updateSessionInfoInFragment(this);
		}
	}

	private void showSessionInfo() {
		if (detailFragment != null && detailFragment.isInLayout()) {
			detailFragment.showSessionInfoInFragment();
		}
	}

	private void populateFields(Long rowID) {
		if (detailFragment != null && detailFragment.isInLayout()) {
			detailFragment.populateFieldsInFragment(rowID);
		}
	}
}
