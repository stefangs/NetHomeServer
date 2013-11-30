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

package nu.nethome.home.items.misc;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.ValueItem;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


/**
 * 
 * ClassName
 * 
 * @author Stefan
 */
public class ValueLogger extends TimerTask implements HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ValueLogger\" >"
			+ "  <Attribute Name=\"File Name\" Type=\"String\" Get=\"getFileName\" 	Set=\"setFileName\" />"
			+ "  <Attribute Name=\"ValueObjects\" Type=\"String\" Get=\"getValueObjects\" 	Set=\"setValueObjects\" />"
			+ "  <Attribute Name=\"Interval\" Type=\"String\" Get=\"getInterval\" 	Set=\"setInterval\" />"
			+ "  <Attribute Name=\"TimeFormat\" Type=\"String\" Get=\"getTimeFormat\" 	Set=\"setTimeFormat\" />"
			+ "  <Attribute Name=\"ValueSeparator\" Type=\"String\" Get=\"getValueSeparator\" 	Set=\"setValueSeparator\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(ValueLogger.class.getName());
	protected String m_Name = "NoNameYet";
	protected long m_ID = 0L;
	protected HomeService m_EventBroker;
	protected Timer m_Timer = new Timer();

	// Public attributes
	protected String m_FileName = "c:\\data\\logs\\Temperature.log";
	protected String m_ValueObjects = "Yard Thermometer";
	protected int m_Interval = 15;
	protected String m_TimeFormat = "yyyy.MM.dd HH:mm:ss;";
	protected String m_ValueSeparator = ";";

	public ValueLogger() {
	}
	
	public String getModel() {
		return m_Model;
	}

	public void setName(String name) {
		m_Name = name;
	}

	public String getName() {
		return m_Name;
	}

	public long getItemId() {
		return m_ID;
	}

	public void setItemId(long id) {
		m_ID = id;
	}

    public boolean receiveEvent(Event event) {
        return false;
    }

	
	public void activate(HomeService server) {
        m_EventBroker = server;
		// Get current time
	    Calendar date = Calendar.getInstance();
	    // Start at next even hour
	    date.set(Calendar.HOUR, date.get(Calendar.HOUR) + 1);
	    date.set(Calendar.MINUTE, 0);
	    date.set(Calendar.SECOND, 0);
	    date.set(Calendar.MILLISECOND, 0);
	    // Schedule the job at m_Interval minutes interval
	    m_Timer.schedule(
	    		this,
				date.getTime(),
				1000 * 60 * m_Interval
	    );
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		m_Timer.cancel();
	}

	public void run() {
		logger.fine("Value Log Timer Fired");
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(m_FileName, true));
	        // Format the current time.
	        SimpleDateFormat formatter
	            = new SimpleDateFormat (m_TimeFormat);
	        Date currentTime = new Date();
	        String dateString = formatter.format(currentTime);
	        String newLogLine = dateString;
		    LinkedList valueObjects = stringToList(m_ValueObjects);
		    ListIterator i = valueObjects.listIterator();
			while(i.hasNext()) {
				String valueObject = i.next().toString();
				ValueItem valueItem = findValueItem(valueObject);
				if (valueItem != null)
				{
					newLogLine += valueItem.getValue();
					if (i.hasNext()){
						newLogLine += m_ValueSeparator;
					}
				}
			}
	        out.write(newLogLine);
	        out.newLine();
	        out.close();
		}
		catch (IOException e) {
			logger.warning("Failed to open log file: " + m_FileName + " Error:" + e.toString());
		}
	}

	public LinkedList stringToList(String list) {
		LinkedList linkList = new LinkedList();
		int start = 0;
		int end = 0;
		
		while ((end = list.indexOf(",", end + 1)) != -1) {
			String temp = new String(list.substring(start, end));
			linkList.add(temp);
			start = end + 1;
		}
		String temp = new String(list.substring(start, list.length()));
		linkList.add(temp);
		return linkList;
	}
	
	// NYI - now only works for UPMThermometer, should work for all "value items"
	protected ValueItem findValueItem(String name){
		return (ValueItem)m_EventBroker.openInstance(name).getInternalRepresentation();
	}

	/**
	 * @return Returns the m_FileName.
	 */
	public String getFileName() {
		return m_FileName;
	}
	/**
	 * @param FileName The m_FileName to set.
	 */
	public void setFileName(String FileName) {
		m_FileName = FileName;
	}

	/**
	 * @return Returns the m_Interval.
	 */
	public String getInterval() {
		return Integer.toString(m_Interval);
	}
	/**
	 * @param Interval The m_Interval to set.
	 */
	public void setInterval(String Interval) {
		m_Interval = Integer.parseInt(Interval);
	}
	/**
	 * @return Returns the m_ValueObjects.
	 */
	public String getValueObjects() {
		return m_ValueObjects;
	}
	/**
	 * @param ValueObjects The m_ValueObjects to set.
	 */
	public void setValueObjects(String ValueObjects) {
		m_ValueObjects = ValueObjects;
	}	
	/**
	 * @return Returns the m_TimeFormat.
	 */
	public String getTimeFormat() {
		return m_TimeFormat;
	}
	/**
	 * @param TimeFormat The m_TimeFormat to set.
	 */
	public void setTimeFormat(String TimeFormat) {
		m_TimeFormat = TimeFormat;
	}	
	/**
	 * @return Returns the m_ValueSeparator.
	 */
	public String getValueSeparator() {
		return m_ValueSeparator;
	}
	/**
	 * @param ValueSeparator The m_ValueSeparator to set.
	 */
	public void setValueSeparator(String ValueSeparator) {
		m_ValueSeparator = ValueSeparator;
	}	
}

