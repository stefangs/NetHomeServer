/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.timer;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * 
 * IntervalTimer
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Timers")
public class IntervalTimer extends HomeItemAdapter implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"IntervalTimer\" Category=\"Timers\" >"
			+ "  <Attribute Name=\"Time\" Type=\"String\" Get=\"getTime\" 	Set=\"setTime\" />"
			+ "  <Attribute Name=\"StartCommand\" Type=\"Command\" Get=\"getStartCommand\" 	Set=\"setStartCommand\" />"
			+ "  <Attribute Name=\"StopCommand\" Type=\"Command\" Get=\"getStopCommand\" 	Set=\"setStopCommand\" />"
			+ "  <Attribute Name=\"TickInterval\" Type=\"String\" Get=\"getTickInterval\" 	Set=\"setTickInterval\" />"
			+ "  <Attribute Name=\"TickCommand\" Type=\"Command\" Get=\"getTickCommand\" 	Set=\"setTickCommand\" />"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getActive\" Default=\"true\" />"
			+ "  <Action Name=\"StartTimer\"		Method=\"startTimer\" Default=\"true\" />"
			+ "  <Action Name=\"StopTimer\"		Method=\"stopTimer\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(IntervalTimer.class.getName());
	protected Timer timer = null;
	protected Timer tickTimer = null;
	protected CommandLineExecutor executor;


	// Public attributes
	protected float time = 10f;
	protected float tickInterval = 0.0f;
	protected boolean active = false;
	protected String startCommand = "";
	protected String stopCommand = "";
	protected String tickCommand = "";

	public IntervalTimer() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return MODEL;
	}

	public void activate(HomeService server) {
        super.activate(server);
		executor = new CommandLineExecutor(server, true);
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		stopTimer();
        super.stop();
	}

	public void timerExpired() {
	    // Stop all timers
		stopTimer();
		// Execute the stop command
	    executor.executeCommandLine(stopCommand);
	}

	public void tickTimerExpired() {
		// Execute the tick command
	    executor.executeCommandLine(tickCommand);
	}

	public void startTimer() {
		if (active) {
			return;
		}
		// Get current time
	    Calendar date = Calendar.getInstance();
	    date.add(Calendar.MILLISECOND, (int)(1000 * 60 * time));
	    timer = new Timer();
	    // Schedule the job at time minutes interval
	    timer.schedule(
	    		new TimerTask(){public void run() {timerExpired();}},
				date.getTime()
	    );
	    if (tickInterval != 0.0f) {
		    Calendar tickDate = Calendar.getInstance();
		    tickDate.add(Calendar.MILLISECOND, (int)(1000 * 60 * tickInterval));
		    tickTimer = new Timer();
		    // Schedule the job at time minutes interval
		    tickTimer.schedule(
		    		new TimerTask(){public void run() {tickTimerExpired();}},
		    		tickDate.getTime(),
					(int)(1000 * 60 * tickInterval)
		    );
	    }
	    // Perform the start command
	    executor.executeCommandLine(startCommand);
	    active = true;
	}

	public void stopTimer() {
		if (!active) {
			return;
		}
		active = false;
	    if (tickTimer != null) {
	    	tickTimer.cancel();
	    	tickTimer = null;
	    }
	    if (timer != null) {
	    	timer.cancel();
	    	timer = null;
	    }
	}

	/**
	 * @return Returns the time.
	 */
	public String getTime() {
		return Float.toString(time);
	}
	/**
	 * @param Time The time to set.
	 */
	public void setTime(String Time) {
		try {
			float temp = Float.parseFloat(Time); 
			time = temp > 0f ? temp : time;
		} catch (NumberFormatException e) {
			// DoDinada, bad input
		}
	}

	/**
	 * Returns if the timer is currently active (running) or not.
	 * @return Returns the active state, "Yes" or "No".
	 */
	public String getActive() {
		return active ? "Active" : "Inactive";
	}
	/**
	 * @return Returns the tickInterval.
	 */
	public String getTickInterval() {
		return Float.toString(tickInterval);
	}
	/**
	 * @param TickInterval The tickInterval to set.
	 */
	public void setTickInterval(String TickInterval) {
		try {
			float temp = Float.parseFloat(TickInterval); 
			tickInterval = temp >= 0f ? temp : tickInterval;
		} catch (NumberFormatException e) {
			// DoDinada, bad input
		}
	}	
	/**
	 * @return Returns the startCommand.
	 */
	public String getStartCommand() {
		return startCommand;
	}
	/**
	 * @param StartCommand The startCommand to set.
	 */
	public void setStartCommand(String StartCommand) {
		startCommand = StartCommand;
	}	
	/**
	 * @return Returns the stopCommand.
	 */
	public String getStopCommand() {
		return stopCommand;
	}
	/**
	 * @param StopCommand The stopCommand to set.
	 */
	public void setStopCommand(String StopCommand) {
		stopCommand = StopCommand;
	}	
	/**
	 * @return Returns the tickCommand.
	 */
	public String getTickCommand() {
		return tickCommand;
	}
	/**
	 * @param TickCommand The tickCommand to set.
	 */
	public void setTickCommand(String TickCommand) {
		tickCommand = TickCommand;
	}	
}
