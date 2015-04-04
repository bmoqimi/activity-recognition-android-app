package project.praktikum.database;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class Activity {

	Date Start;
	Date Finish;
	String act;
	
	public Activity(String act , String start , String finish) {
		// TODO Auto-generated constructor stub
		this.act = act;
		try {
			this.Start = convertStringToDate(start);
			this.Finish = convertStringToDate(finish);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Activity(String act , Date start , Date finish) {
		// TODO Auto-generated constructor stub
		this.act = act;
		this.Start = start;
		this.Finish = finish;
	}
	
	private Date convertStringToDate(String input) throws ParseException
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		d = df.parse(input);
		return d;
	}
	
	public Date getStart() {
		return Start;
	}
	public void setStart(Date start) {
		Start = start;
	}
	public Date getFinish() {
		return Finish;
	}
	public void setFinish(Date finish) {
		Finish = finish;
	}
	public String getAct() {
		return act;
	}
	public void setAct(String act) {
		this.act = act;
	}
}
