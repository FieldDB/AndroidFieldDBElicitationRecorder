package org.ilanguage.fielddbsessionrecorder;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class DetailFragment extends Fragment {
	VideoView Display;

	private DatumsDbAdapter mDbHelper;

	private EditText mCouch_IDText;
	private EditText mRow_IDText;
	private EditText mField1Text;
	private EditText mField2Text;
	private EditText mField3Text;
	private EditText mField4Text;
	private EditText mField5Text;

	private TextView warningText;
	private LinearLayout detailContainer;

	PublicInterface mCallback;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (PublicInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PublicInterface");
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session_detail,
				container, false);

		mDbHelper = new DatumsDbAdapter(view.getContext());

		
		mCouch_IDText = (EditText) view.findViewById(R.id.couch_id);
		mRow_IDText = (EditText) view.findViewById(R.id.row_id);
		mField1Text = (EditText) view.findViewById(R.id.field1);
		mField2Text = (EditText) view.findViewById(R.id.field2);
		mField3Text = (EditText) view.findViewById(R.id.field3);
		mField4Text = (EditText) view.findViewById(R.id.field4);
		mField5Text = (EditText) view.findViewById(R.id.field5);

		warningText = (TextView) view.findViewById(R.id.warning_text);
		detailContainer = (LinearLayout) view
				.findViewById(R.id.main_detail_container);
		if ((mRow_IDText.getText().toString()).equals("")) {
			hideSessionInfoInFragment();
		} else {
			showSessionInfoInFragment();
		}

		Button confirmButton = (Button) view.findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				updateSessionInfoInFragment(v.getContext());
				mCallback.updateThumbnails();
			}
		});

		return view;
	}

	public void updateSessionInfoInFragment(Context c) {

		mDbHelper = new DatumsDbAdapter(c);

		mDbHelper.open();

		if ((mRow_IDText.getText().toString()).equals("")) {
			return;
		}

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

	public void showSessionInfoInFragment() {
		warningText.setVisibility(TextView.GONE);
		detailContainer.setVisibility(LinearLayout.VISIBLE);

	}

	public void hideSessionInfoInFragment() {
		warningText.setVisibility(TextView.VISIBLE);
		detailContainer.setVisibility(LinearLayout.GONE);
	}

	public void populateFieldsInFragment(Long rowID) {
		if (rowID != null) {
			mDbHelper.open();
			Cursor note = mDbHelper.fetchNote(rowID);

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
}
