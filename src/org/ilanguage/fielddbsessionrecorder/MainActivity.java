package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity implements
		ListFragment.OnItemSelectedListener {
	private static final int NEW_SESSION_ID = Menu.FIRST;
	private static final int SETTINGS_ID = Menu.FIRST + 1;
	private static final int CAMERA_VID_REQUEST = 1337;
	private DatumsDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case NEW_SESSION_ID:
			createSession();
			return true;
		case SETTINGS_ID:
			goToSettings();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	// TODO EXPAND INTO ACTIVITY
	private void createSession() {
		ListFragment listFragment = (ListFragment) getFragmentManager()
				.findFragmentById(R.id.listFragment);
		onClickCamera(listFragment.getView());
		return;
	}

	private void goToSettings() {
		return;
	}

	public void onClickCamera(View v) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

		// Create new video file (and folder, if needed)
		File videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdirs();
		String time = String.valueOf(System.currentTimeMillis());
		String video_filename = "fielddb_session_" + time + ".mp4";
		File video = new File(videosFolder, video_filename);
		Uri uriSavedVideo = Uri.fromFile(video);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);

		startActivityForResult(cameraIntent, CAMERA_VID_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_VID_REQUEST) {
			if (data.getData() != null) {
				DetailFragment fragment = (DetailFragment) getFragmentManager()
						.findFragmentById(R.id.detailFragment);

				if (fragment != null && fragment.isInLayout()) {
					VideoView vid = (VideoView) findViewById(R.id.IVDisplay);
					MediaController mediaController = new MediaController(this);
					mediaController.setAnchorView(vid);
					vid.setMediaController(mediaController);
					Uri mVideoUri = data.getData();
					vid.setVideoURI(mVideoUri);

					String uriToString = mVideoUri.toString();
					String[] uriParts = uriToString.split("\\.");
					String[] uriSubParts = uriParts[0].split("_");
					long sessionID = Long.parseLong(uriSubParts[2]);

					mDbHelper = new DatumsDbAdapter(this);
					mDbHelper.open();
					long id = mDbHelper.createNote(sessionID, "", "TEMP",
							"TEMP", "", "", "");

					// TEST
					// CODE BELOW RETURNS CORRECT INFO; RECORD IS BEING CREATED
					// TODO MAKE VIEW POPULATE FIELD BASED ON WHICHEVER SESSION
					// ID IS CURRENTLY SHOWING

					Cursor note = mDbHelper.fetchNote(id);

					Log.v("TEST",
							note.getString(note
									.getColumnIndexOrThrow(DatumsDbAdapter.KEY_ROWID))
									+ " "
									+ note.getString(note
											.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1))
									+ " "
									+ note.getString(note
											.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD2))
									+ " "
									+ note.getString(note
											.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD3)));

					//END TEST
					
					ListFragment mainFragment = (ListFragment) getFragmentManager()
							.findFragmentById(R.id.listFragment);
					mainFragment.updateThumbnails(mainFragment.getView());

				}
			}
		}
	}

	public void onVideoSelect(View v) {
		DetailFragment fragment = (DetailFragment) getFragmentManager()
				.findFragmentById(R.id.detailFragment);
		if (fragment != null && fragment.isInLayout()) {
			fragment.setVideo(v.getTag().toString());
		}
	}

	public void showHideVids(View v) {
		ListFragment listFragment = (ListFragment) getFragmentManager()
				.findFragmentById(R.id.listFragment);
		if (listFragment != null && listFragment.isInLayout()) {
			LinearLayout listContainer = (LinearLayout) findViewById(R.id.listFragmentContainer);
			LinearLayout vidContainer = (LinearLayout) findViewById(R.id.IVDisplayContainer);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Boolean currentState = listFragment.isVisible();
			if (currentState == true) {
				ft.hide(listFragment);
				// vidContainer.setVisibility(View.INVISIBLE);
				LinearLayout.LayoutParams newListParams = new LinearLayout.LayoutParams(
						0, 0);
				newListParams.weight = 0;
				listContainer.setLayoutParams(newListParams);
				LinearLayout.LayoutParams newVidParams = new LinearLayout.LayoutParams(
						0, 0);
				newVidParams.weight = 0;
				vidContainer.setLayoutParams(newVidParams);

			} else {
				ft.show(listFragment);
				// vidContainer.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams newListParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, 0);
				newListParams.weight = 20;
				listContainer.setLayoutParams(newListParams);
				LinearLayout.LayoutParams newVidParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				newVidParams.weight = 1;
				vidContainer.setLayoutParams(newVidParams);

			}
			ft.commit();
		}
	}

}
