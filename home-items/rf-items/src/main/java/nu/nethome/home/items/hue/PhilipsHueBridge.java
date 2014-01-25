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

import nu.nethome.home.system.Event;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class PhilipsHueBridge {

    public static final String WWW_MEETHUE_COM_API = "http://www.meethue.com";
    public static final String HUE_NUPNP = "/api/nupnp";

    public static class Identity {
        public final String id;
        public final String address;

        public Identity(String id, String address) {
            this.id = id;
            this.address = address;
        }
    }

    private String url = "http://192.168.1.174";
    private String id = "";

    JsonRestClient client = new JsonRestClient();

    public PhilipsHueBridge(String address) {
        this.url = "http://" + address;
    }

    public PhilipsHueBridge(Identity id) {
        this.url = "http://" + id.address;
        this.id = id.id;
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
            JSONData result = client.put(url, resource, stateParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Light getLight(String user, String lamp) {
        String resource = String.format("/api/%s/lights/%s", user, lamp);
        try {
            JSONData result = client.get(url, resource, null);
            if (result.isObject()) {
                return new Light(result.getObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<LightId> listLights(String user) {
        return Collections.EMPTY_LIST; // TODO: Implement!
    }

    public static List<Identity> listLocalPhilipsHueBridges() {
        JsonRestClient client = new JsonRestClient();
        try {
            JSONData result = client.get(WWW_MEETHUE_COM_API, HUE_NUPNP, null);
            if (!result.isObject()) {
                List<Identity> list = new ArrayList<Identity>();
                for (int i = 0; i < result.getArray().length(); i++) {
                    JSONObject id = result.getArray().getJSONObject(i);
                    list.add(new Identity(id.getString("id"), id.getString("internalipaddress")));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}