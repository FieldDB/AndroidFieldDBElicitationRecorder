package ca.ilanguage.fielddbsessionrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class EditSessionFragment extends Fragment {
	private EditText mRow_IDText;
	private EditText mCouch_IDText;
	private EditText mField1Text;
	private EditText mField2Text;
	private EditText mField3Text;
	private EditText mField4Text;
	private EditText mField5Text;
	private Long mRowId;
	private DatumsDbAdapter mDbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_session, container,
				false);

		mDbHelper = new DatumsDbAdapter(view.getContext());
		mDbHelper.open();

		mRow_IDText = (EditText) view.findViewById(R.id.row_id);
		mCouch_IDText = (EditText) view.findViewById(R.id.couch_id);
		mField1Text = (EditText) view.findViewById(R.id.field1);
		mField2Text = (EditText) view.findViewById(R.id.field2);
		mField3Text = (EditText) view.findViewById(R.id.field3);
		mField4Text = (EditText) view.findViewById(R.id.field4);
		mField5Text = (EditText) view.findViewById(R.id.field5);

		Button confirmButton = (Button) view.findViewById(R.id.confirm);

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				saveState();
				
				AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
				alert.setTitle(R.string.notification); 
				alert.setMessage(R.string.saved);

				alert.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alert.create();
				alertDialog.show();
			}
		});

		/** Recover savedInstanceState if focus is lost and regained */
		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(DatumsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getActivity().getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DatumsDbAdapter.KEY_ROWID)
					: null;
			populateFields();
		}
		return view;
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			// startManagingCursor(note);
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

	// SavedInstanceState

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mRowId != null) {
			saveState();
			outState.putSerializable(DatumsDbAdapter.KEY_ROWID, mRowId);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void saveState() {
		String couch_id = mCouch_IDText.getText().toString();
		String field1 = mField1Text.getText().toString();
		String field2 = mField2Text.getText().toString();
		String field3 = mField3Text.getText().toString();
		String field4 = mField4Text.getText().toString();
		String field5 = mField5Text.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createNote(couch_id, field1, field2, field3,
					field4, field5);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateNote(mRowId, couch_id, field1, field2, field3,
					field4, field5);
		}
	}
}
