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
@HomeItemType(value = "Lamps")
public class HueLamp extends HomeItemAdapter implements HomeItem {

    private String lampId = "";
    private int brightness = 0;
    private String colour = "";
    private boolean isOn;

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HueLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getLampId\" 	Set=\"setLampId\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"Brightness\" Type=\"String\" Get=\"getBrightness\" 	Set=\"setBrightness\" />"
            + "  <Attribute Name=\"Colour\" Type=\"String\" Get=\"getColour\" 	Set=\"setColour\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "</HomeItem> ");

    public HueLamp() {
    }

    public String getModel() {
        return MODEL;
    }

    protected void sendOnCommand(int brightness, int hue, int saturation) {
        Event ev = createEvent();
        ev.setAttribute("Hue.Command", "On");
        ev.setAttribute("Hue.Brightness", percentToHue(brightness));
        ev.setAttribute("Hue.Saturation", saturation);
        ev.setAttribute("Hue.Hue", hue);
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
        isOn = true;
    }

    private Event createEvent() {
        Event ev = server.createEvent("Hue_Message", "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute("Hue.Lamp", lampId);
        return ev;
    }

    public void on() {
        int hue = 0;
        int saturation = 0;
        String[] colourParts = colour.split(",");
        if (colourParts.length == 2) {
            hue = Integer.parseInt(colourParts[0]);
            saturation = Integer.parseInt(colourParts[1]);
        }
        sendOnCommand(brightness, hue, saturation);
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
            brightness = 100;
        } else {
            int newDimLevel = Integer.parseInt(level);
            if ((newDimLevel >= 0) && (newDimLevel <= 100)) {
                brightness = newDimLevel;
                if (isOn) {
                    on();
                }
            }
        }
    }

    public String getBrightness() {
        return Integer.toString(brightness);
    }

    public String getLampId() {
        return lampId;
    }

    public void setLampId(String lampId) {
        this.lampId = lampId;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}