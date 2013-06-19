package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DetailActivity extends Activity {
	private EditText mCouch_IDText;
	private EditText mSession_IDText;
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
		// If yes, finished and go back to the start Activity
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
		}
		setContentView(R.layout.activity_detail);
		mDbHelper = new DatumsDbAdapter(this);
		mDbHelper.open();
		mCouch_IDText = (EditText) findViewById(R.id.couch_id);
		mSession_IDText = (EditText) findViewById(R.id.session_id);
		mField1Text = (EditText) findViewById(R.id.field1);
		mField2Text = (EditText) findViewById(R.id.field2);
		mField3Text = (EditText) findViewById(R.id.field3);
		mField4Text = (EditText) findViewById(R.id.field4);
		mField5Text = (EditText) findViewById(R.id.field5);

		Button confirmButton = (Button) findViewById(R.id.confirm);
		/** Recover savedInstanceState if focus is lost and regained */
		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(DatumsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DatumsDbAdapter.KEY_ROWID)
					: null;
		}
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});
//		populateFields();

	}

	private void populateFields(Long sessionID) {
		if (sessionID != null) {
//		if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);
			mCouch_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_COUCH_ID)));
			mSession_IDText.setText(note.getString(note
					.getColumnIndexOrThrow(DatumsDbAdapter.KEY_ROWID)));
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(DatumsDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields(mRowId);
	}

	private void saveState() {
		Long row_id = Long.parseLong(mSession_IDText.getText().toString());
		String couch_id = mCouch_IDText.getText().toString();
		String field1 = mField1Text.getText().toString();
		String field2 = mField2Text.getText().toString();
		String field3 = mField3Text.getText().toString();
		String field4 = mField4Text.getText().toString();
		String field5 = mField5Text.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createNote(row_id, couch_id, field1, field2,
					field3, field4, field5);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateNote(mRowId, couch_id, field1, field2, field3,
					field4, field5);
		}
	}
}
