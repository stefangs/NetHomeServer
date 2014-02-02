/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.hue;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "Hue_Message")
public class HueLamp extends HomeItemAdapter implements HomeItem {

    public static final int DIM_STEP = 20;
    private String lampId = "";
    private int onBrightness = 100;
    private int currentBrightness = 100;
    private int hue = 0;
    private int saturation = 0;
    private int colorTemperature = 0;
    private String color;
    private boolean isOn;
    private int dimLevel1 = 0;
    private int dimLevel2 = 33;
    private int dimLevel3 = 66;
    private int dimLevel4 = 100;


    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HueLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getLampId\" 	Set=\"setLampId\" />"
            + "  <Attribute Name=\"LampModel\" Type=\"String\" Get=\"getLampModel\" 	Init=\"setLampModel\" />"
            + "  <Attribute Name=\"Type\" Type=\"String\" Get=\"getLampType\" 	Init=\"setLampType\" />"
            + "  <Attribute Name=\"Version\" Type=\"String\" Get=\"getLampVersion\" 	Init=\"setLampVersion\" />"
            + "  <Attribute Name=\"Brightness\" Type=\"String\" Get=\"getCurrentBrightness\"  />"
            + "  <Attribute Name=\"OnBrightness\" Type=\"String\" Get=\"getBrightness\" 	Set=\"setBrightness\" />"
            + "  <Attribute Name=\"Color\" Type=\"String\" Get=\"getColor\" 	Set=\"setColor\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "</HomeItem> ");

    private String lampModel = "";
    private String lampType = "";
    private String lampVersion = "";

    public HueLamp() {
    }

    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Hue_Message") &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute("Hue.Lamp").equals(lampId)) {
            String command = event.getAttribute("Hue.Command");
            if (command.equals("On")) {
                isOn = true;
            } else if (command.equals("Off")) {
                isOn = false;
            }
            updateAttributes(event);
            return true;
        }
        return handleInit(event);
    }

    private void updateAttributes(Event event) {
        lampModel = event.getAttribute("Hue.Model");
        lampType = event.getAttribute("Hue.Type");
        lampVersion = event.getAttribute("Hue.Version");
    }

    @Override
    protected boolean initAttributes(Event event) {
        lampId = event.getAttribute("Hue.Lamp");
        updateAttributes(event);
        return true;
    }

    protected void sendOnCommand(int brightness, int hue, int saturation) {
        Event ev = createEvent();
        currentBrightness = brightness;
        ev.setAttribute("Hue.Command", "On");
        ev.setAttribute("Hue.Brightness", percentToHue(brightness));
        if (colorTemperature != 0) {
            ev.setAttribute("Hue.Temperature", colorTemperature);
        } else {
            ev.setAttribute("Hue.Saturation", saturation);
            ev.setAttribute("Hue.Hue", hue);
        }
        server.send(ev);
        isOn = true;
    }

    private int percentToHue(int brightness) {
        return (brightness * 254) / 100;
    }

    protected void sendOffCommand() {
        Event ev = createEvent();
        ev.setAttribute("Hue.Command", "Off");
        server.send(ev);
        isOn = false;
    }

    private Event createEvent() {
        Event ev = server.createEvent("Hue_Message", "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute("Hue.Lamp", lampId);
        return ev;
    }

    public void on() {
        sendOnCommand(onBrightness, hue, saturation);
        isOn = true;
    }

    public void off() {
        sendOffCommand();
        isOn = false;
    }

    public void toggle() {
        if (isOn) {
            off();
        } else {
            on();
        }
    }

    public void setBrightness(String level) {
        if (level.length() == 0) {
            onBrightness = 100;
        } else {
            int newDimLevel = Integer.parseInt(level);
            if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
                onBrightness = newDimLevel;
                if (isOn) {
                    on();
                }
            }
        }
    }

    public String getBrightness() {
        return Integer.toString(onBrightness);
    }

    public String getLampId() {
        return lampId;
    }

    public void setLampId(String lampId) {
        this.lampId = lampId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        String[] colourParts = color.split(",");
        if (colourParts.length == 2) {
            hue = Integer.parseInt(colourParts[0]);
            saturation = Integer.parseInt(colourParts[1]);
            colorTemperature = 0;
        } else {
            colorTemperature = Integer.parseInt(color);
        }
        this.color = color;
    }

    public String getState() {
        return isOn ? "On" : "Off";
    }

    public boolean isOn() {
        return isOn;
    }

    public String getLampModel() {
        return lampModel;
    }

    public void setLampModel(String lampModel) {
        this.lampModel = lampModel;
    }

    public String getLampType() {
        return lampType;
    }

    public void setLampType(String lampType) {
        this.lampType = lampType;
    }

    public String getLampVersion() {
        return lampVersion;
    }

    public void setLampVersion(String lampVersion) {
        this.lampVersion = lampVersion;
    }

    public void dim1() {
        sendOnCommand(dimLevel1, hue, saturation);
    }

    public void dim2() {
        sendOnCommand(dimLevel2, hue, saturation);
    }

    public void dim3() {
        sendOnCommand(dimLevel3, hue, saturation);
    }

    public void dim4() {
        sendOnCommand(dimLevel4, hue, saturation);
    }

    public String getDimLevel1() {
        return Integer.toString(dimLevel1);
    }

    public void setDimLevel1(String mDimLevel1) {
        int newDimLevel = Integer.parseInt(mDimLevel1);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel1 = newDimLevel;
        }
    }

    public String getDimLevel2() {
        return Integer.toString(dimLevel2);
    }

    public void setDimLevel2(String mDimLevel2) {
        int newDimLevel = Integer.parseInt(mDimLevel2);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel2 = newDimLevel;
        }
    }

    public String getDimLevel3() {
        return Integer.toString(dimLevel3);
    }

    public void setDimLevel3(String mDimLevel3) {
        int newDimLevel = Integer.parseInt(mDimLevel3);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel3 = newDimLevel;
        }
    }

    public String getDimLevel4() {
        return Integer.toString(dimLevel4);
    }

    public void setDimLevel4(String mDimLevel4) {
        int newDimLevel = Integer.parseInt(mDimLevel4);
        if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
            dimLevel4 = newDimLevel;
        }
    }

    public void bright() {
        currentBrightness += DIM_STEP;
        if (currentBrightness > 100) {
            currentBrightness = 100;
        }
        sendOnCommand(currentBrightness, hue, saturation);
    }

    public void dim() {
        currentBrightness -= DIM_STEP;
        if (currentBrightness < 0) {
            currentBrightness = 0;
        }
        sendOnCommand(currentBrightness, hue, saturation);
    }

    public String getCurrentBrightness() {
        return Integer.toString(currentBrightness);
    }
}