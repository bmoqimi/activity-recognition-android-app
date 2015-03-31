package project.praktikum.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
		
		createTable = "create table timelineHistorical"+
				"( "
				+ "_id"
				+ " integer primary key AUTOINCREMENT, "
				+ "date"
				+ " text, "
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
	
	private Activity getTimeLineLastRecord(Cursor c)
	{
		Activity act = null;
		String activity = "";
		String start = "";
		String end = "";
		if(c.getCount() > 0)
		{
			c.moveToFirst();
			activity = c.getString(c.getColumnIndex("activity"));
			start = c.getString(c.getColumnIndex("start"));
			end = c.getString(c.getColumnIndex("end"));
			act = new Activity(activity, start, end);
		}
		return act;
	}
	
	public void fillTimeLine() throws Exception
	{
		
		//deteleAllTimeLineRecords();
		Cursor cr = getAllRecords(getLastDate());
		Cursor ct = fetchLasTimeLineRecord();
		if(cr.moveToFirst())
		{
			calcActivity calc = new calcActivity();
			
			ArrayList<Activity> activities = calc.Calc(cr, getTimeLineLastRecord(ct));
			for(int i = 0 ; i < activities.size() ; i++)
			{
				insertTimeLine(activities.get(i).getAct(),
						convertDateToString(activities.get(i).getStart()),
						convertDateToString(activities.get(i).getFinish()));
			}
//			while(cr.isAfterLast() == false)
//			{
//				String tmp = cr.getString(cr.getColumnIndex(COLUMN_ACTIVITY));
//				//if(ct.moveToFirst())
//				//{
//					//if(ct.getString(ct.getColumnIndex("activity")).equals(tmp)
//					//		&& compareDates(ct.getString(ct.getColumnIndex("start")),cr.getString(cr.getColumnIndex(COLUMN_DATE)) ))
//				//	{
//				//		activity = tmp;
//				//		start = ct.getString(ct.getColumnIndex("start"));
//				//		deteleTimeLineRecord(ct.getInt(ct.getColumnIndex("_id")));
//				//	}
//				//}
//				if(!tmp.equals(activity))
//				{
//					if(!activity.equals(""))
//					{
//						//if(!activity.equals("Still"))
//							insertTimeLine(activity, start, end);
//					}
//					activity = tmp;
//					start = cr.getString(cr.getColumnIndex(COLUMN_DATE));
//				}
//				end = cr.getString(cr.getColumnIndex(COLUMN_DATE));
//				cr.moveToNext();
//			}
			//if(!activity.equals("Still"))
		}
	}
	
	
	public void deteleAllTimeLineRecords()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("timeline", null, null);
	}
	
	private void deteleTimeLineRecord(int id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("timeline", "_id=" + id, null);
	}
	
	private String convertDateToString(Date input) throws ParseException
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = df.format(input);
		return date;
	}
	
	private void insertTimeLine(String activity , String start , String end)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put("activity", activity);
		contentValues.put("start", start);
		contentValues.put("end", end);
		Log.i(TAG, "Inserting into timeline" + activity);
		db.insert("timeline", null, contentValues);
	}
	
	public Cursor fetchTimeLine()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query("timeline", null, null, null, null, null, "_id DESC", null);
		return res;
	}
	
	public Cursor fetchLasTimeLineRecord()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.query("timeline", null, null, null, null, null, "_id DESC", "1");
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
	
	public int getTimelineWalkingTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='Walking' and strftime('%s', end) - strftime('%s',start)>0 and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    int result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString())/60.0);
	    }
	    catch(Exception ex) {}
	    return (result);
	}
	
	public int getTimelineRunningTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='Running' and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    int result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString())/60.0);
	    }
	    catch(Exception ex) {}
	    return (result);
	}
	
	public double getTimelineSleepingTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='Sleeping' and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    double result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString()));
	    }
	    catch(Exception ex) {
	    	return 0d;
	    }
	    return (Math.round(result/3600d));
	}
	
	public int getTimelineInVehicleTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='InVehicle' and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    int result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString())/60.0);
	    }
	    catch(Exception ex) {}
	    return (result);
	}
	
	public int getTimelineStillTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='Still' and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    int result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString())/60.0);
	    }
	    catch(Exception ex) {}
	    return (result);
	}
	
	public int getTimelineOnBicycleTime(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    String sql = "SELECT sum(strftime('%s', end) - strftime('%s',start)) FROM timeline where activity='OnBicycle' and start like '%"+date+"%'";
	    SQLiteStatement statement = db.compileStatement(sql);
	    //long count = statement.simpleQueryForLong();
	    //return Long.toString(count);
	    int result=0;
	    try{
	    result = (int)(Integer.parseInt(statement.simpleQueryForString())/60.0);
	    }
	    catch(Exception ex) {}
	    return (result);
	}
	
	public String getTimelineCount(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
	    //String sql = "SELECT count(*) FROM timeline where activity='Walking' and strftime('%Y-%d-%m', 'start')='2015-03-08'";
		 String sql = "SELECT count(*) FROM timeline where activity='Walking' and trim(substr(start,0,11)) =trim('"+date+"')";
	    SQLiteStatement statement = db.compileStatement(sql);
	    return statement.simpleQueryForString();
	   

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
		//db.delete("timeline", null, null);
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
