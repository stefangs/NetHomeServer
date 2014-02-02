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

    private String url = "http://192.168.1.174";
    private String id = "";

    JsonRestClient client = new JsonRestClient();

    public PhilipsHueBridge(String url, String identity) {
        this.url = url;
        this.id = identity;
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
            if (state.getColorTemperature() > 0) {
                stateParameter.put("ct", state.getColorTemperature());
            } else {
                stateParameter.put("hue", state.getHue());
                stateParameter.put("sat", state.getSaturation());
            }
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
        String resource = String.format("/api/%s/lights", user);
        try {
            JSONData result = client.get(url, resource, null);
            if (result.isObject()) {
                List<LightId> list = new ArrayList<LightId>();
                for (String lampId : getFieldNames(result.getObject())) {
                    list.add(new LightId(lampId, result.getObject().getJSONObject(lampId).getString("name")));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] getFieldNames(JSONObject object) {
        String[] result = JSONObject.getNames(object);
        return result == null ? new String[0] : result;
    }


    public static List<PhilipsHueBridge> listLocalPhilipsHueBridges() {
        JsonRestClient client = new JsonRestClient();
        try {
            JSONData result = client.get(WWW_MEETHUE_COM_API, HUE_NUPNP, null);
            if (!result.isObject()) {
                List<PhilipsHueBridge> list = new ArrayList<PhilipsHueBridge>();
                for (int i = 0; i < result.getArray().length(); i++) {
                    JSONObject id = result.getArray().getJSONObject(i);
                    list.add(new PhilipsHueBridge("http://" + id.getString("internalipaddress"), id.getString("id")));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }
}