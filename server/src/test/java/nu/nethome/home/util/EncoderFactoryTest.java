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

package nu.nethome.home.util;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.system.Event;
import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.Message;
import nu.nethome.util.ps.ProtocolEncoder;
import nu.nethome.util.ps.ProtocolInfo;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncoderFactoryTest {

    EncoderFactory factory;
    Event event;
    ProtocolEncoder encoder;
    ProtocolInfo info;

    @Before
    public void setUp() throws Exception {
        factory = new EncoderFactory();
        encoder = mock(ProtocolEncoder.class);
        info = mock(ProtocolInfo.class);
        when(encoder.getInfo()).thenReturn(info);
        when(info.getName()).thenReturn("Nexa");
    }

    @Test
    public void decodesAttributesFromProtocolEvent() throws Exception {
        event = new InternalEvent("Nexa_Message");
        event.setAttribute("Nexa.1", "one");
        event.setAttribute("Nexa.2", "two");
        event.setAttribute("3", "three");

        Message message = factory.extractMessage(event);

        assertThat(message.getFields().size(), is(2));
        assertThat(message.getFields(), hasItem(new FieldValue("1", "one")));
        assertThat(message.getFields(), hasItem(new FieldValue("2", "two")));
    }

    @Test
    public void handlesAttributesInIncompleteProtocolEvents() throws Exception {
        event = new InternalEvent("_Message");
        event.setAttribute("3", "three");
        assertThat(factory.extractMessage(event).getFields().size(), is(0));

        event = new InternalEvent("Nexa_Message");
        event.setAttribute("Nexa.", "none");
        assertThat(factory.extractMessage(event).getFields().size(), is(0));
    }

    @Test
    public void canFindAddedEncoder() throws Exception {
        factory.addEncoder(encoder);
        event = new InternalEvent("Nexa_Message");
        assertThat(factory.getEncoder(event), is(encoder));
    }

    @Test
    public void canHandleNonProtocolMessage() throws Exception {
        factory.addEncoder(encoder);
        event = new InternalEvent("Foo");
        assertThat(factory.getEncoder(event), is(nullValue()));
    }

    @Test
    public void canHandleUnsupportedProtocolMessage() throws Exception {
        factory.addEncoder(encoder);
        event = new InternalEvent("Deltronic_Message");
        assertThat(factory.getEncoder(event), is(nullValue()));
    }
}
