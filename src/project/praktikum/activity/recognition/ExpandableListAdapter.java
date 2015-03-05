package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter
    {
         
 
        private LayoutInflater inflater;
        private ArrayList<Parent> parents;
        ExpandableListView exList;
        private Context ctx;
 
        public ExpandableListAdapter(Context ctx , ArrayList<Parent> parents, ExpandableListView exList)
        {
        	this.parents = parents;
        	
            // Create Layout Inflator
        	this.ctx = ctx;
            inflater = LayoutInflater.from(ctx);
            this.exList = exList;
        }
     
         
        // This Function used to inflate parent rows view
         
        @SuppressLint("SimpleDateFormat")
		@Override
        public View getGroupView(int groupPosition, boolean isExpanded, 
                View convertView, ViewGroup parentView)
        {
            final Parent parent = parents.get(groupPosition);
             
            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.list_group, parentView, false); 
             
            // Get grouprow.xml file elements and set values
            String date = new SimpleDateFormat("dd-MM-yyyy").format(parent.getDate());
            ((TextView) convertView.findViewById(R.id.lblListHeader)).setText(date);
            ((TextView) convertView.findViewById(R.id.lblListStill)).setText(
            		String.valueOf(getHourString(parent.getStill())));
            ((TextView) convertView.findViewById(R.id.lblListSleep)).setText(
            		String.valueOf(getHourString(parent.getSleeping())));
            ((TextView) convertView.findViewById(R.id.TextWalking)).setText(
            		String.valueOf(getHourString(parent.getWalking())));
            ((TextView) convertView.findViewById(R.id.TextRunning)).setText(
            		String.valueOf(getHourString(parent.getRunning())));
            ((TextView) convertView.findViewById(R.id.TextDriving)).setText(
            		String.valueOf(getHourString(parent.getDriving())));
            ((TextView) convertView.findViewById(R.id.TextCycling)).setText(
            		String.valueOf(getHourString(parent.getCycling())));
             
            //Log.i("onCheckedChanged", "isChecked: "+parent.isChecked());
            exList.setDividerHeight(20);
            return convertView;
        }
        
        private String getHourString(long input)
        {
        	String ans = "";
        	long mins = input / 60000; 
        	long hour = mins / 60;
        	mins = mins % 60;
        	ans = hour<10?"0" + String.valueOf(hour):String.valueOf(hour);
        	ans += ":";
        	ans += mins<10?"0" + String.valueOf(mins):String.valueOf(mins);
        	return ans;
        }
 
         
        // This Function used to inflate child rows view
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
                View convertView, ViewGroup parentView)
        {
            final Parent parent = parents.get(groupPosition);
            final Child child = parent.getChildren().get(childPosition);
             
            // Inflate childrow.xml file for child rows
            convertView = inflater.inflate(R.layout.list_item, parentView, false);
             
            // Get childrow.xml file elements and set values
            ((ImageView) convertView.findViewById(R.id.imgActivity)).setImageDrawable(getImgRes(child.getActivity()));
            ((TextView) convertView.findViewById(R.id.lblStart)).setText(child.getStart());
            ((TextView) convertView.findViewById(R.id.lblEnd)).setText(child.getEnd());
            ((TextView) convertView.findViewById(R.id.lblDuration)).setText("Duration : "
            		+ getHourString(child.getDuration()));
            exList.setDividerHeight(0);
            return convertView;
        }
 
        private Drawable getImgRes(String activity)
        {
        	if(activity.equals("Still"))
        		return ctx.getResources().getDrawable(R.drawable.standing);
        	else if(activity.equals("Running"))
        		return ctx.getResources().getDrawable(R.drawable.running);
        	else if(activity.equals("OnBicycle"))
        		return ctx.getResources().getDrawable(R.drawable.biking);
        	else if(activity.equals("InVehicle"))
        		return ctx.getResources().getDrawable(R.drawable.driving);
        	else if(activity.equals("Walking"))
        		return ctx.getResources().getDrawable(R.drawable.walking);
        	else
        		return ctx.getResources().getDrawable(R.drawable.sleeping);
        }
         
        @Override
        public Object getChild(int groupPosition, int childPosition)
        {
            //Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
            return parents.get(groupPosition).getChildren().get(childPosition);
        }
 
        //Call when child row clicked
        @Override
        public long getChildId(int groupPosition, int childPosition)
        {
            /****** When Child row clicked then this function call *******/
             
            //Log.i("Noise", "parent == "+groupPosition+"=  child : =="+childPosition);
            return childPosition;
        }
 
        @Override
        public int getChildrenCount(int groupPosition)
        {
            int size=0;
            if(parents.get(groupPosition).getChildren()!=null)
                size = parents.get(groupPosition).getChildren().size();
            return size;
        }
      
         
        @Override
        public Object getGroup(int groupPosition)
        {
            Log.i("Parent", groupPosition+"=  getGroup ");
             
            return parents.get(groupPosition);
        }
 
        @Override
        public int getGroupCount()
        {
            return parents.size();
        }
 
        //Call when parent row clicked
        @Override
        public long getGroupId(int groupPosition)
        {    
            return groupPosition;
        }
 
        @Override
        public void notifyDataSetChanged()
        {
            // Refresh List rows
            super.notifyDataSetChanged();
        }
 
        @Override
        public boolean isEmpty()
        {
            return ((parents == null) || parents.isEmpty());
        }
 
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition)
        {
            return true;
        }
 
        @Override
        public boolean hasStableIds()
        {
            return true;
        }
 
        @Override
        public boolean areAllItemsEnabled()
        {
            return true;
        }
    }