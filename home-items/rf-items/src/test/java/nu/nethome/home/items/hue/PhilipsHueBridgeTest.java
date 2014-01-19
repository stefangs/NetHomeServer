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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class PhilipsHueBridgeTest {

    public static final String USER_NAME = "test";
    PhilipsHueBridge api;
    JsonRestClient restClient;

    @Before
    public void setUp() {
        restClient = mock(JsonRestClient.class);
        api = new PhilipsHueBridge(restClient, "1.1.1.1");
    }

    @Test
    public void canTurnLampOn() throws Exception {
        LightState onState = new LightState(254, 0, 254);
        api.setLightState(USER_NAME, "2", onState);
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient, times(1)).put(eq("http://1.1.1.1"), eq("/api/test/lights/2/state"), captor.capture());
        assertThat(captor.getValue().getBoolean("on"), is(true));
    }
    @Test
    public void canTurnLampOff() throws Exception {
        LightState off = new LightState();
        api.setLightState(USER_NAME, "2", off);
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient, times(1)).put(eq("http://1.1.1.1"), eq("/api/test/lights/2/state"), captor.capture());
        assertThat(captor.getValue().getBoolean("on"), is(false));
    }
}
