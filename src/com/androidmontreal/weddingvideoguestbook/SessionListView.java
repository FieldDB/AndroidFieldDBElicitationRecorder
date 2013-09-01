package com.androidmontreal.weddingvideoguestbook;

import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SessionListView extends ListActivity {
	private DatumsDbAdapter mDbHelper;
	private static final int VIDEO_GALLERY_VIEW_ID = Menu.FIRST;
	private static final int NEW_SESSION_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int SESSION_ACCESS = 0;
	private File videosFolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_list);

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
		menu.add(0, VIDEO_GALLERY_VIEW_ID, 0, R.string.menu_view_video_gallery);
		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case VIDEO_GALLERY_VIEW_ID:
			showVideoGallery();
			return true;
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
			alert.setTitle(R.string.delete);
			alert.setMessage(R.string.dialog_verify_delete);

			alert.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Delete session and all videos from that session

							// Query for all videos on external storage
							ContentResolver cr = getContentResolver();
							String[] proj = { MediaStore.Video.Media._ID,
									MediaStore.Video.Media.TITLE };

							Cursor c = cr
									.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
											proj, null, null, null);
							if (c.moveToFirst()) {
								do {
									int videoId = c.getInt(0);
									int videoTitleIndex = c
											.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
									String videoTitle = c
											.getString(videoTitleIndex);
									String[] videoTitleParts = videoTitle
											.split("[.]");

									String[] videoTitleSubParts = videoTitleParts[0]
											.split("_");
									if (videoTitleSubParts[0].equals(PrivateConstants.DATA_KEYWORD)) {
										Long rowID = Long
												.parseLong(videoTitleSubParts[3]);
										if (rowID == row_id) {
											// Delete video and thumbnail that
											// matches session id
											// Delete video and
											// associated thumbnail
											cr.delete(
													MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
													MediaStore.Video.VideoColumns._ID
															+ " = ?",
													new String[] { "" + videoId });

											cr.delete(
													MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
													MediaStore.Video.Thumbnails.VIDEO_ID
															+ " = ?",
													new String[] { "" + videoId });

										}
									}
								} while (c.moveToNext());
							}
							c.close();
							
							// Delete session
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

	private void showVideoGallery() {
		Intent i = new Intent(this, GalleryActivity.class);
		startActivity(i);
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

		// Now create a simple cursor adapter and set it to display, layout
		// depending on device size
		// Check for device size to determine size of gallery images
		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		MyCursorAdapter notes;
		if (tabletSize == true) {
			notes = new MyCursorAdapter(this, R.layout.session_list_row,
					notesCursor, from, to, 0);
		} else {
			notes = new MyCursorAdapter(this, R.layout.session_list_row_phones,
					notesCursor, from, to, 0);
		}
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
				view.setBackgroundColor(Color.parseColor("#DEDEDE"));
			} else {
				view.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}
			return view;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Fill data on activity resumed; updates ListView of changes
		fillData();
	}

}
