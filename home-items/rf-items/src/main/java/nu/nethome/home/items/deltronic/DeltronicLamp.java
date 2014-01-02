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

package nu.nethome.home.items.deltronic;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a switch (typically connected to a lamp) which is controlled by
 * the Deltronic RF protocol. The DeltronicLamp requires a port which can send Deltronic
 * protocol messages as RF signals. This is typically done with the AudioProtocolTransmitter
 * <p/>
 * <br>
 * TODO: UnitTest
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value="Lamps", creationEvents = "Deltronic_Message")
public class DeltronicLamp extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"DeltronicLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"StringList\" Get=\"getTargetButton\" Set=\"setTargetButton\" >"
            + "     <item>A</item> <item>B</item> <item>C</item> <item>D</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(DeltronicLamp.class.getName());

    // Public attributes
    private boolean state = false;
    private int targetAddress = 0;
    private String targetButton = "A";

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Deltronic_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt("Deltronic.Address") == targetAddress) &&
                event.getAttributeInt("Deltronic.Button") == ((int) targetButton.charAt(0)) - ((int) 'A')) {
            // In that case, update our state accordingly
            state = (event.getAttributeInt("Deltronic.Command") == 1);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        targetAddress = event.getAttributeInt("Deltronic.Address");
        targetButton = "" + (char)(event.getAttributeInt("Deltronic.Button") + ((int) 'A'));
        return true;
    }

    /* (non-Javadoc)
          * @see ssg.home.HomeItem#getModel()
          */
    public String getModel() {
        return MODEL;
    }

    /**
     * @return Returns the m_State.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getState() {
        if (state) {
            return "On";
        }
        return "Off";
    }

    /**
     * @return Returns the m_Button.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getTargetButton() {
        return targetButton;
    }

    /**
     * @param button The button to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setTargetButton(String button) {
        String newButton = button.toUpperCase();
        this.targetButton = ((newButton.length() == 1) && (newButton.compareTo("A") >= 0) &&
                (newButton.compareTo("D") <= 0)) ? newButton : this.targetButton;
    }

    /**
     * @return Returns the houseCode.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
        return Integer.toString(targetAddress);
    }

    /**
     * @param address The address to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address) {
        int tempAddress = Integer.parseInt(address);
        this.targetAddress = ((tempAddress >= 0) && (tempAddress < 64)) ? tempAddress : this.targetAddress;
    }

    public void sendCommand(int command) {
        Event ev = server.createEvent("Deltronic_Message", "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute("Deltronic.Address", targetAddress);
        ev.setAttribute("Deltronic.Button", ((int) targetButton.charAt(0)) - ((int) 'A'));
        ev.setAttribute("Deltronic.Command", command);
        server.send(ev);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void on() {
        logger.fine("Switching on " + name);
        sendCommand(1);
        state = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void off() {
        logger.fine("Switching off " + name);
        sendCommand(0);
        state = false;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void toggle() {
        logger.fine("Toggling " + name);
        state = !state;
        sendCommand(state ? 1 : 0);
    }
}
