/*
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

package nu.nethome.home.items.hue;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Hardware")
public class HueBridge extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HueBridge\"  Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"IpAddress\" Type=\"String\" Get=\"getIp\" Set=\"setIp\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getBridgeIdentity\" Set=\"setBridgeIdentity\" />"
            + "  <Attribute Name=\"UserName\" Type=\"String\" Get=\"getUserName\" Set=\"setUserName\" />"
            + "  <Action Name=\"registerUser\" Method=\"registerUser\" />"
            + "</HomeItem> ");

    String userName = "stefanstromberg";
    String ip = "192.168.1.174";
    String bridgeIdentity = "";
    PhilipsHueBridge hueBridge;


    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        hueBridge = new PhilipsHueBridge(ip);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (!isActivated()) {
            return false;
        }
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Hue_Message") &&
                event.getAttribute("Direction").equals("Out")) {
            String lampId = event.getAttribute("Hue.Lamp");
            String command = event.getAttribute("Hue.Command");
            if (command.equals("On")) {
                turnLampOn(lampId, event);
            } else if (command.equals("Off")) {
                turnLampOff(lampId);
            }
            fetchLampState(lampId);
        }
        return true;
    }

    private void fetchLampState(String lampId) {
        Light light = hueBridge.getLight(userName, lampId);
        Event event = server.createEvent("Hue_Message", "");
        event.setAttribute("Direction", "In");
        event.setAttribute("Hue.Lamp", lampId);
        event.setAttribute("Hue.Command", light.getState().isOn() ? "On" : "Off");
        event.setAttribute("Hue.Name", light.getName());
        event.setAttribute("Hue.Model", light.getModelid());
        event.setAttribute("Hue.Type", light.getType());
        event.setAttribute("Hue.Version", light.getSwversion());
        server.send(event);
    }

    private void turnLampOff(String lampId) {
        hueBridge.setLightState(userName, lampId, new LightState());
    }

    private void turnLampOn(String lampId, Event event) {
        int brightness = event.getAttributeInt("Hue.Brightness");
        int hue = event.getAttributeInt("Hue.Hue");
        int saturation = event.getAttributeInt("Hue.Saturation");
        hueBridge.setLightState(userName, lampId, new LightState(brightness, hue, saturation));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        hueBridge = new PhilipsHueBridge(ip);
    }

    public String getBridgeIdentity() {
        return bridgeIdentity;
    }

    public void setBridgeIdentity(String bridgeIdentity) {
        this.bridgeIdentity = bridgeIdentity;
    }
}
