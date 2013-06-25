package org.ilanguage.fielddbsessionrecorder;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoFragment extends Fragment {
	VideoView Display;
	// TextView Text;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_video,
				container, false);

		return view;
	}

	public void setVideo(String tag) {

		// Text = (TextView) getView().findViewById(R.id.detailsText);
		// Text.setText(tag);
		Uri vidUri = Uri.parse(tag);
		Display = (VideoView) getView().findViewById(R.id.IVDisplay);
		MediaController mediaController = new MediaController(getView()
				.getContext());
		mediaController.setAnchorView(Display);
		Display.setMediaController(mediaController);
		Display.setVideoURI(vidUri);
		Display.requestFocus();
		Display.bringToFront();
		Display.start();
	}
	
	
}
