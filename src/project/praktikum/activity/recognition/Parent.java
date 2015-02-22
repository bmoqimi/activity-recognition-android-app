package project.praktikum.activity.recognition;

import java.util.ArrayList;
import java.util.Date;

public class Parent {
	private long Running = 0;
    private long Walking = 0;
    private long Sleeping = 0;
    private long Cycling = 0;
    private long Driving = 0;
    private long Still = 0;
	private Date Date;
     
	public long getStill() {
		return Still;
	}

	public void setStill(long still) {
		Still = still;
	}
	
    private ArrayList<Child> children;
     
    public long getRunning() {
		return Running;
	}

	public void setRunning(long running) {
		Running = running;
	}

	public long getWalking() {
		return Walking;
	}

	public void setWalking(long walking) {
		Walking = walking;
	}

	public long getSleeping() {
		return Sleeping;
	}

	public void setSleeping(long sleeping) {
		Sleeping = sleeping;
	}

	public long getCycling() {
		return Cycling;
	}

	public void setCycling(long cycling) {
		Cycling = cycling;
	}

	public long getDriving() {
		return Driving;
	}

	public void setDriving(long driving) {
		Driving = driving;
	}

	public Date getDate() {
		return Date;
	}

	public void setDate(Date date) {
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
