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

public class SessionAccess extends FragmentActivity {
	private static final int NEW_VIDEO_ID = Menu.FIRST;
	private static final int CAMERA_VID_REQUEST = 1337;
	private EditText mRow_IDText;
	VideoGridFragment videoGridFragment;
	private File videosFolder;
	String rowID;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_session_access);
		
		mRow_IDText = (EditText) findViewById(R.id.row_id);
		rowID = mRow_IDText.getText().toString();
//		Long currentRowId = Long.parseLong(rowID);
		
//		Bundle bundle=new Bundle();
//		bundle.putLong("currentRowId", currentRowId);
//		videoGridFragment = new VideoGridFragment();
//		videoGridFragment.setArguments(bundle);
		
//		Log.v("TEST", "arguments in SessionAccess " + videoGridFragment.getArguments().getLong("currentRowId"));
		
		
		
		
		// Create video folder if it does not already exist
		videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdir();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_VIDEO_ID, 0, R.string.menu_new_video);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case NEW_VIDEO_ID:
			recordVideo();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	public void recordVideo() {
		if (rowID == null) {
			return;
		}
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

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
				videoGridFragment = (VideoGridFragment) getSupportFragmentManager()
						.findFragmentById(R.id.videoGridFragment);
				if (videoGridFragment != null && videoGridFragment.isInLayout()) {
					// VideoView vid = (VideoView) findViewById(R.id.IVDisplay);

					// MediaController mediaController = new
					// MediaController(this);
					// mediaController.setAnchorView(vid);
					// vid.setMediaController(mediaController);

					// Uri mVideoUri = data.getData();
					// vid.setVideoURI(mVideoUri);

					// uriToString = mVideoUri.toString();
					// String[] uriParts = uriToString.split("\\.");
					// String[] uriSubParts = uriParts[0].split("_");
					// long videoID = Long.parseLong(uriSubParts[2]);

					// long rowID = Long.parseLong(uriSubParts[3]);
					// populateFields(rowID);
					videoGridFragment.updateThumbnails(this);
				}
			}
		}
	}
}
