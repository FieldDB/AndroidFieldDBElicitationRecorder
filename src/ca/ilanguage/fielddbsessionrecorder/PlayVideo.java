package ca.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideo extends Activity {
	VideoView Display;
	MediaController mediaController;
	String filename;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_video);

		Display = (VideoView) findViewById(R.id.IVDisplay);
		mediaController = new MediaController(this);

		if (savedInstanceState != null
				&& savedInstanceState.getString("videoTag") != null) {
			String lastVideoFile = (String) savedInstanceState
					.getString("videoTag");
			filename = lastVideoFile;
		} else {
			filename = getIntent().getStringExtra("videoFilename");
		}
		setVideo(filename);

	}

	public void setVideo(String tag) {
		filename = tag;
		// Find video file in MediaStore
		ContentResolver cr = getContentResolver();
		Uri videosUri = MediaStore.Video.Media.getContentUri("external");
		String[] projection = { MediaStore.Video.VideoColumns.DATA };
		Cursor cursor;
		try {
			cursor = cr.query(videosUri, projection,
					MediaStore.Video.VideoColumns.TITLE + " LIKE ?",
					new String[] { filename }, null);

			cursor.moveToFirst();
		} catch (Exception e) {
			return;
		}
		int columnIndex = cursor.getColumnIndex(projection[0]);
		String videoId = cursor.getString(columnIndex);
		cursor.close();

		// Get Uri to video and set it to display
		Uri vidUri = Uri.parse(videoId);
		mediaController.setAnchorView(Display);
		Display.setMediaController(mediaController);
		Display.setVideoURI(vidUri);
		Display.requestFocus();
		Display.bringToFront();
		Display.start();
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (filename != null && !filename.isEmpty()) {
			outState.putString("videoTag", filename);
		}
	}
}
