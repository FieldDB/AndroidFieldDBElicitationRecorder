package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements
		MyListFragment.OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// TODO add onCreateOptionsMenu here

	public void onImageSelect(View v) {
		Log.v("METHOD", "onImageSelect called.");
		DetailFragment fragment = (DetailFragment) getFragmentManager()
				.findFragmentById(R.id.detailFragment);
		if (fragment != null && fragment.isInLayout()) {
			fragment.setImage(v.getId(), v.getTag().toString());
		}
	}
}
