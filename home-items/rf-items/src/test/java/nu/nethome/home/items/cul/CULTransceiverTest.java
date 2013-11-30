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

package nu.nethome.home.items.cul;

import nu.nethome.home.items.pronto.ProntoLamp;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.system.Event;
import nu.nethome.util.ps.impl.CULProtocolPort;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class CULTransceiverTest {

    CULTransceiver transceiver;
    CULProtocolPort port;
    TstHomeService server;
    ProntoLamp lamp;
    Event event;
    protected final String sonyMessage1 =
   			"0000 0067 0000 0015 0060 0018 0018 0018 0030 0018 0030 0018 0030 " +
   			"0018 0018 0018 0030 0018 0018 0018 0018 0018 0030 0018 0018 0018 " +
   			"0030 0018 0030 0018 0030 0018 0018 0018 0018 0018 0030 0018 0018 " +
   			"0018 0018 0018 0030 0018 0018 03f6";
    public static final int LENGTH_OF_SONY_MESSAGE = 42;
    public static final int MODULATION_ON_OFF_PERIOD = 33;


    @Before
    public void init() {
        transceiver = new CULTransceiver();
        port = mock(CULProtocolPort.class);
        transceiver.culPort = port;
        server = new TstHomeService();
        lamp = new ProntoLamp();
        lamp.activate(server);
        lamp.setOnCode(sonyMessage1);
        when(port.isOpen()).thenReturn(true);
    }

    @Test
    public void EncodesAndPlaysReceivedMessage() {
        lamp.on();
        transceiver.receiveEvent(server.sentEvents.get(0));
        ArgumentCaptor<int[]> countCaptor = ArgumentCaptor.forClass(int[].class);
        verify(port).playMessage(countCaptor.capture(), anyInt(), anyInt());
        assertThat(countCaptor.getValue().length, is(LENGTH_OF_SONY_MESSAGE));
    }

    @Test
    public void playsRepeatFromEncoder() {
        lamp.on();
        transceiver.receiveEvent(server.sentEvents.get(0));
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(port).playMessage(any(int[].class), countCaptor.capture(), anyInt());
        assertThat(countCaptor.getValue(), is(5));
    }

    @Test
    public void repeatFromEventOverridesEncoder() {
        lamp.on();
        server.sentEvents.get(0).setAttribute("Repeat", 10);
        transceiver.receiveEvent(server.sentEvents.get(0));
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(port).playMessage(any(int[].class), countCaptor.capture(), anyInt());
        assertThat(countCaptor.getValue(), is(10));
    }

    @Test
    public void noModulationIfNotSpecified() {
        lamp.setUseModulation("false");
        lamp.on();
        transceiver.receiveEvent(server.sentEvents.get(0));
        verify(port).playMessage(any(int[].class), anyInt(), anyInt());
        verify(port, times(2)).setModulationOffPeriod(0);
        verify(port, times(2)).setModulationOnPeriod(0);
    }

    @Test
    public void modulationFromEncoderIfSpecifiedInEvent() {
        lamp.setUseModulation("true");
        lamp.on();
        transceiver.receiveEvent(server.sentEvents.get(0));
        verify(port).playMessage(any(int[].class), anyInt(), anyInt());
        verify(port).setModulationOffPeriod(0);
        verify(port).setModulationOnPeriod(0);
        verify(port).setModulationOffPeriod(MODULATION_ON_OFF_PERIOD);
        verify(port).setModulationOnPeriod(MODULATION_ON_OFF_PERIOD);
    }
}
