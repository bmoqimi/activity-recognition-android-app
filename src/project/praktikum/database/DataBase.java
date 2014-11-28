package project.praktikum.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper
{
	   public static final String DATABASE_NAME = "MyDBName.db";
	   public static final String TABLE_NAME = "activity";
	   public static final String COLUMN_ID = "_id";
	   public static final String COLUMN_UNKNOWN = "unknown";
	   public static final String COLUMN_IN_VEHICLE = "in_vehicle";
	   public static final String COLUMN_ON_BICYCLE = "on_bicycle";
	   public static final String COLUMN_ON_FOOT = "on_foot";
	   public static final String COLUMN_STILL = "still";
	   public static final String COLUMN_TILTING = "tilting";
	   public static final String COLUMN_CONF = "conf";
	   public static final String COLUMN_DATE = "date";
	   public static final String COLUMN_CORRECTNESS = "correct";

	   public DataBase(Context context)
	   {
	      super(context, DATABASE_NAME , null, 1);
	   }

	   @Override
	   public void onCreate(SQLiteDatabase db) {
	      // TODO Auto-generated method stub
		   
		   String createTable = "create table " + TABLE_NAME +
				      "( "
				      + COLUMN_ID
				      + " integer primary key AUTOINCREMENT, "
				      + COLUMN_UNKNOWN
				      + " integer,"
				      + COLUMN_IN_VEHICLE
				      + " integer,"
				      + COLUMN_ON_BICYCLE
				      + " integer,"
				      + COLUMN_ON_FOOT
				      + " integer,"
				      + COLUMN_STILL
				      + " integer,"
				      + COLUMN_TILTING
				      + " integer,"
					  + COLUMN_CONF
					  + " integer,"
					  + COLUMN_CORRECTNESS
					  + " integer,"
				      + COLUMN_DATE
				      + " text)";
		   
	      db.execSQL(createTable);
	   }

	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      // TODO Auto-generated method stub
	      db.execSQL("DROP TABLE IF EXISTS contacts");
	      onCreate(db);
	   }

	   public boolean insertRecord  (int unknown,int in_vehicle,int on_bicycle,int on_foot,
			   int still,int tilting,String date)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();

	      contentValues.put(COLUMN_UNKNOWN, unknown);
	      contentValues.put(COLUMN_IN_VEHICLE, in_vehicle);
	      contentValues.put(COLUMN_ON_BICYCLE, on_bicycle);
	      contentValues.put(COLUMN_ON_FOOT, on_foot);
	      contentValues.put(COLUMN_STILL, still);
	      contentValues.put(COLUMN_TILTING, tilting);
	      contentValues.put(COLUMN_DATE, date);
	      contentValues.put(COLUMN_CORRECTNESS, 0);

	      db.insert(TABLE_NAME, null, contentValues);
	      return true;
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
}
