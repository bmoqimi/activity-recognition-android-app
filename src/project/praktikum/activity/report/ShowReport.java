package project.praktikum.activity.report;

import java.text.NumberFormat;
import java.util.Calendar;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import project.praktikum.activity.recognition.R;
import project.praktikum.database.DataBase;
import android.R.string;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class ShowReport extends Activity {

	private int mYear;
	private int mMonth;
	private int mDay;
	private GraphView graph;
	private TextView mDateDisplay;
	private Button mPickDate;
	private TextView mStepsDisplay;
	private TextView mCaloriesDisplay;
	private String  TAG = "ReportGeneration";

	public static StringBuilder today;
	static final int DATE_DIALOG_ID = 0;
	DataBase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		db = DataBase.getInstance(getApplicationContext());
		//db.deteleAllTimeLineRecords();
		try {
			db.fillTimeLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "database error occured");
			e.printStackTrace();
		}

		mDateDisplay = (TextView) findViewById(R.id.showDate);
		mStepsDisplay= (TextView) findViewById(R.id.textViewSteps);
		mCaloriesDisplay= (TextView) findViewById(R.id.textViewCalories);
		mPickDate = (Button) findViewById(R.id.datePickerButton);

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		StringBuilder today = new StringBuilder()
		.append(mYear).append("-").append(String.format("%02d", mMonth + 1)).append("-")
		.append(String.format("%02d",mDay));
		
		this.mDateDisplay.setText(today);
//		
//		 Toast.makeText(this, String.valueOf(today.toString()),
//		 Toast.LENGTH_LONG).show();

		graph = (GraphView) findViewById(R.id.graph);
		BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(
				new DataPoint[] {
						new DataPoint(1, db.getTimelineWalkingTime(today.toString())),
						new DataPoint(2, db.getTimelineRunningTime(today.toString())),
						new DataPoint(3, db.getTimelineSleepingTime(today.toString())),
						new DataPoint(4, db.getTimelineOnBicycleTime(today.toString())),
						new DataPoint(5, db.getTimelineInVehicleTime(today.toString())) 
						});
		
		series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
		    @Override
		    public int get(DataPoint data) {
		    return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
		    }
		});

		series.setSpacing(10);

		// draw values on top
		series.setDrawValuesOnTop(true);
		series.setValuesOnTopColor(Color.BLACK);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(1);

		StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
		staticLabelsFormatter.setHorizontalLabels(new String[] {"W","R","S","B","V"});
		staticLabelsFormatter.setDynamicLabelFormatter(new DefaultLabelFormatter(nf, nf));
		graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
		graph.getGridLabelRenderer().setNumHorizontalLabels(5); 
		graph.addSeries(series);

		mPickDate.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(DATE_DIALOG_ID);
	        }
	    });
		
		//https://transportation.stanford.edu/pdf/caloriecalc_bike.pdf
		int bikingTime= db.getTimelineOnBicycleTime(today.toString());
		int steps = db.getTimelineWalkingTime(today.toString())*100+
		db.getTimelineRunningTime(today.toString())*180;
		String remainingSteps;
		if(steps>=6000)
			remainingSteps="\nGoal is reached.";
		else
			remainingSteps="\n"+String.valueOf(6000-steps)+" steps are remained.";
			
		this.mStepsDisplay.setText(new StringBuilder().append("\n\nTotal number of steps are: ")
				.append(String.valueOf(steps)).append("\nYour goal is: 6000 steps").append(remainingSteps)
				.append("\n\n"));
		
		// 0.045
		this.mCaloriesDisplay.setText(new StringBuilder().append("Total amount of calories burned is: ")
				.append(String.valueOf((int)Math.ceil(steps* 0.045)+(bikingTime*10))).append("\n"));

	}

	private void updateDisplay() {
		
		today = new StringBuilder()
		.append(mYear).append("-").append(String.format("%02d", mMonth + 1)).append("-")
		.append(String.format("%02d",mDay));
		
		this.mDateDisplay.setText(today);
		
		graph.removeAllSeries();
		BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(
				new DataPoint[] {
						new DataPoint(1, db.getTimelineWalkingTime(today.toString())),
						new DataPoint(2, db.getTimelineRunningTime(today.toString())),
						new DataPoint(3, db.getTimelineSleepingTime(today.toString())),
						new DataPoint(4, db.getTimelineOnBicycleTime(today.toString())),
						new DataPoint(5, db.getTimelineInVehicleTime(today.toString())) 
						});
		series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
		    @Override
		    public int get(DataPoint data) {
		    return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
		    }
		});

		series.setSpacing(10);

		// draw values on top
		series.setDrawValuesOnTop(true);
		series.setValuesOnTopColor(Color.BLACK);
		graph.addSeries(series);
		
		//https://transportation.stanford.edu/pdf/caloriecalc_bike.pdf
		int bikingTime= db.getTimelineOnBicycleTime(today.toString());
		int steps = db.getTimelineWalkingTime(today.toString())*100+
		db.getTimelineRunningTime(today.toString())*180;
		String remainingSteps;
		if(steps>=6000)
			remainingSteps="\nGoal is reached.";
		else
			remainingSteps="\n"+String.valueOf(6000-steps)+" steps are remained.";
			
		this.mStepsDisplay.setText(new StringBuilder().append("\n\nTotal number of steps are: ")
				.append(String.valueOf(steps)).append("\nYour goal is: 6000 steps").append(remainingSteps)
				.append("\n\n"));
		
		// 0.045
		this.mCaloriesDisplay.setText(new StringBuilder().append("Total amount of calories burned(via walk,run and bike): ")
				.append(String.valueOf((int)Math.ceil(steps* 0.045)+(bikingTime*10))).append("\n"));

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * public boolean onMenuItemSelected(int featureId, MenuItem item) {
	 * mainWifi.startScan(); mainText.setText("Starting Scan"); return
	 * super.onMenuItemSelected(featureId, item); }
	 */

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
	}

}