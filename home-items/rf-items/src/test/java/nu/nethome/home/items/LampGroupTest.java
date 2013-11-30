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

package nu.nethome.home.items;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by Stefan 2013-11-17
 */
public class LampGroupTest {

    HomeItemProxy lampOn1;
    HomeItemProxy lampOn2;
    HomeItemProxy lampOff;
    LampGroup lampGroup;
    HomeService server;

    @Before
    public void setUp() throws Exception {
        lampGroup = new LampGroup();

        lampOn1 = mock(HomeItemProxy.class);
        when(lampOn1.getAttributeValue("State")).thenReturn("on");
        when(lampOn1.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)).thenReturn("1");

        lampOn2 = mock(HomeItemProxy.class);
        when(lampOn2.getAttributeValue("State")).thenReturn("on");
        when(lampOn2.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)).thenReturn("3");

        lampOff = mock(HomeItemProxy.class);
        when(lampOff.getAttributeValue("State")).thenReturn("off");
        when(lampOff.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)).thenReturn("2");

        server = mock(HomeService.class);
        when(server.openInstance("1")).thenReturn(lampOn1);
        when(server.openInstance("2")).thenReturn(lampOff);
        when(server.openInstance("3")).thenReturn(lampOn2);

        lampGroup.setLamps("1,2,3");
        lampGroup.setDelay("0");
        lampGroup.activate(server);
    }

    @After
    public void tearDown() {
        lampGroup.stop();
    }

    @Test
    public void canTurnAllLampsOn() throws ExecutionFailure {
        lampGroup.performOn();
        sleep(100);
        verify(lampOn1, times(1)).callAction("on");
        verify(lampOn2, times(1)).callAction("on");
        verify(lampOff, times(1)).callAction("on");
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    @Test
    public void canTurnAllLampsOff() throws ExecutionFailure {
        lampGroup.performOff();
        sleep(100);
        verify(lampOn1, times(1)).callAction("off");
        verify(lampOn2, times(1)).callAction("off");
        verify(lampOff, times(1)).callAction("off");
    }

    @Test
    public void savesStateWhenTurningLampsOff() throws ExecutionFailure {
        lampGroup.performOff();
        sleep(100);
        assertThat(lampGroup.getLampsOn(), is("1,3"));
    }

    @Test
    public void canRestoreLampState() throws ExecutionFailure {
        lampGroup.performOff();
        lampGroup.performRecall();
        sleep(100);
        verify(lampOn1, times(1)).callAction("on");
        verify(lampOn2, times(1)).callAction("on");
        verify(lampOff, times(0)).callAction("on");
    }

    @Test
    public void pausesBetweenOff() throws ExecutionFailure {
        lampGroup.setDelay("100");
        lampGroup.performOff();
        sleep(10);
        verify(lampOn1, times(1)).callAction("off");
        verify(lampOn2, times(0)).callAction("off");
        verify(lampOff, times(0)).callAction("off");
        sleep(200);
        verify(lampOn2, times(1)).callAction("off");
        sleep(400);
        verify(lampOff, times(1)).callAction("off");
    }
}
