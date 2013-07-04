package org.lingsync.elicitation.collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DatumsDbAdapter {

	public static final String KEY_COUCH_ID = "couch_id";
	public static final String KEY_FIELD1 = "field1";
	public static final String KEY_FIELD2 = "field2";
	public static final String KEY_FIELD3 = "field3";
	public static final String KEY_FIELD4 = "field4";
	public static final String KEY_FIELD5 = "field5";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "DatumsDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table notes (_id integer primary key autoincrement, "
			+ "couch_id text not null, field1 text not null, field2 text not null, field3 text not null, field4 text not null, field5 text not null);";

	private static final String DATABASE_NAME = "fielddbsessions";
	private static final String DATABASE_TABLE = "notes";
	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DatumsDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DatumsDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new note using the fields provided. If the note is successfully
	 * created return the new rowId for that note, otherwise return a -1 to
	 * indicate failure.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long createNote(String couch_id, String field1, String field2,
			String field3, String field4, String field5) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_COUCH_ID, couch_id);
		initialValues.put(KEY_FIELD1, field1);
		initialValues.put(KEY_FIELD2, field2);
		initialValues.put(KEY_FIELD3, field3);
		initialValues.put(KEY_FIELD4, field4);
		initialValues.put(KEY_FIELD5, field5);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	// public Cursor fetchAllDatums() {
	//
	// return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_COUCH_ID,
	// KEY_FIELD1,
	// KEY_FIELD2, KEY_FIELD3, KEY_FIELD4, KEY_FIELD5 },
	// null, null, null, null, KEY_ROWID + " DESC");
	// }

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNote(long rowId) throws SQLException {

		Cursor mCursor;
		try {
			mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID,
					KEY_COUCH_ID, KEY_FIELD1, KEY_FIELD2, KEY_FIELD3,
					KEY_FIELD4, KEY_FIELD5 }, KEY_ROWID + "=" + rowId, null,
					null, null, null, null);
		} catch (Exception e) {
			Log.v(PrivateConstants.TAG, "Error retrieving requested database record " + rowId);
			return null;
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the datum using the details provided. The datum to be updated is
	 * specified using the rowId, and it is altered to use the field values
	 * passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateNote(long rowId, String couch_id, String field1,
			String field2, String field3, String field4, String field5) {
		ContentValues args = new ContentValues();
		args.put(KEY_COUCH_ID, couch_id);
		args.put(KEY_FIELD1, field1);
		args.put(KEY_FIELD2, field2);
		args.put(KEY_FIELD3, field3);
		args.put(KEY_FIELD4, field4);
		args.put(KEY_FIELD5, field5);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllDatums() {

		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_COUCH_ID, KEY_FIELD1, KEY_FIELD2, KEY_FIELD3, KEY_FIELD4,
				KEY_FIELD5 }, null, null, null, null, KEY_ROWID + " DESC");
	}
}
