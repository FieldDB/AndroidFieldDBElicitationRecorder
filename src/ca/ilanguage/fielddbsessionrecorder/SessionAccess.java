package ca.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class SessionAccess extends FragmentActivity implements
		VideoGridFragment.PublicInterface {
	private static final int NEW_VIDEO_ID = Menu.FIRST;
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
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

		String time = String.valueOf(System.currentTimeMillis());
		String video_filename = "fielddb_session_" + time + "_" + rowID
				+ ".mp4";

		File video = new File(videosFolder, video_filename);

		Uri uriSavedVideo = Uri.fromFile(video);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);

		startActivity(cameraIntent);
	}

	// Hides or shows VideoGridFragment depending on whether there are video
	// thumbnails to be displayed
	public void hideVideoGridFragment(Boolean hide) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		VideoGridFragment videoGridFragment = (VideoGridFragment) fragmentManager
				.findFragmentById(R.id.videoGridFragment);
		if (hide == true) {
			fragmentTransaction.hide(videoGridFragment);
		} else {
			fragmentTransaction.show(videoGridFragment);
		}
		fragmentTransaction.commit();
	}
}
