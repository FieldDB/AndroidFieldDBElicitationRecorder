package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity implements
		ListFragment.OnItemSelectedListener {
	private static final int NEW_SESSION_ID = Menu.FIRST;
	private static final int NEW_VIDEO_ID = Menu.FIRST + 1;
	private static final int SETTINGS_ID = Menu.FIRST + 2;
	private static final int CAMERA_VID_REQUEST = 1337;
	private static final int ACTIVITY_EDIT = 42;

	private File videosFolder;
	private DatumsDbAdapter mDbHelper;

	private EditText mCouch_IDText;
	private EditText mRow_IDText;
	private EditText mField1Text;
	private EditText mField2Text;
	private EditText mField3Text;
	private EditText mField4Text;
	private EditText mField5Text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Create video folder if it does not already exist
		videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdir();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		// populateFields(test);
		mDbHelper = new DatumsDbAdapter(this);
		DetailFragment detailFragment = (DetailFragment) getFragmentManager()
				.findFragmentById(R.id.detailFragment);
		if (detailFragment != null && detailFragment.isInLayout()) {
			// Get pointers to EditText fields if they are in the view
			mCouch_IDText = (EditText) findViewById(R.id.couch_id);
			mRow_IDText = (EditText) findViewById(R.id.row_id);
			mField1Text = (EditText) findViewById(R.id.field1);
			mField2Text = (EditText) findViewById(R.id.field2);
			mField3Text = (EditText) findViewById(R.id.field3);
			mField4Text = (EditText) findViewById(R.id.field4);
			mField5Text = (EditText) findViewById(R.id.field5);

			// Hide empty session info on startup
			if ((mRow_IDText.getText().toString()).equals("")) {
				hideSessionInfo();
			} else {
				showSessionInfo();
			}

			Button confirmButton = (Button) findViewById(R.id.confirm);
			confirmButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					updateSessionInfo();
					ListFragment listFragment = (ListFragment) getFragmentManager()
							.findFragmentById(R.id.listFragment);
					listFragment.updateThumbnails(listFragment.getView());
				}

			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// TODO Temporarily getting rid of options menu in portrait mode

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
		mDbHelper.open();
		long id = mDbHelper.createNote("", "", "", "", "", "");
		showSessionInfo();
		populateFields(id);
		return;
	}

	private void recordVideo() {
		// Execute recording method
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

		mRow_IDText = (EditText) findViewById(R.id.row_id);
		String rowID;

		if (!(mRow_IDText.getText().toString()).equals("")) {
			// Save any changes to session info before starting video intent
			updateSessionInfo();

			rowID = mRow_IDText.getText().toString();
		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.notification); // Set Alert dialog title
													// here
			alert.setMessage(R.string.session_required); // Message here

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
				DetailFragment detailFragment = (DetailFragment) getFragmentManager()
						.findFragmentById(R.id.detailFragment);

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

					ListFragment listFragment = (ListFragment) getFragmentManager()
							.findFragmentById(R.id.listFragment);
					listFragment.updateThumbnails(listFragment.getView());
				}
			}
		}
	}

	public void onVideoSelect(View v) {
		VideoFragment videoFragment = (VideoFragment) getFragmentManager()
				.findFragmentById(R.id.videoFragment);
//		DetailFragment detailFragment = (DetailFragment) getFragmentManager()
//				.findFragmentById(R.id.detailFragment);
		String uriToString = v.getTag().toString();
		String[] uriParts = uriToString.split("\\.");
		String[] uriSubParts = uriParts[0].split("_");
		// long videoID = Long.parseLong(uriSubParts[2]);
		long rowID = Long.parseLong(uriSubParts[3]);

		if (videoFragment != null && videoFragment.isInLayout()) {
			showSessionInfo();
			updateSessionInfo();
			populateFields(rowID);
			videoFragment.setVideo(v.getTag().toString());
		} else {
			Intent i = new Intent(this, DetailActivity.class);
			i.putExtra("tag", v.getTag().toString());
			startActivity(i);
		}

		Log.v("TEST", "onVideoSelect clicked!");
	}

	// Commenting this method out because it's rather pointless... maybe can use
	// in another activity
	// public void showHideVids(View v) {
	// ListFragment listFragment = (ListFragment) getFragmentManager()
	// .findFragmentById(R.id.listFragment);
	// if (listFragment != null && listFragment.isInLayout()) {
	// LinearLayout listContainer = (LinearLayout)
	// findViewById(R.id.listFragmentContainer);
	// LinearLayout vidContainer = (LinearLayout)
	// findViewById(R.id.IVDisplayContainer);
	// FragmentTransaction ft = getFragmentManager().beginTransaction();
	// Boolean currentState = listFragment.isVisible();
	// if (currentState == true) {
	// ft.hide(listFragment);
	// LinearLayout.LayoutParams newListParams = new LinearLayout.LayoutParams(
	// 0, 0);
	// newListParams.weight = 0;
	// listContainer.setLayoutParams(newListParams);
	// LinearLayout.LayoutParams newVidParams = new LinearLayout.LayoutParams(
	// 0, 0);
	// newVidParams.weight = 0;
	// vidContainer.setLayoutParams(newVidParams);
	//
	// } else {
	// ft.show(listFragment);
	// LinearLayout.LayoutParams newListParams = new LinearLayout.LayoutParams(
	// LayoutParams.MATCH_PARENT, 0);
	// newListParams.weight = 20;
	// listContainer.setLayoutParams(newListParams);
	// LinearLayout.LayoutParams newVidParams = new LinearLayout.LayoutParams(
	// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	// newVidParams.weight = 1;
	// vidContainer.setLayoutParams(newVidParams);
	//
	// }
	// ft.commit();
	// }
	// }

	private void populateFields(Long rowID) {
		if (rowID != null) {
			mDbHelper.open();
			Cursor note = mDbHelper.fetchNote(rowID);

			startManagingCursor(note);
			mRow_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_ROWID)));
			mCouch_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_COUCH_ID)));
			mField1Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1)));
			mField2Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD2)));
			mField3Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD3)));
			mField4Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD4)));
			mField5Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5)));
		}
	}

	public void updateSessionInfo() {
		mDbHelper.open();

		if ((mRow_IDText.getText().toString()).equals("")) {
			return;
		}

		Long mRowId = Long.parseLong(mRow_IDText.getText().toString());
		String couch_id = mCouch_IDText.getText().toString();
		String field1 = mField1Text.getText().toString();
		String field2 = mField2Text.getText().toString();
		String field3 = mField3Text.getText().toString();
		String field4 = mField4Text.getText().toString();
		String field5 = mField5Text.getText().toString();

		mDbHelper.updateNote(mRowId, couch_id, field1, field2, field3, field4,
				field5);
	}

	public void showSessionInfo() {
		TextView warningText = (TextView) findViewById(R.id.warning_text);
		warningText.setVisibility(TextView.GONE);
		LinearLayout detailContainer = (LinearLayout) findViewById(R.id.main_detail_container);
		detailContainer.setVisibility(LinearLayout.VISIBLE);

	}

	public void hideSessionInfo() {
		TextView warningText = (TextView) findViewById(R.id.warning_text);
		warningText.setVisibility(TextView.VISIBLE);
		LinearLayout detailContainer = (LinearLayout) findViewById(R.id.main_detail_container);
		detailContainer.setVisibility(LinearLayout.GONE);
	}

}
