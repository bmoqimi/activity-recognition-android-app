package project.praktikum.database;

import java.util.ArrayList;
import java.util.Date;

import android.database.Cursor;

public class calcActivity {

	String activity;
	long comparisonOffset = 120000;
	Activity act = null;
	ArrayList<Activity> res = new ArrayList<Activity>();
	
	public ArrayList<Activity> Calc(Cursor c , Activity act)
	{
		if(act!=null)
			res.add(act);
		fillRes(c);
		mergeActivity();
		removeShortActivity();
		mergeActivity();
		return res;
	}
	
	private void fillRes(Cursor c)
	{
		do
		{
			Activity temp = new Activity(c.getString(1),
					c.getString(4),
					c.getString(4));
			res.add(temp);
		}while(c.moveToNext());
	}

	private void mergeActivity()
	{
		for(int i = 0 ; i < res.size() - 1 ; i++)
		{
			if(checkSameActivity(res.get(i).getAct(), res.get(i + 1).getAct()))
			{
				res.get(i).setFinish(res.get(i + 1).getFinish());
				res.remove(i + 1);
				i--;
			}
		}
	}
	
	private boolean checkSameActivity(String s1, String s2)
	{
		if(s1.equals("Wakeup"))
			s1 = "Sleeping";
		if(s2.equals("Wakeup"))
			s2 = "Sleeping";
		if(s1.equals(s2))
			return true;
		return false;
	}
	
	private void removeShortActivity()
	{
		for(int i = 0 ; i < res.size() ; i++)
		{
			if(compareDates(res.get(i).getStart(), res.get(i).getFinish()))
			{
				res.remove(i);
				i--;
			}
		}
	}
	
	
	/**
	 * 
	 * @param start
	 * @param finish
	 * @return true if duration is less that comparisonOffset else false
	 */
	private boolean compareDates(Date start , Date finish)
	{
		if(start.getTime() > finish.getTime() - comparisonOffset)
			return true;
		return false;
	}
	
	
}
