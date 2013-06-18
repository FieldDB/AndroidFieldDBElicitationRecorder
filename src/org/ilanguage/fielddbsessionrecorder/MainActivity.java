package org.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity implements
		MyListFragment.OnItemSelectedListener {

	private static final int CAMERA_VID_REQUEST = 1337;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// TODO add onCreateOptionsMenu here

	public void onClickCamera(View v) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

		// Create new video file
		File videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdirs();
		String video_filename = "fielddb_session_" + System.currentTimeMillis()
				+ ".mp4";
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

					 MyListFragment mainFragment = (MyListFragment)
					 getFragmentManager().findFragmentById(R.id.listFragment);
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
}
