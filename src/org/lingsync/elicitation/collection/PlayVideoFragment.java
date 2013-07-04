package org.lingsync.elicitation.collection;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideoFragment extends Fragment {
	VideoView Display;
	MediaController mediaController;
	String filename;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_play_video, container,
				false);

		Display = (VideoView) view.findViewById(R.id.IVDisplay);
		mediaController = new MediaController(view.getContext());

		if (savedInstanceState != null
				&& savedInstanceState.getString("videoTag") != null) {
			String lastVideoFile = (String) savedInstanceState
					.getString("videoTag");
			filename = lastVideoFile;
		} else {
			filename = getActivity().getIntent()
					.getStringExtra("videoFilename");
		}
		return view;
	}

	public void setVideo(String tag) {
		filename = tag;

		// Find video file in MediaStore
		ContentResolver cr = getActivity().getContentResolver();
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
		Display.setTag(R.id.VIDEO_FILENAME_TAG_KEY, tag);
		Display.start();
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (filename != null && !filename.isEmpty()) {
			outState.putString("videoTag", filename);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Play selected video on launch
		setVideo(filename);
	}
}
