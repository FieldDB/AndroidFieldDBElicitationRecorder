package ca.ilanguage.fielddbsessionrecorder;

import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainSessionRecorderActivity extends ListActivity {
	private DatumsDbAdapter mDbHelper;
	private static final int NEW_SESSION_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SESSION_ACCESS = 0;
	private File videosFolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_session_recorder);

		// Create videos folder if not already present
		videosFolder = new File(Environment.getExternalStorageDirectory(),
				"FieldDBSessions");
		videosFolder.mkdir();

		mDbHelper = new DatumsDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case NEW_SESSION_ID:
			createNote();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			final long row_id = info.id;
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.delete); // Set Alert dialog title here
			alert.setMessage(R.string.dialog_verify_delete); // Message here

			alert.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							// TODO Also delete all video files associated with
							// this session.

							mDbHelper.deleteNote(row_id);
							fillData();
						}
					});

			alert.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = alert.create();
			alertDialog.show();

			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, SessionAccess.class);
		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, SESSION_ACCESS);
	}

	private void createNote() {
		Long id = mDbHelper.createNote("", "", "", "", "", "");
		Intent i = new Intent(this, SessionAccess.class);
		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
		startActivity(i);
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		Cursor notesCursor = mDbHelper.fetchAllDatums();
		startManagingCursor(notesCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[] { DatumsDbAdapter.KEY_FIELD1,
				DatumsDbAdapter.KEY_FIELD2, DatumsDbAdapter.KEY_FIELD3,
				DatumsDbAdapter.KEY_FIELD4, DatumsDbAdapter.KEY_FIELD5 };

		// and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.text1, R.id.text2, R.id.text3, R.id.text4,
				R.id.text5 };

		// Now create a simple cursor adapter and set it to display
		MyCursorAdapter notes = new MyCursorAdapter(this,
				R.layout.session_list_row, notesCursor, from, to, 0);
		setListAdapter(notes);

	}

	// extend the SimpleCursorAdapter to create a custom class where we
	// can override the getView to change the row colors
	private class MyCursorAdapter extends SimpleCursorAdapter {

		public MyCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// get reference to the row
			View view = super.getView(position, convertView, parent);
			// check for odd or even to set alternate colors to the row
			// background
			if (position % 2 == 0) {
				view.setBackgroundColor(Color.rgb(157, 243, 245));
			} else {
				view.setBackgroundColor(Color.rgb(205, 255, 255));
			}
			return view;
		}
	}

	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// //intent results, for those that return something
	// }

	@Override
	public void onResume() {
		super.onResume();
		// Fill data on activity resumed; updates ListView of changes
		fillData();
	}

}
