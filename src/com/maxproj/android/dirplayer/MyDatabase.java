package com.maxproj.android.dirplayer;

import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MyDatabase {

	/* Inner class that defines the table contents */
	public static abstract class LvRowDB implements BaseColumns {
		public static final String COLUMN_NAME_PATH = "path";
		public static final String COLUMN_NAME_SELECTED = "selected";
		public static final String COLUMN_NAME_PLAYING = "playing";
	}

	private static final String INT_TYPE = " INT";
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";

	/**
	 * write list
	 */
	public static void writeList2DB(LinkedList<LvRow> ll, String tableName) {
		DbHelper dbHelper = new MyDatabase().new DbHelper(LocalConst.app, tableName, 1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.delete(tableName, null, null);
		
		for (LvRow lr : ll) {
			ContentValues values = new ContentValues();
			values.put(LvRowDB.COLUMN_NAME_PATH, lr.getPath());
			values.put(LvRowDB.COLUMN_NAME_SELECTED, lr.getSelected());
			values.put(LvRowDB.COLUMN_NAME_PLAYING, lr.getPlayingStatus());
			db.insert(tableName, null, values);
			Log.d(LocalConst.DTAG, "database read(write): " + lr.getPath());
		}
		db.close();
		return;
	}

	/**
	 * read list
	 */
	public static LinkedList<LvRow> readListFromDB(String tableName) {
		String path;
		boolean selected;
		int playing;
		LinkedList<LvRow> ll = new LinkedList<LvRow>();
		
		DbHelper dbHelper = new MyDatabase().new DbHelper(LocalConst.app, tableName, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String[] projection = {
				LvRowDB._ID,
				LvRowDB.COLUMN_NAME_PATH,
				LvRowDB.COLUMN_NAME_SELECTED,
				LvRowDB.COLUMN_NAME_PLAYING
			    };
		String sortOrder =
				LvRowDB._ID + " ASC";
		
		Cursor c = db.query(
				tableName,  // The table to query
			    projection,                               // The columns to return
			    null,                                // The columns for the WHERE clause
			    null,                            // The values for the WHERE clause
			    null,                                     // don't group the rows
			    null,                                     // don't filter by row groups
			    sortOrder                                 // The sort order
			    );
		
		int colIndex_Path = c.getColumnIndexOrThrow(LvRowDB.COLUMN_NAME_PATH);
		int colIndex_Selected = c.getColumnIndexOrThrow(LvRowDB.COLUMN_NAME_SELECTED);
		int colIndex_Playing = c.getColumnIndexOrThrow(LvRowDB.COLUMN_NAME_PLAYING);
		
		while(c.moveToNext()){
			path = c.getString(colIndex_Path);
			if(c.getInt(colIndex_Selected) == 0)
				selected = false;
			else
				selected = true;
			playing = c.getInt(colIndex_Playing);
			ll.add(new LvRow(path, selected, playing));
			
			Log.d(LocalConst.DTAG, "database read: " + path + " "+ playing);
		}
		
		c.close();
		db.close();
		return ll;
	}




	public class DbHelper extends SQLiteOpenHelper {
		public int ver;
		public String dbName;
		public String dbCreateStr;
		public String dbDelStr;

		public DbHelper(Context context, String dbName, int ver) {
			super(context, dbName, null, ver);
			this.dbName = dbName;
			this.dbCreateStr = "CREATE TABLE " + dbName + " (" + LvRowDB._ID
					+ " INTEGER PRIMARY KEY," + LvRowDB.COLUMN_NAME_PATH
					+ TEXT_TYPE + COMMA_SEP + LvRowDB.COLUMN_NAME_SELECTED
					+ INT_TYPE + COMMA_SEP + LvRowDB.COLUMN_NAME_PLAYING
					+ INT_TYPE + " )";
			this.dbDelStr = "DROP TABLE IF EXISTS " + dbName;
			this.ver = ver;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(dbCreateStr);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL(dbDelStr);
			onCreate(db);
		}
	}
}
