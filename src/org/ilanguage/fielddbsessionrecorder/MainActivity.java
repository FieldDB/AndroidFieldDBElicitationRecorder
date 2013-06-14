package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity implements MyListFragment.OnItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // if the wizard generated an onCreateOptionsMenu you can delete
    // it, not needed for this tutorial

  @Override
//  public void onRssItemSelected(String link) {
//    DetailFragment fragment = (DetailFragment) getFragmentManager()
//            .findFragmentById(R.id.detailFragment);
//        if (fragment != null && fragment.isInLayout()) {
//          fragment.setText(link);
//        } 
//  }

  public void onImageSelect(int item_id) {
	    DetailFragment fragment = (DetailFragment) getFragmentManager()
	            .findFragmentById(R.id.detailFragment);
	        if (fragment != null && fragment.isInLayout()) {
	          fragment.setImage(item_id);
	        } 
	  }
    
} 
