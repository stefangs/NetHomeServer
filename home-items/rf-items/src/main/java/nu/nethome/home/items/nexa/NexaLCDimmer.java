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
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@SuppressWarnings("UnusedDeclaration")
@Plugin
public class NexaLCDimmer extends NexaLCLamp implements HomeItem {

    private static final double NEXA_DIM_LEVEL_K = (14D / 100D);
    private static final double NEXA_DIM_LEVEL_M = 0.5;
    private boolean dimmedToPresetLevel = false;

    // Public attributes
    private int onDimLevel = 0;
    private int dimLevel1 = 25;
    private int dimLevel2 = 50;
    private int dimLevel3 = 75;
    private int dimLevel4 = 100;

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCDimmer\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"OnDimLevel\" Type=\"String\" Get=\"getOnDimLevel\" 	Set=\"setOnDimLevel\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Attribute Name=\"TransmissionRepeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "</HomeItem> ");

    @SuppressWarnings("UnusedDeclaration")
    public NexaLCDimmer() {
    }

    public String getModel() {
        return MODEL;
    }

    /**
     * Send the event to dim the device to the specified level. If the level is 0 the lamp
     * is turned off.
     *
     * @param dimLevel level in % of full power, 0 = off, 100 = full power
     */
    protected void sendDimCommand(int dimLevel) {
        if (dimLevel == 0) {
            off();
        } else {
            Event ev = server.createEvent("NexaL_Message", "");
            ev.setAttribute("Direction", "Out");
            ev.setAttribute("NexaL.Address", lampAddress);
            ev.setAttribute("NexaL.Button", lampButton);
            ev.setAttribute("NexaL.Command", 1);
            ev.setAttribute("NexaL.DimLevel", percentDimToNexaDim(dimLevel));
            if (repeats > 0) {
                ev.setAttribute("Repeat", repeats);
            }
            server.send(ev);
            isOn = true;
        }
    }

    private int percentDimToNexaDim(int dimLevel) {
        return (int) (dimLevel * NEXA_DIM_LEVEL_K + NEXA_DIM_LEVEL_M);
    }

    @Override
    public void on() {
        dimmedToPresetLevel = false;
        if (onDimLevel > 0) {
            sendDimCommand(onDimLevel);
        } else {
            super.on();
        }
    }

    @Override
    public void off() {
        dimmedToPresetLevel = false;
        super.off();
    }

    /**
     * Dim to the pre set dim level 1
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim1() {
        sendDimCommand(dimLevel1);
        dimmedToPresetLevel = true;
    }

    /**
     * Dim to the pre set dim level 2
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim2() {
        sendDimCommand(dimLevel2);
        dimmedToPresetLevel = true;
    }

    /**
     * Dim to the pre set dim level 3
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim3() {
        sendDimCommand(dimLevel3);
        dimmedToPresetLevel = true;
    }

    /**
     * Dim to the pre set dim level 4
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim4() {
        sendDimCommand(dimLevel4);
        dimmedToPresetLevel = true;
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDimLevel1() {
        return Integer.toString(dimLevel1);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel1 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDimLevel1(String mDimLevel1) {
        int newDimLevel = Integer.parseInt(mDimLevel1);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel1 = newDimLevel;
        }
    }


    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDimLevel2() {
        return Integer.toString(dimLevel2);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel2 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDimLevel2(String mDimLevel2) {
        int newDimLevel = Integer.parseInt(mDimLevel2);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel2 = newDimLevel;
        }
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDimLevel3() {
        return Integer.toString(dimLevel3);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel3 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDimLevel3(String mDimLevel3) {
        int newDimLevel = Integer.parseInt(mDimLevel3);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel3 = newDimLevel;
        }
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDimLevel4() {
        return Integer.toString(dimLevel4);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel4 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDimLevel4(String mDimLevel4) {
        int newDimLevel = Integer.parseInt(mDimLevel4);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel4 = newDimLevel;
        }
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getOnDimLevel() {
        if (onDimLevel == 0) {
            return "";
        }
        return Integer.toString(onDimLevel);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param level dimLevel level in % of full power, 0 = off, 100 = full power
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setOnDimLevel(String level) {
        if (level.length() == 0) {
            onDimLevel = 0;
        } else {
            int newDimLevel = Integer.parseInt(level);
            if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
                onDimLevel = newDimLevel;
                if (trackDimLevel()) {
                    sendDimCommand(onDimLevel);
                }
            }
        }
    }

    private boolean trackDimLevel() {
        return !dimmedToPresetLevel && isOn;
    }
}