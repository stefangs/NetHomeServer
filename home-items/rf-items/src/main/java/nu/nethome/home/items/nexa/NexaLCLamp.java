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

package nu.nethome.home.items.nexa;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 *
 * Represents a switch (typically connected to a lamp) which is controlled by
 * the NEXA Learning Code RF protocol. The NexaLCLamp requires a port which can
 * send NEXA protocol messages as RF signals. This is typically done with the
 * {@see nu.nethome.home.items.audio.AudioProtocolTransmitter}.
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "NexaL_Message")
public class NexaLCLamp extends HomeItemAdapter implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"NexaLCLamp\" Category=\"Lamps\" >"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
			+ "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
			+ "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
			+ "  <Attribute Name=\"TransmissionRepeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
			+ "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
			+ "  <Action Name=\"on\" 	Method=\"on\" />"
			+ "  <Action Name=\"off\" 	Method=\"off\" />"
			+ "</HomeItem> ");

	protected static Logger logger = Logger.getLogger(NexaLCLamp.class.getName());

	// Public attributes
	protected boolean isOn = false;
	protected int lampAddress = 0;
	protected int lampButton = 1;
    protected int repeats = 0;

	public NexaLCLamp() {
	}

	public boolean receiveEvent(Event event) {
		// Check if this is an inward event directed to this instance
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("NexaL_Message") &&
				event.getAttribute("Direction").equals("In") &&
				(event.getAttributeInt("NexaL.Address") == lampAddress) &&
                (event.getAttributeInt("NexaL.Button") == lampButton)) {
			// In that case, update our state accordingly
			isOn = (event.getAttributeInt("NexaL.Command") == 1);
            return true;
		} else {
		    return handleInit(event);
        }
	}

    @Override
    protected boolean handleInit(Event event) {
        lampAddress = event.getAttributeInt("NexaL.Address");
        lampButton = event.getAttributeInt("NexaL.Button");
        return true;
    }

    public String getModel() {
		return MODEL;
	}

	/**
	 * @return Returns the m_State.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getState() {
		if (isOn) {
			return "On";
		}
		return "Off";
	}

	/**
	 * @return Returns the m_HouseCode.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
		return Integer.toString(lampAddress);
	}

	/**
	 * @param houseCode The m_HouseCode to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setAddress(String houseCode) {
		int newAddress = Integer.parseInt(houseCode);
		if ((newAddress >= 0) && (newAddress < 1<<26)) {
			lampAddress = newAddress;
		}
	}

	public void sendCommand(int command) {
		Event ev = server.createEvent("NexaL_Message", "");
		ev.setAttribute("Direction", "Out");
		ev.setAttribute("NexaL.Address", lampAddress);
		ev.setAttribute("NexaL.Button", lampButton);
		ev.setAttribute("NexaL.Command", command);
        if (repeats > 0) {
            ev.setAttribute("Repeat", repeats);
        }
        server.send(ev);
	}

	@SuppressWarnings("UnusedDeclaration")
    public void on() {
		logger.fine("Switching on " + name);
		sendCommand(1);
		isOn = true;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void off() {
		logger.fine("Switching off " + name);
		sendCommand(0);
		isOn = false;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void toggle() {
		logger.fine("Toggling " + name);
        if (isOn) {
            off();
        } else {
            on();
        }
	}

	@SuppressWarnings("UnusedDeclaration")
    public String getButton() {
		return Integer.toString(lampButton);
	}

	@SuppressWarnings("UnusedDeclaration")
    public void setButton(String sbutton) {
		int button = Integer.parseInt(sbutton);
		if ((button > 0) && (button < 33)) {
			lampButton = button;
		}
	}

    @SuppressWarnings("UnusedDeclaration")
    public String getRepeats() {
        if (repeats == 0) {
            return "";
        }
        return Integer.toString(repeats);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRepeats(String repeats) {
        if (repeats.length() == 0) {
            this.repeats = 0;
        } else {
            int newRepeats = Integer.parseInt(repeats);
            if ((newRepeats >= 0) && (newRepeats <= 50)) {
                this.repeats = newRepeats;
            }
        }
    }

}
