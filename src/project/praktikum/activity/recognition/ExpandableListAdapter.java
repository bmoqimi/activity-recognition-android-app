package project.praktikum.activity.recognition;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter
    {
         
 
        private LayoutInflater inflater;
        private ArrayList<Parent> parents;
 
        public ExpandableListAdapter(Context ctx , ArrayList<Parent> parents)
        {
        	this.parents = parents;
        	
            // Create Layout Inflator
            inflater = LayoutInflater.from(ctx);
        }
     
         
        // This Function used to inflate parent rows view
         
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, 
                View convertView, ViewGroup parentView)
        {
            final Parent parent = parents.get(groupPosition);
             
            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.list_group, parentView, false); 
             
            // Get grouprow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.lblListHeader)).setText(parent.getDate());
             
            //Log.i("onCheckedChanged", "isChecked: "+parent.isChecked());
             
            return convertView;
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
            ((TextView) convertView.findViewById(R.id.lblActivity)).setText(child.getActivity());
            ((TextView) convertView.findViewById(R.id.lblStart)).setText(child.getStart());
            ((TextView) convertView.findViewById(R.id.lblEnd)).setText(child.getEnd());
             
            return convertView;
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