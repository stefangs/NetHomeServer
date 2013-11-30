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

package nu.nethome.home.items.nexa;

import nu.nethome.coders.decoders.NexaLDecoder;
import nu.nethome.coders.encoders.Encoders;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.ps.BadMessageException;
import nu.nethome.util.ps.Message;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolEncoder;
import nu.nethome.util.ps.impl.PulseTestPlayer;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NexaLCLampScenarioTest {

    TstHomeService server;
    PulseTestPlayer player;
    ProtocolDecoder decoder;
    NexaLCLamp lamp;
    EncoderFactory factory;

    @Before
    public void setUp() throws Exception {

        server = new TstHomeService();
        player = new PulseTestPlayer();
        decoder = new NexaLDecoder();
        lamp = new NexaLCLamp();
        factory = new EncoderFactory(Encoders.getAllTypes());
        player.setDecoder(decoder);
        decoder.setTarget(player);
    }

    @Test
    public void onCommand() throws BadMessageException {
        lamp.activate(server);
        lamp.setAddress("1234");
        lamp.setButton("7");
        lamp.on();
        ProtocolEncoder foundEncoder = factory.getEncoder(server.sentEvents.get(0));
        Message message = factory.extractMessage(server.sentEvents.get(0));
        player.playMessage(foundEncoder.encode(message, ProtocolEncoder.Phase.REPEATED));

        assertThat(player.getMessageField(0, "Address"), is(1234));
        assertThat(player.getMessageField(0, "Button"), is(7));
        assertThat(player.getMessageField(0, "Command"), is(1));
    }
}
