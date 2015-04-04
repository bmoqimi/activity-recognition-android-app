package project.praktikum.activity.recognition;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import project.praktikum.database.DataBase;
import android.annotation.SuppressLint;
import android.app.ExpandableListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ExpandableListView;
 
public class ActivityExpandableListView extends ExpandableListActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    DataBase db;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        db = DataBase.getInstance(getApplicationContext());
        
        try {
			db.fillTimeLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        loadHosts(prepareListData());
    }
 
    /*
     * Preparing the list data
     */
    
    @SuppressLint("SimpleDateFormat")
	private Date stringToDate(String aDate) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;            

     }
    
    private void loadHosts(final ArrayList<Parent> newParents)
    {
        if (newParents == null)
            return;
        if (this.getExpandableListAdapter() == null)
        {
            final ExpandableListAdapter mAdapter = new ExpandableListAdapter(this , newParents,this.getExpandableListView());
            this.setListAdapter(mAdapter);
        }
        else
        {
            ((ExpandableListAdapter)getExpandableListAdapter()).notifyDataSetChanged();
        }   
    }
    
    private void setActivityTime(Parent parent , long amount , String activity)
    {
    	if(activity.equals("Still"))
    	{
    		parent.setStill(parent.getStill() + amount);
    	}
    	
    	else if(activity.equals("Walking"))
    	{
    		parent.setWalking(parent.getWalking() + amount);
    	}
    	else if(activity.equals("Sleeping"))
    	{
    		parent.setSleeping(parent.getSleeping() + amount);
    	}
    	else if(activity.equals("InVehicle"))
    	{
    		parent.setDriving(parent.getDriving() + amount);
    	}
    	else if(activity.equals("OnBicycle"))
    	{
    		parent.setCycling(parent.getCycling() + amount);
    	}
    	else if(activity.equals("Running"))
    	{
    		parent.setRunning(parent.getRunning() + amount);
    	}
    }
    
    @SuppressWarnings("deprecation")
	private ArrayList<Parent> prepareListData() {
    	
    	final ArrayList<Parent> list = new ArrayList<Parent>();
    	
        Date currentDay = new Date();
        Date currentActivity = new Date();
    	Cursor c = db.fetchTimeLine();
    	if(c.moveToFirst())
    	{
    		do
    		{
    			Parent header = new Parent();
    			currentActivity = stringToDate(c.getString(2));
            	currentDay = currentActivity;
            	header.setDate(currentDay);
            	
            	ArrayList<Child> children = new ArrayList<Child>();
            	
    			while(currentActivity.getDate() == currentDay.getDate() && !c.isAfterLast())
    			{
    				Child mChild = new Child();
    				mChild.setActivity(c.getString(c.getColumnIndex("activity")));
    				mChild.setStart(c.getString(c.getColumnIndex("start")));
    				mChild.setEnd(c.getString(c.getColumnIndex("end")));
    				mChild.setDuration(
    						stringToDate(mChild.getEnd()).getTime() - 
    						stringToDate(mChild.getStart()).getTime());
    				children.add(mChild);
    				setActivityTime(header,
    						stringToDate(mChild.getEnd()).getTime() - stringToDate(mChild.getStart()).getTime(),
    						mChild.getActivity());
    				c.moveToNext();
    				if(!c.isAfterLast())
    					currentActivity = stringToDate(c.getString(2));
    			}
    			
    			header.setChildren(children);
    			list.add(header);
    			
    		}while(c.moveToNext());
    	}
    	return list;
    }
}