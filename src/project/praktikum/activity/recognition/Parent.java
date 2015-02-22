package project.praktikum.activity.recognition;

import java.util.ArrayList;

public class Parent {
	private String Running;
    private String Walking;
    private String Sleeping;
    private String Cycling;
    private String Driving;
    private String Date;
     
    private ArrayList<Child> children;
     
    public String getRunning() {
		return Running;
	}

	public void setRunning(String running) {
		Running = running;
	}

	public String getWalking() {
		return Walking;
	}

	public void setWalking(String walking) {
		Walking = walking;
	}

	public String getSleeping() {
		return Sleeping;
	}

	public void setSleeping(String sleeping) {
		Sleeping = sleeping;
	}

	public String getCycling() {
		return Cycling;
	}

	public void setCycling(String cycling) {
		Cycling = cycling;
	}

	public String getDriving() {
		return Driving;
	}

	public void setDriving(String driving) {
		Driving = driving;
	}

	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	// ArrayList to store child objects
    public ArrayList<Child> getChildren()
    {
        return children;
    }
     
    public void setChildren(ArrayList<Child> children)
    {
        this.children = children;
    }
}
