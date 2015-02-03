package project.praktikum.activity.report;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import project.praktikum.activity.recognition.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class ShowReport extends Activity{


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		GraphView graph = (GraphView) findViewById(R.id.graph);
		BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(new DataPoint[] {
		          new DataPoint(1, 124),
		          new DataPoint(2, 56),
		          new DataPoint(3, 115),
		          new DataPoint(4, 913),
		          new DataPoint(5, 61)
		});
		graph.addSeries(series);

		GraphView graph2 = (GraphView) findViewById(R.id.graph2);
		LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(new DataPoint[] {
		          new DataPoint(01, 180),
		          new DataPoint(02, 51),
		          new DataPoint(03, 30),
		          new DataPoint(04, 200),
		          new DataPoint(05, 77),
		          new DataPoint(7, 56),
		          new DataPoint(8, 115),
		          new DataPoint(9, 250),
		          new DataPoint(10, 61),
		          new DataPoint(11, 180),
		          new DataPoint(12, 51),
		          new DataPoint(13, 30),
		          new DataPoint(14, 200),
		          new DataPoint(15, 77),
		          new DataPoint(17, 56),
		          new DataPoint(18, 115),
		          new DataPoint(19, 250),
		          new DataPoint(20, 61)
		});
		graph2.addSeries(series2);
		
		GraphView graph3 = (GraphView) findViewById(R.id.graph3);
		PointsGraphSeries<DataPoint> series3 = new PointsGraphSeries<DataPoint>(new DataPoint[] {
		          new DataPoint(01, 180),
		          new DataPoint(02, 51),
		          new DataPoint(03, 30),
		          new DataPoint(04, 200),
		          new DataPoint(05, 77),
		          new DataPoint(7, 56),
		          new DataPoint(8, 115),
		          new DataPoint(9, 250),
		          new DataPoint(10, 61)
		});
		graph3.addSeries(series3);

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