package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class DetailActivity extends Activity {
	private EditText mCouch_IDText;
	private EditText mRow_IDText;
	private EditText mField1Text;
	private EditText mField2Text;
	private EditText mField3Text;
	private EditText mField4Text;
	private EditText mField5Text;
	private Long mRowId;
	private DatumsDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Need to check if Activity has been switched to landscape mode
		// If yes, finish and go back to the start Activity
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
		}
		setContentView(R.layout.activity_detail);

//		LinearLayout videoContainer = (LinearLayout) findViewById(R.id.IVDisplayContainer);
//		videoContainer.setVisibility(LinearLayout.GONE);
		
		// Use videoURI from intent to populate fields in view
		Intent currentIntent = this.getIntent();
		String uriToString = currentIntent.getStringExtra("tag");
		Log.v("TEST", "" + uriToString);

		String[] uriParts = uriToString.split("\\.");
		String[] uriSubParts = uriParts[0].split("_");
		// long videoID = Long.parseLong(uriSubParts[2]);
		long rowID = Long.parseLong(uriSubParts[3]);

		mDbHelper = new DatumsDbAdapter(this);
		mDbHelper.open();
		mCouch_IDText = (EditText) findViewById(R.id.couch_id);
		mRow_IDText = (EditText) findViewById(R.id.row_id);
		mField1Text = (EditText) findViewById(R.id.field1);
		mField2Text = (EditText) findViewById(R.id.field2);
		mField3Text = (EditText) findViewById(R.id.field3);
		mField4Text = (EditText) findViewById(R.id.field4);
		mField5Text = (EditText) findViewById(R.id.field5);

		Button confirmButton = (Button) findViewById(R.id.confirm);
		/** Recover savedInstanceState if focus is lost and regained */
		// mRowId = (savedInstanceState == null) ? null
		// : (Long) savedInstanceState
		// .getSerializable(DatumsDbAdapter.KEY_ROWID);
		// if (mRowId == null) {
		// Bundle extras = getIntent().getExtras();
		// mRowId = extras != null ? extras.getLong(DatumsDbAdapter.KEY_ROWID)
		// : null;
		// }
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				updateSessionInfo();
				return;
			}
			//
		});

		populateFields(rowID);

	}

	private void populateFields(Long rowID) {
		if (rowID != null) {
			mDbHelper.open();
			Cursor note = mDbHelper.fetchNote(rowID);

			startManagingCursor(note);
			mRow_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_ROWID)));
			mCouch_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_COUCH_ID)));
			mField1Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1)));
			mField2Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD2)));
			mField3Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD3)));
			mField4Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD4)));
			mField5Text.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5)));
		}
	}

	private void updateSessionInfo() {
		Long mRowId = Long.parseLong(mRow_IDText.getText().toString());
		String couch_id = mCouch_IDText.getText().toString();
		String field1 = mField1Text.getText().toString();
		String field2 = mField2Text.getText().toString();
		String field3 = mField3Text.getText().toString();
		String field4 = mField4Text.getText().toString();
		String field5 = mField5Text.getText().toString();

		mDbHelper.updateNote(mRowId, couch_id, field1, field2, field3, field4,
				field5);
	}

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// saveState();
	// outState.putSerializable(DatumsDbAdapter.KEY_ROWID, mRowId);
	// }

	// @Override
	// protected void onPause() {
	// super.onPause();
	// saveState();
	// }

	// @Override
	// protected void onResume() {
	// super.onResume();
	// // populateFields(mRowId);
	// }

	// private void saveState() {
	// Long row_id = Long.parseLong(mRow_IDText.getText().toString());
	// String couch_id = mCouch_IDText.getText().toString();
	// String field1 = mField1Text.getText().toString();
	// String field2 = mField2Text.getText().toString();
	// String field3 = mField3Text.getText().toString();
	// String field4 = mField4Text.getText().toString();
	// String field5 = mField5Text.getText().toString();
	//
	// if (mRowId == null) {
	// long id = mDbHelper.createNote(couch_id, field1, field2,
	// field3, field4, field5);
	// if (id > 0) {
	// mRowId = id;
	// }
	// } else {
	// mDbHelper.updateNote(mRowId, couch_id, field1, field2, field3,
	// field4, field5);
	// }
	// }
}
