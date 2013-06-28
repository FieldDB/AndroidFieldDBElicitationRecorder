package ca.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class NoteTaking extends FragmentActivity {
	private static final int VIDEO_GALLERY_VIEW_ID = Menu.FIRST;
	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST + 1;
	private static final int NEW_SESSION_ID = Menu.FIRST + 2;
	private static final int NEW_VIDEO_ID = Menu.FIRST + 3;
	private static final int RECORD_VIDEO = 0;
	private DatumsDbAdapter mDbHelper;
	private EditText mRow_IDText;
	private File videosFolder;
	String rowID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_taking);
		mDbHelper = new DatumsDbAdapter(this);
		mDbHelper.open();
		mRow_IDText = (EditText) findViewById(R.id.row_id);
		rowID = mRow_IDText.getText().toString();
		// Create video folder if it does not already exist
		videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdir();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, VIDEO_GALLERY_VIEW_ID, 0, R.string.menu_view_video_gallery);
		menu.add(0, SESSION_LIST_VIEW_ID, 0, R.string.menu_view_session_list);
		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
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
		case NEW_SESSION_ID:
			createNote();
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

	private void createNote() {
		Long id = mDbHelper.createNote("", "", "", "", "", "");
		Intent i = new Intent(this, SessionAccess.class);
		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
		startActivity(i);
	}

	public void recordVideo() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

		String time = String.valueOf(System.currentTimeMillis());
		String video_filename = "fielddb_session_" + time + "_" + rowID
				+ ".mp4";

		File video = new File(videosFolder, video_filename);

		Uri uriSavedVideo = Uri.fromFile(video);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);

		startActivityForResult(cameraIntent, RECORD_VIDEO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RECORD_VIDEO && resultCode != 0 && data.getData() != null) {
			PlayVideoFragment playVideoFragment = (PlayVideoFragment) getSupportFragmentManager().findFragmentById(R.id.playVideoFragment);
			String filePath = data.getData().toString();
			playVideoFragment.setVideo(filePath);
		}
	}

}
