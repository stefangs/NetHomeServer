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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class PhilipsHueBridge {

    String url = "http://192.168.1.174";
    JsonRestClient client = new JsonRestClient();

    public PhilipsHueBridge(String address) {
        this.url = "http://" + address;
    }

    PhilipsHueBridge(JsonRestClient client, String address) {
        this.url = "http://" + address;
        this.client = client;
    }

    public void setLightState(String user, String lamp, LightState state) {
        JSONObject stateParameter = new JSONObject();
        if (state.isOn()) {
            stateParameter.put("on", true);
            stateParameter.put("bri", state.getBrightness());
            stateParameter.put("hue", state.getHue());
            stateParameter.put("sat", state.getSaturation());
        } else {
            stateParameter.put("on", false);
        }
        String resource = String.format("/api/%s/lights/%s/state", user, lamp);
        try {
            JSONArray result = client.put(url, resource, stateParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
