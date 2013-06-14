package org.ilanguage.fielddbsessionrecorder;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DetailFragment extends Fragment {
	ImageView Display;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_detail,
				container, false);
		return view;
	}

	// public void setText(String item) {
	// TextView view = (TextView) getView().findViewById(R.id.detailsText);
	// view.setText(item);
	// }

	public void setImage(int item_id) {
		// TODO Auto-generated method stub
		Display = (ImageView) getView().findViewById(R.id.IVDisplay);
		switch (item_id) {
		case R.id.image1:
			Display.setImageResource(R.drawable.test1);
			break;

		case R.id.image2:
			Display.setImageResource(R.drawable.test2);
			break;

		case R.id.image3:
			Display.setImageResource(R.drawable.test3);

			break;
		case R.id.image4:
			Display.setImageResource(R.drawable.test4);

			break;
		case R.id.image5:
			Display.setImageResource(R.drawable.test5);

			break;
		case R.id.image6:
			Display.setImageResource(R.drawable.test6);

			break;
		case R.id.image7:
			Display.setImageResource(R.drawable.test7);

			break;
		case R.id.image8:
			Display.setImageResource(R.drawable.test8);

			break;
		case R.id.image9:
			Display.setImageResource(R.drawable.test9);

			break;
		case R.id.image10:
			Display.setImageResource(R.drawable.test10);

			break;
		case R.id.image11:
			Display.setImageResource(R.drawable.test11);

			break;
		case R.id.image12:
			Display.setImageResource(R.drawable.test12);

			break;
		case R.id.image13:
			Display.setImageResource(R.drawable.test13);

			break;
		}

	}

}
