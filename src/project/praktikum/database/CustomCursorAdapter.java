package project.praktikum.database;

import project.praktikum.activity.recognition.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class CustomCursorAdapter  extends CursorAdapter 
{

	private LayoutInflater mInflater;
	private DataBase db;
	 
	public CustomCursorAdapter(Context context, Cursor c, int flags , DataBase db) 
	{
	  super(context, c, flags);
	  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  this.db = db;
	}
	 
	@Override
	public void bindView(View view, Context context, Cursor cursor) 
	{
		 setTextBoxes(view, cursor);
		 setCkeckBoxes(view, cursor);
	}
	 
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
	  return mInflater.inflate(R.layout.item_lv, parent, false);
	}
	 
	private void setCkeckBoxes(View v, Cursor c)
	{
		CheckBox chkTrue = (CheckBox) v.findViewById(R.id.chkTrue);
		chkTrue.setTag(c.getInt(c.getColumnIndex(db.COLUMN_ID)));
		CheckBox chkFalse = (CheckBox) v.findViewById(R.id.chkFalse);
		chkFalse.setTag(c.getInt(c.getColumnIndex(db.COLUMN_ID)));
		
		int check = c.getInt(c.getColumnIndex("correct"));
		if(check == 0)
		{
			chkTrue.setChecked(false);
			chkFalse.setChecked(false);
		}
		if(check == 1)
		{
			chkTrue.setChecked(true);
			chkFalse.setChecked(false);
		}
		if(check == 2)
		{
			chkTrue.setChecked(false);
			chkFalse.setChecked(true);
		}
		
		chkTrue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton b, boolean isChecked) {
				// TODO Auto-generated method stub 
				int i = Integer.parseInt(b.getTag().toString());
				if(b.isChecked())
				{
					db.updateRecord(i, 1);
				}
				else
				{
					db.updateRecord(i, 0);
				}
			}
		});
		
		chkFalse.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton b, boolean isChecked)
			{
				// TODO Auto-generated method stub
				int i = Integer.parseInt(b.getTag().toString());
				if(b.isChecked())
				{
					db.updateRecord(i, 2);
				}
				else
				{
					db.updateRecord(i, 0);
				}
			}
		});
	}
	 
	private void setTextBoxes(View v, Cursor c)
	{
		TextView txtDate = (TextView) v.findViewById(R.id.textView1);
		TextView txtActivity = (TextView) v.findViewById(R.id.textView2);
		TextView txtConfidence = (TextView) v.findViewById(R.id.textView3);
		txtDate.setText(c.getString(c.getColumnIndex("date")));
		txtActivity.setText(c.getString(c.getColumnIndex(db.COLUMN_ACTIVITY)));
		txtConfidence.setText(c.getString(c.getColumnIndex("conf")));
	}
}











