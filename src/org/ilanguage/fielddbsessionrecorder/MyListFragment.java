package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MyListFragment extends Fragment {
	
//	ImageView Display;

	private OnItemSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_overview,
				container, false);
		
		ImageView im1 = (ImageView) view.findViewById(R.id.image1);
		ImageView im2 = (ImageView) view.findViewById(R.id.image2);
		ImageView im3 = (ImageView) view.findViewById(R.id.image3);
		ImageView im4 = (ImageView) view.findViewById(R.id.image4);
		ImageView im5 = (ImageView) view.findViewById(R.id.image5);
		ImageView im6 = (ImageView) view.findViewById(R.id.image6);
		ImageView im7 = (ImageView) view.findViewById(R.id.image7);
		ImageView im8 = (ImageView) view.findViewById(R.id.image8);
		ImageView im9 = (ImageView) view.findViewById(R.id.image9);
		ImageView im10 = (ImageView) view.findViewById(R.id.image10);
		ImageView im11 = (ImageView) view.findViewById(R.id.image11);
		ImageView im12 = (ImageView) view.findViewById(R.id.image12);
		ImageView im13 = (ImageView) view.findViewById(R.id.image13);

		im1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image1);
			}
		});
		
		im2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image2);
			}
		});
		
		im3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image3);
			}
		});

		im4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image4);
			}
		});
		
		im5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image5);
			}
		});
		
		im6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image6);
			}
		});
		
		im7.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image7);
			}
		});
		
		im8.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image8);
			}
		});
		
		im9.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image9);
			}
		});
		
		im10.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image10);
			}
		});
		
		im11.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image11);
			}
		});
		
		im12.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image12);
			}
		});
		
		im13.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateImage(R.id.image13);
			}
		});
//		im2.setOnClickListener(this);

//		Button button = (Button) view.findViewById(R.id.button1);
//		button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				updateDetail();
//			}
//		});
		return view;
	}

	public interface OnItemSelectedListener {
//		public void onRssItemSelected(String link);
		public void onImageSelect(int item_id);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnItemSelectedListener) {
			listener = (OnItemSelectedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MyListFragment.OnItemSelectedListener");
		}
	}

	// May also be triggered from the Activity
//	public void updateDetail() {
//		// Create fake data
//		String newTime = String.valueOf(System.currentTimeMillis());
//		// Send data to Activity
//		listener.onRssItemSelected(newTime);
//	}
	
	public void updateImage(int item_id) {
		listener.onImageSelect(item_id);
	}
}
