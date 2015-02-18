package project.praktikum.database;


import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper
{
	private String TAG = "Database";
	public static final String DATABASE_NAME = "MyDBName.db";
	public static final String TABLE_NAME = "activity";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ACTIVITY = "_activity";
	public static final String COLUMN_CONF = "conf";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_CORRECTNESS = "correct";
	private static DataBase mInstance = null;

	public static DataBase getInstance(Context ctx) {
        /** 
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information: 
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new DataBase(ctx.getApplicationContext());
        }
        return mInstance;
    }
	
	private DataBase(Context context)
	{
		super(context, DATABASE_NAME , null, 1);
	}
	
	public SQLiteDatabase getDB()
	{
		return this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		String createTable = "create table " + TABLE_NAME +
				"( "
				+ COLUMN_ID
				+ " integer primary key AUTOINCREMENT, "
				+ COLUMN_ACTIVITY
				+ " text, "
				+ COLUMN_CONF
				+ " integer,"
				+ COLUMN_CORRECTNESS
				+ " integer,"
				+ COLUMN_DATE
				+ " text)";

		db.execSQL(createTable);
		
		createTable = "create table timeline"+
				"( "
				+ "_id"
				+ " integer primary key AUTOINCREMENT, "
				+ "start"
				+ " text, "
				+ "end"
				+ " text, "
				+ "activity"
				+ " text)";

		db.execSQL(createTable);
		
		createTable = "create table fingerprints"+
				"( "
				+ "_id"
				+ " integer primary key AUTOINCREMENT, "
				+ "type"
				+ " text, "
				+ "bssid"
				+ " text, "
				+ "ssid"
				+ " text, "
				+ "capabilities"
				+ " text, "
				+ "frequency"
				+ " integer,"
				+ "level"
				+ " integer,"
				+ "latitude"
				+ " text, "
				+ "longitude"
				+ " text, "
				+ "date"
				+ " text)";

		db.execSQL(createTable);
		
		createTable = "create table fingerprintshome"+
				"( "
				+ "_id"
				+ " integer primary key AUTOINCREMENT, "
				+ "type"
				+ " text, "
				+ "bssid"
				+ " text, "
				+ "ssid"
				+ " text, "
				+ "capabilities"
				+ " text, "
				+ "frequency"
				+ " integer,"
				+ "level"
				+ " integer,"
				+ "latitude"
				+ " text, "
				+ "longitude"
				+ " text, "
				+ "date"
				+ " text)";

		db.execSQL(createTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS contacts");
		onCreate(db);
	}
	
	public void fillTimeLine()
	{
		Cursor c = getAllRecords(getLastDate());
		int i = c.getCount();
		if(c.moveToFirst())
		{
			String activity = "";
			String start = "";
			String end = "";
			while(c.isAfterLast() == false)
			{
				String tmp = c.getString(c.getColumnIndex(COLUMN_ACTIVITY));
				if(!tmp.equals(activity))
				{
					if(!activity.equals(""))
					{
						if(!activity.equals("Still"))
							insertTimeLine(activity, start, end);
					}
					activity = tmp;
					start = c.getString(c.getColumnIndex(COLUMN_DATE));
				}
				end = c.getString(c.getColumnIndex(COLUMN_DATE));
				c.moveToNext();
			}
			if(!activity.equals("Still"))
				insertTimeLine(activity, start, end);
		}
	}
	
	private void insertTimeLine(String activity , String start , String end)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put("activity", activity);
		contentValues.put("start", end);
		contentValues.put("end", end);
		Log.i(TAG, "Inserting into timeline" + activity);
		db.insert("timeline", null, contentValues);
	}
	
	public Cursor fetchTimeLine()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query("timeline", null, null, null, null, null, null, null);
		return res;
	}
	
	private String getLastDate()
	{
		String ans = "2000-01-01 00:00:00";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query("timeline", null, null, null, null, null, "_id DESC", "1");
		if(res.getCount() > 0)
		{
			res.moveToFirst();
			ans = res.getString(res.getColumnIndex("end"));
		}
		
		return ans;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public boolean insertRecord  (String activity,int conf,String date)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put(COLUMN_ACTIVITY, activity);
		contentValues.put(COLUMN_CONF, conf);
		contentValues.put(COLUMN_DATE, date);
		contentValues.put(COLUMN_CORRECTNESS, 0);
		Log.i(TAG, "Inserting into " + TABLE_NAME + " " + activity + " On " + date.toString());
		db.insert(TABLE_NAME, null, contentValues);
		return true;
	}

	public boolean insertFingerprintRecord(String type, String bssid, String ssid, String capabilities, int level,
			int latitude, int longitude, String date)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put("type", type);
		contentValues.put("bssid", bssid);
		contentValues.put("ssid", ssid);
		contentValues.put("capabilities", capabilities);
		contentValues.put("level", level);
		contentValues.put("latitude", latitude);
		contentValues.put("longitude", longitude);
		contentValues.put("date", date);
		Log.i(TAG, "Inserting into fingerprints on " + date.toString());
		db.insert("fingerprints", null, contentValues);
		return true;
	}
	
public boolean insertFingerprintHomeRecord(String type, String bssid, String ssid, String capabilities, int level,
			int latitude, int longitude, String date)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put("type", type);
		contentValues.put("bssid", bssid);
		contentValues.put("ssid", ssid);
		contentValues.put("capabilities", capabilities);
		contentValues.put("level", level);
		contentValues.put("latitude", latitude);
		contentValues.put("longitude", longitude);
		contentValues.put("date", date);
		Log.i(TAG, "Inserting into fingerprintshome on " + date.toString());
		db.insert("fingerprintshome", null, contentValues);
		return true;
	}
	
	public String fetchfingerprintsCount() {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT COUNT(*) FROM fingerprints";
	    SQLiteStatement statement = db.compileStatement(sql);
	    long count = statement.simpleQueryForLong();
	    return Long.toString(count);
	}
	
	public String fetchfingerprintshomeCount() {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT COUNT(*) FROM fingerprintshome";
	    SQLiteStatement statement = db.compileStatement(sql);
	    long count = statement.simpleQueryForLong();
	    return Long.toString(count);
	}
	
	public Cursor fetchfingerprintshome() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.query("fingerprintshome", null, null, null, null, null, null, null);
	}
	
	public void deleteallrowsfingerprintshome() {
		SQLiteDatabase db = this.getWritableDatabase();
	    db.delete("fingerprintshome", null, null);
	}	

	public void updateRecord(int index , int value)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_CORRECTNESS, value);
		db.update(TABLE_NAME, contentValues, "_id "+"="+index, null);
	}

	public Cursor getAllRecords()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC", null);
		return res;
	}
	
	public Cursor getAllRecords(String date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query(TABLE_NAME,
				null,
				COLUMN_DATE + " > Datetime('" + date + "')",
				null,
				null,
				null,
				null,
				null);
		return res;
	}
}
