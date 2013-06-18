package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements
		MyListFragment.OnItemSelectedListener {

	private static final int CAMERA_PIC_REQUEST = 1337;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// TODO add onCreateOptionsMenu here

	public void onImageSelect(View v) {
		DetailFragment fragment = (DetailFragment) getFragmentManager()
				.findFragmentById(R.id.detailFragment);
		if (fragment != null && fragment.isInLayout()) {
			fragment.setImage(v.getId(), v.getTag().toString());
		}
	}

	public void onClickCamera(View v) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_PIC_REQUEST) {
			if (data.getExtras() != null) {
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

				DetailFragment fragment = (DetailFragment) getFragmentManager()
						.findFragmentById(R.id.detailFragment);
				if (fragment != null && fragment.isInLayout()) {
					ImageView img = (ImageView) findViewById(R.id.IVDisplay);
					img.setImageBitmap(thumbnail);
				}
			} 
		}
	}
}
