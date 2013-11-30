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
import nu.nethome.home.items.Lamp;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

/**
 * @author Stefan
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
@Plugin
public class FS20Lamp extends HomeItemAdapter implements HomeItem, Lamp {

    private final String m_Model = ("<?xml version = \"1.0\"?> \n"

            + "<HomeItem Class=\"FS20Lamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" 		Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"DeviceCode\" Type=\"String\" Get=\"getDeviceCode\" 	Set=\"setDeviceCode\" />"
            + "  <Attribute Name=\"FHZ1000PcPort\" 	Type=\"Item\" Get=\"getFHZ1000PcPort\" 		Set=\"setFHZ1000PcPort\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"dim25\" 	Method=\"dim25\" />"
            + "  <Action Name=\"dim50\" 	Method=\"dim50\" />"
            + "  <Action Name=\"dim75\" 	Method=\"dim75\" />"
            + "  <Action Name=\"dim100\" 	Method=\"dim100\" />"
            + "  <Action Name=\"dimLoop\" 	Method=\"dimLoop\" />"
            + "</HomeItem> ");

    protected boolean m_IsAddressed = false;

    // Public attributes
    protected boolean m_IsOn = false;
    protected String m_HouseCode = "11111124";
    protected String m_DeviceCode = "1111";
    protected String m_FHZ1000PcPort = "FHZ1000PcPort";
    protected int m_LocationX = 10;
    protected int m_LocationY = 10;

    public FS20Lamp() {
    }

    public boolean receiveEvent(Event event) {
        // Check the events and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(FHZ1000PcPort.EVENT_TYPE_FS20_EVENT)) {
            if (event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE).equals(m_HouseCode) &&
                    event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE).equals(m_DeviceCode)) {
                receiveCommand((byte) event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE));
                return true;
            }
        }
        return false;
    }

    public void receiveCommand(byte command) {
        switch (command) {
            case FHZ1000PcPort.COMMAND_ON:
            case FHZ1000PcPort.COMMAND_DIM_UP:
            case FHZ1000PcPort.COMMAND_DIM_DOWN:
                m_IsOn = true;
                break;
            case FHZ1000PcPort.COMMAND_OFF:
                m_IsOn = false;
                break;
            case FHZ1000PcPort.COMMAND_TOGGLE:
            case FHZ1000PcPort.COMMAND_DIM_LOOP:
                m_IsOn = !m_IsOn;
        }
        if ((command >= FHZ1000PcPort.COMMAND_DIM1) && (command < FHZ1000PcPort.COMMAND_ON)) {
            m_IsOn = true;
        }
    }

    public String getModel() {
        return m_Model;
    }

    public String getState() {
        if (m_IsOn) {
            return "On";
        }
        return "Off";
    }

    /**
     * @return Returns the m_LocationX.
     */
    public String getLocationX() {
        return String.valueOf(m_LocationX);
    }

    /**
     * @param locationX The m_LocationX to set.
     */
    public void setLocationX(String locationX) {
        m_LocationX = Integer.parseInt(locationX);
    }

    /**
     * @return Returns the m_LocationY.
     */
    public String getLocationY() {
        return String.valueOf(m_LocationY);
    }

    /**
     * @param locationY The m_LocationY to set.
     */
    public void setLocationY(String locationY) {
        m_LocationY = Integer.parseInt(locationY);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    /**
     * @return Returns the m_DeviceCode.
     */
    public String getDeviceCode() {
        return m_DeviceCode;
    }

    /**
     * @param deviceCode The m_DeviceCode to set.
     */
    public void setDeviceCode(String deviceCode) {
        m_DeviceCode = deviceCode;
    }

    /**
     * @return Returns the m_HouseCode.
     */
    public String getHouseCode() {
        return m_HouseCode;
    }

    /**
     * @param houseCode The m_HouseCode to set.
     */
    public void setHouseCode(String houseCode) {
        m_HouseCode = houseCode;
    }

    /**
     * @return Returns the m_FHZ1000PcPort.
     */
    public String getFHZ1000PcPort() {
        return m_FHZ1000PcPort;
    }

    /**
     * @param port The m_FHZ1000PcPort to set.
     */
    public void setFHZ1000PcPort(String port) {
        m_FHZ1000PcPort = port;
    }

    public void sendCommand(byte command) {
        Event ev = server.createEvent(FHZ1000PcPort.EVENT_TYPE_FS20_COMMAND, "");
        ev.setAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE, m_HouseCode);
        ev.setAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE, m_DeviceCode);
        ev.setAttribute(Event.EVENT_VALUE_ATTRIBUTE, command);
        server.send(ev);
    }

    public void on() {
        sendCommand(FHZ1000PcPort.COMMAND_ON);
        m_IsOn = true;
    }

    public void off() {
        sendCommand(FHZ1000PcPort.COMMAND_OFF);
        m_IsOn = false;
    }

    public void bright() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_UP);
        m_IsOn = true;
    }

    public void dim() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_DOWN);
        //m_IsOn = false;
    }

    public void toggle() {
        sendCommand(FHZ1000PcPort.COMMAND_TOGGLE);
        m_IsOn = !m_IsOn;
    }

    public void dimLoop() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_LOOP);
        //m_IsOn = !m_IsOn;
    }

    public void dim25() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 4));
        //m_IsOn = false;
    }

    public void dim50() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 8));
        //m_IsOn = false;
    }

    public void dim75() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 12));
        //m_IsOn = false;
    }

    public void dim100() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_ON - 1));
        //m_IsOn = false;
    }

    public void dimTo(int percent) {
        if ((percent < 0) || (percent > 100)) return;
        byte commandValue = (byte) (percent * 16 / 100);
        sendCommand(commandValue);
        m_IsOn = commandValue > 0 ? true : false;
    }
}
