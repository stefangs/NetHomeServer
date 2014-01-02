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
import nu.nethome.home.item.HomeItemType;
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
@HomeItemType(value="Lamps", creationEvents = FHZ1000PcPort.EVENT_TYPE_FS20_EVENT)
public class FS20Lamp extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"

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
    protected boolean isOn = false;
    protected String houseCode = "11111124";
    protected String deviceCode = "1111";
    protected String fhz1000PcPort = "FHZ1000PcPort";

    public FS20Lamp() {
    }

    public boolean receiveEvent(Event event) {
        // Check the events and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(FHZ1000PcPort.EVENT_TYPE_FS20_EVENT) &&
                event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE).equals(houseCode) &&
                event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE).equals(deviceCode)) {
                receiveCommand((byte) event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE));
                return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        houseCode = event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE);
        deviceCode = event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE);
        return true;
    }

    public void receiveCommand(byte command) {
        switch (command) {
            case FHZ1000PcPort.COMMAND_ON:
            case FHZ1000PcPort.COMMAND_DIM_UP:
            case FHZ1000PcPort.COMMAND_DIM_DOWN:
                isOn = true;
                break;
            case FHZ1000PcPort.COMMAND_OFF:
                isOn = false;
                break;
            case FHZ1000PcPort.COMMAND_TOGGLE:
            case FHZ1000PcPort.COMMAND_DIM_LOOP:
                isOn = !isOn;
        }
        if ((command >= FHZ1000PcPort.COMMAND_DIM1) && (command < FHZ1000PcPort.COMMAND_ON)) {
            isOn = true;
        }
    }

    public String getModel() {
        return MODEL;
    }

    public String getState() {
        if (isOn) {
            return "On";
        }
        return "Off";
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    /**
     * @return Returns the deviceCode.
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * @param deviceCode The deviceCode to set.
     */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    /**
     * @return Returns the houseCode.
     */
    public String getHouseCode() {
        return houseCode;
    }

    /**
     * @param houseCode The houseCode to set.
     */
    public void setHouseCode(String houseCode) {
        this.houseCode = houseCode;
    }

    /**
     * @return Returns the fhz1000PcPort.
     */
    public String getFHZ1000PcPort() {
        return fhz1000PcPort;
    }

    /**
     * @param port The fhz1000PcPort to set.
     */
    public void setFHZ1000PcPort(String port) {
        fhz1000PcPort = port;
    }

    public void sendCommand(byte command) {
        Event ev = server.createEvent(FHZ1000PcPort.EVENT_TYPE_FS20_COMMAND, "");
        ev.setAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE, houseCode);
        ev.setAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE, deviceCode);
        ev.setAttribute(Event.EVENT_VALUE_ATTRIBUTE, command);
        server.send(ev);
    }

    public void on() {
        sendCommand(FHZ1000PcPort.COMMAND_ON);
        isOn = true;
    }

    public void off() {
        sendCommand(FHZ1000PcPort.COMMAND_OFF);
        isOn = false;
    }

    public void bright() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_UP);
        isOn = true;
    }

    public void dim() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_DOWN);
        //isOn = false;
    }

    public void toggle() {
        sendCommand(FHZ1000PcPort.COMMAND_TOGGLE);
        isOn = !isOn;
    }

    public void dimLoop() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_LOOP);
        //isOn = !isOn;
    }

    public void dim25() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 4));
        //isOn = false;
    }

    public void dim50() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 8));
        //isOn = false;
    }

    public void dim75() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_DIM1 + 12));
        //isOn = false;
    }

    public void dim100() {
        sendCommand((byte) (FHZ1000PcPort.COMMAND_ON - 1));
        //isOn = false;
    }

    public void dimTo(int percent) {
        if ((percent < 0) || (percent > 100)) return;
        byte commandValue = (byte) (percent * 16 / 100);
        sendCommand(commandValue);
        isOn = commandValue > 0 ? true : false;
    }
}
