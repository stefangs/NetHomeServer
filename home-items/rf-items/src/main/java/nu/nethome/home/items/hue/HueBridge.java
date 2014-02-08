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

import java.util.List;

/**
 * Represents a Philips Hue bridge and handles communications with it
 * TODO: Registration of user
 * TODO: Refresh of Hue Lamps
 * TODO: Error handling/exceptions
 * TODO: Connection state
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Hardware")
public class HueBridge extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HueBridge\"  Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getUrl\" Set=\"setUrl\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getBridgeIdentity\" Init=\"setBridgeIdentity\" />"
            + "  <Attribute Name=\"UserName\" Type=\"String\" Get=\"getUserName\" Set=\"setUserName\" />"
            + "  <Action Name=\"findBridge\" Method=\"reconnect\" />"
            + "  <Action Name=\"registerUser\" Method=\"registerUser\" />"
            + "</HomeItem> ");

    String userName = "";
    String url = "http://192.168.1.174";
    String bridgeIdentity = "";
    PhilipsHueBridge hueBridge;


    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        hueBridge = new PhilipsHueBridge(url, bridgeIdentity);
    }

    public void reconnect() {
        List<PhilipsHueBridge> bridges = PhilipsHueBridge.listLocalPhilipsHueBridges ();
        if (bridges.size() > 0) {
            this.url = bridges.get(0).getUrl();
            this.bridgeIdentity = bridges.get(0).getId();
            this.hueBridge = bridges.get(0);
        }
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
            if (command.equals("On") && lampId.length() > 0) {
                turnLampOn(lampId, event);
                reportLampState(lampId);
            } else if (command.equals("Off") && lampId.length() > 0) {
                turnLampOff(lampId);
                reportLampState(lampId);
            }
            return true;
        } else if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("ReportItems")) {
            List<LightId> ids = hueBridge.listLights(userName);
            for (LightId id : ids) {
                reportLampState(id.getLampId());
            }
            return true;
        }
        return false;
    }

    private void reportLampState(String lampId) {
        Light light = hueBridge.getLight(userName, lampId);
        Event event = server.createEvent("Hue_Message", "");
        event.setAttribute("Direction", "In");
        event.setAttribute("Hue.Lamp", lampId);
        event.setAttribute("Hue.Command", light.getState().isOn() ? "On" : "Off");
        event.setAttribute("Hue.Brightness", light.getState().getBrightness());
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
        int temperature = event.getAttributeInt("Hue.Temperature");
        int hue = event.getAttributeInt("Hue.Hue");
        int saturation = event.getAttributeInt("Hue.Saturation");
        LightState state;
        if (temperature > 0) {
            state = new LightState(brightness, temperature);
        } else {
            state = new LightState(brightness, hue, saturation);
        }
        hueBridge.setLightState(userName, lampId, state);
    }

    public void registerUser() {
        String result = hueBridge.registerUser("OpenNetHomeServer", userName);
        if (result != null && result != "") {
            userName = result;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        hueBridge = new PhilipsHueBridge(url, "");
    }

    public String getBridgeIdentity() {
        return bridgeIdentity;
    }

    public void setBridgeIdentity(String bridgeIdentity) {
        this.bridgeIdentity = bridgeIdentity;
    }
}
