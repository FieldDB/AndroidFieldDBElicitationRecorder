package org.ilanguage.fielddbsessionrecorder;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoFragment extends Fragment {
	VideoView Display;
	MediaController mediaController;
	String filename;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_video,
				container, false);
		Display = (VideoView) view.findViewById(R.id.IVDisplay);
		mediaController = new MediaController(view.getContext());

		if (savedInstanceState != null && savedInstanceState
				.getString("videoTag") != null) {
			String lastVideoFile = (String) savedInstanceState
					.getString("videoTag");
			setVideo(lastVideoFile);
		}

		return view;
	}

	public void setVideo(String tag) {
		filename = tag;
		Uri vidUri = Uri.parse(tag);

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
