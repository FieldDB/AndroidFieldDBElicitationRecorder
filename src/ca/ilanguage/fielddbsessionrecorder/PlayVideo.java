package ca.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
		Uri vidUri = Uri.parse(tag);
		Log.v("TEST", "tag in setVideo is " + filename);
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
