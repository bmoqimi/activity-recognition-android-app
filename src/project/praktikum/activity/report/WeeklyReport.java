package project.praktikum.activity.report;

import project.praktikum.activity.recognition.R;
import project.praktikum.activity.recognition.R.id;
import project.praktikum.activity.recognition.R.layout;
import project.praktikum.activity.recognition.R.menu;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport.AxisBoundsStatus;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import project.praktikum.database.DataBase;
import android.graphics.Color;

public class WeeklyReport extends Activity {
	private int mYear;
	private int mMonth;
	private int mDay;
	private GraphView graph;

	public static StringBuilder today;
	static final int DATE_DIALOG_ID = 0;
	DataBase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weekly_report);
		
		db = DataBase.getInstance(getApplicationContext());
		//db.deteleAllTimeLineRecords();
		db.fillTimeLine();

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		StringBuilder today = new StringBuilder().append(mYear).append("-")
				.append(String.format("%02d", mMonth + 1)).append("-")
				.append(String.format("%02d", mDay));

		String day1;
		String day2;
		String day3;
		String day4;
		String day5;
		String day6;
		String day7;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		day1 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day2 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day3 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day4 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day5 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day6 = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		day7 = dateFormat.format(cal.getTime());
		//
		// Toast.makeText(this, String.valueOf(today.toString()),
		// Toast.LENGTH_LONG).show();

		graph = (GraphView) findViewById(R.id.graph);
		LineGraphSeries<DataPoint> series;
		series = new LineGraphSeries<DataPoint>(new DataPoint[] {
				new DataPoint(0, db.getTimelineWalkingTime(today.toString())),
				new DataPoint(1, db.getTimelineWalkingTime(day2)),
				new DataPoint(2, db.getTimelineWalkingTime(day3)),
				new DataPoint(3, db.getTimelineWalkingTime(day4)),
				new DataPoint(4, db.getTimelineWalkingTime(day5)),
				new DataPoint(5, db.getTimelineWalkingTime(day6)),
				new DataPoint(6, db.getTimelineWalkingTime(day7)) });

		series.setTitle("W");
		series.setColor(Color.BLUE);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(2);
		series.setThickness(1);
		graph.addSeries(series);

		series = new LineGraphSeries<DataPoint>(new DataPoint[] {
				new DataPoint(0, db.getTimelineRunningTime(today.toString())),
				new DataPoint(1, db.getTimelineRunningTime(day2)),
				new DataPoint(2, db.getTimelineRunningTime(day3)),
				new DataPoint(3, db.getTimelineRunningTime(day4)),
				new DataPoint(4, db.getTimelineRunningTime(day5)),
				new DataPoint(5, db.getTimelineRunningTime(day6)),
				new DataPoint(6, db.getTimelineRunningTime(day7)) });

		series.setTitle("R");
		series.setColor(Color.RED);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(2);
		series.setThickness(1);
		graph.addSeries(series);
		
		series = new LineGraphSeries<DataPoint>(new DataPoint[] {
				new DataPoint(0, db.getTimelineInVehicleTime(today.toString())),
				new DataPoint(1, db.getTimelineInVehicleTime(day2)),
				new DataPoint(2, db.getTimelineInVehicleTime(day3)),
				new DataPoint(3, db.getTimelineInVehicleTime(day4)),
				new DataPoint(4, db.getTimelineInVehicleTime(day5)),
				new DataPoint(5, db.getTimelineInVehicleTime(day6)),
				new DataPoint(6, db.getTimelineInVehicleTime(day7)) });

		series.setTitle("R");
		series.setColor(Color.GREEN);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(2);
		series.setThickness(1);
		graph.addSeries(series);
		
		series = new LineGraphSeries<DataPoint>(new DataPoint[] {
				new DataPoint(0, db.getTimelineInVehicleTime(today.toString())),
				new DataPoint(1, db.getTimelineInVehicleTime(day2)),
				new DataPoint(2, db.getTimelineInVehicleTime(day3)),
				new DataPoint(3, db.getTimelineInVehicleTime(day4)),
				new DataPoint(4, db.getTimelineInVehicleTime(day5)),
				new DataPoint(5, db.getTimelineInVehicleTime(day6)),
				new DataPoint(6, db.getTimelineInVehicleTime(day7)) });

		series.setTitle("R");
		series.setColor(Color.BLACK);
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(2);
		series.setThickness(1);
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(1);

		StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
		staticLabelsFormatter.setHorizontalLabels(new String[] {today.toString().substring(today.toString().length()-2),
				day2.substring(day2.length()-2),
				day3.substring(day3.length()-2),
				day4.substring(day4.length()-2),
				day5.substring(day5.length()-2),
				day6.substring(day6.length()-2),
				day7.substring(day7.length()-2)});
		staticLabelsFormatter.setDynamicLabelFormatter(new DefaultLabelFormatter(nf, nf));
		graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
		graph.getGridLabelRenderer().setNumHorizontalLabels(7); 
		graph.addSeries(series);

		GraphView graph2 = (GraphView) findViewById(R.id.graph2);
		BarGraphSeries<DataPoint> series2 = new BarGraphSeries<DataPoint>(
				new DataPoint[] {
						new DataPoint(0, db.getTimelineSleepingTime(today
								.toString())),
						new DataPoint(1, db.getTimelineSleepingTime(day2)),
						new DataPoint(2, db.getTimelineSleepingTime(day3)),
						new DataPoint(3, db.getTimelineSleepingTime(day4)),
						new DataPoint(4, db.getTimelineSleepingTime(day5)),
						new DataPoint(5, db.getTimelineSleepingTime(day6)),
						new DataPoint(6, db.getTimelineSleepingTime(day7)) });

		series2.setValueDependentColor(new ValueDependentColor<DataPoint>() {
			@Override
			public int get(DataPoint data) {
				return Color.rgb((int) data.getX() * 255 / 4,
						(int) Math.abs(data.getY() * 255 / 6), 100);
			}
		});

		series2.setSpacing(0);

		// draw values on top
		series2.setDrawValuesOnTop(true);
		series2.setValuesOnTopColor(Color.BLACK);

		try {
			NumberFormat nf2 = NumberFormat.getInstance();
			nf2.setMinimumFractionDigits(1);

			StaticLabelsFormatter staticLabelsFormatter2 = new StaticLabelsFormatter(graph2);
			staticLabelsFormatter2.setHorizontalLabels(new String[] {today.toString().substring(today.toString().length()-2),
					day2.substring(day2.length()-2),
					day3.substring(day3.length()-2),
					day4.substring(day4.length()-2),
					day5.substring(day5.length()-2),
					day6.substring(day6.length()-2),
					day7.substring(day7.length()-2)});
			staticLabelsFormatter2.setDynamicLabelFormatter(new DefaultLabelFormatter(nf2, nf2));
			graph2.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter2);
			graph2.getGridLabelRenderer().setNumHorizontalLabels(7); 
			graph2.addSeries(series2);
		} catch (Exception ex) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weekly_report, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
