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

package nu.nethome.home.items.fs20;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * FHZ1000PcPort communicates with the FS20-radio protocol to control FS20Devices which can
 * turn on and off lamps and other maines powered devices. <br>
 * 
 * @author Stefan
 */
public class FHZ1000PcPort extends HomeItemAdapter implements HomeItem, FS20EventListener {

	public static final String EVENT_TYPE_FS20_COMMAND = "FS20Command";
	public static final String EVENT_TYPE_FS20_EVENT = "FS20Event";
	public static final String EVENT_HOUSECODE_ATTRIBUTE = "HouseCode";
	public static final String EVENT_DEVICECODE_ATTRIBUTE = "DeviceCode";

	public final static byte COMMAND_OFF = FHZ1000PC.COMMAND_OFF;
	public final static byte COMMAND_DIM1 = FHZ1000PC.COMMAND_DIM1;
	public final static byte COMMAND_ON = FHZ1000PC.COMMAND_ON;
	public final static byte COMMAND_DIM_LOOP = FHZ1000PC.COMMAND_DIM_LOOP;
	public final static byte COMMAND_DIM_DOWN = FHZ1000PC.COMMAND_DIM_DOWN;
	public final static byte COMMAND_DIM_UP = FHZ1000PC.COMMAND_DIM_UP;
	public final static byte COMMAND_TOGGLE = FHZ1000PC.COMMAND_TOGGLE;
	public final static byte COMMAND_TIMER_PROG = FHZ1000PC.COMMAND_TIMER_PROG;
	public final static byte COMMAND_DELIVERY_STATE = FHZ1000PC.COMMAND_DELIVERY_STATE;
	
	private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"FHZ1000PcPort\"  Category=\"Hardware\" >"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
			+ "  <Attribute Name=\"PortName\" Type=\"String\" Get=\"getPortName\" Set=\"setPortName\" />"
			+ "  <Attribute Name=\"ReceivedMessages\" Type=\"String\" Get=\"getReceivedMessages\" />"
			+ "  <Attribute Name=\"SentMessages\" Type=\"String\" Get=\"getSentMessages\" />"
			+ "  <Action Name=\"reconnect\" Method=\"reconnect\" Default=\"true\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(FHZ1000PcPort.class.getName());

	protected FHZ1000PC m_Transmitter;
	protected boolean m_IsRunning = false;
	protected int m_ReceivedMessages = 0;
	protected int m_SentMessages = 0;
	protected String m_PortName = "/dev/ttyUSB1";

	public FHZ1000PcPort() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
	 */
	public boolean receiveEvent(Event event) {
		String eventType = event.getAttribute("Type");
		if (eventType.equals(EVENT_TYPE_FS20_COMMAND)) {
			int houseCode = FHZ1000PC.StringFS20ToInt(event.getAttribute(EVENT_HOUSECODE_ATTRIBUTE));
			byte deviceCode = (byte) FHZ1000PC.StringFS20ToInt(event.getAttribute(EVENT_DEVICECODE_ATTRIBUTE));
			byte command = (byte)event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE);
			sendFs20Command(houseCode, deviceCode, command);
            return true;
		}
		return false;
	}

	public String getModel() {
		return m_Model;
	}

	public void activate() {
		try {
			m_Transmitter = new FHZ1000PC(m_PortName);
			m_Transmitter.fs20Init();
			m_Transmitter.registerEventListener(this);
		} catch (IOException e) {
			logger.warning("Failed to open FHZ1000Device:" + e.toString());
			m_Transmitter = null;
			return;
		} catch (UnsatisfiedLinkError e) {
			logger.warning("Failed to open FHZ1000Device:" + e.toString());
			m_Transmitter = null;
			return;
		}
		m_IsRunning = true;
	}
	
	public void fs20Event(FS20Event ev){
		// Send as a House-event
        Event event = server.createEvent(EVENT_TYPE_FS20_EVENT, "");
    	event.setAttribute(EVENT_HOUSECODE_ATTRIBUTE, FHZ1000PC.binFS20ByteToString(ev.getHouseCode(), 16));
    	event.setAttribute(EVENT_DEVICECODE_ATTRIBUTE, FHZ1000PC.binFS20ByteToString(ev.getButton(), 8));
    	event.setAttribute(Event.EVENT_VALUE_ATTRIBUTE, Integer.toString(ev.getFunction()));
    	server.send(event);
    	logger.info(event.toString());
		m_ReceivedMessages++;
	}

	public void stop() {
		if (m_IsRunning) {
			try {
				m_Transmitter.unregisterEventListener();
			} catch (IOException e) {
				logger.warning("Failed to close FHZ1000PC Device: " + e.toString());
			}
		}
		m_IsRunning = false;
		m_Transmitter = null;
	}

	public void sendFs20Command(int houseCode, byte deviceCode, byte command) {
		if (!m_IsRunning) {
			logger.warning("FHZ1000PcPort not running - not sending FS20 command");
			return;
		}
		try {
			m_Transmitter.sendFS20Command(houseCode, deviceCode, command);
			m_SentMessages++;
		}
		catch (IOException e){
			logger.warning("Failed to send FS20 command: " + e.toString());
		}
	}
	
	public String getState() {
		if (m_IsRunning) {
			return "Connected";
		}
		return "Not Connected";
	}

	public void reconnect() {
		if (!m_IsRunning) {
			activate();
		}
	}

	/**
	 * @return the m_ReceivedMessages
	 */
	public String getReceivedMessages() {
		return String.valueOf(m_ReceivedMessages);
	}

	/**
	 * @return the m_SentMessages
	 */
	public String getSentMessages() {
		return String.valueOf(m_SentMessages);
	}

	/**
	 * @return the m_PortName
	 */
	public String getPortName() {
		return m_PortName;
	}

	/**
	 * @param portName the m_PortName to set
	 */
	public void setPortName(String portName) {
		m_PortName = portName;
	}

}

