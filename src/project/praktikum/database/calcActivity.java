package project.praktikum.database;

import java.util.ArrayList;
import java.util.Calendar;
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
		insertAddedActivities();
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
			if(res.get(i).getAct().equals("StartService"))
			{
			}
			else if(checkSameActivity(res.get(i).getAct(), res.get(i + 1).getAct()))
			{
				do
				{
					res.get(i).setFinish(res.get(i + 1).getStart());
					res.remove(i + 1);
				}while(i < res.size() - 1
						&& checkSameActivity(res.get(i).getAct(), res.get(i + 1).getAct()));
			}
			else if(res.get(i + 1).getAct().equals("Wakeup") && res.get(i).getAct().equals("Sleeping"))
			{
				res.get(i).setFinish(res.get(i + 1).getStart());
				res.remove(i + 1);
			}
		}
	}
	
	private boolean isSameDay(Date date1 , Date date2)
	{
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
		                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	private void insertAddedActivities()
	{
		for(int i=0 ; i < res.size() ; i++)
		{
			if(!(isSameDay(res.get(i).getStart(), res.get(i).getFinish())))
			{
				Date midNight = new Date();
				midNight.setTime(res.get(i).getFinish().getTime());
				midNight.setHours(0);
				midNight.setMinutes(0);
				midNight.setSeconds(0);
				Activity act = new Activity(
						res.get(i).getAct(),
						midNight,
						res.get(i).getFinish());
				res.add(i + 1, act);
				Date temp = new Date(midNight.getTime() - 1000);
				res.get(i).setFinish(temp);
			}
		}
	}
	
	private boolean checkSameActivity(String s1, String s2)
	{
//		if(s1.equals("Wakeup"))
//			s1 = "Sleeping";
//		if(s2.equals("Wakeup"))
//			s2 = "Sleeping";
		if(s1.equals(s2))
			return true;
		return false;
	}
	
	private void removeShortActivity()
	{
		for(int i = 1 ; i < res.size() ; i++)
		{
			if(compareDates(res.get(i).getStart(), res.get(i).getFinish()) && !res.get(i).getAct().equals("Sleeping"))
			{
				res.get(i).setStart(res.get(i - 1).getFinish());
//				res.remove(i);
//				i--;
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
