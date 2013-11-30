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

package nu.nethome.home.items.infra;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class LocationTest {

    private Location location;

    @Mock private HomeService server;
    @Mock private Event sentEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(server.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        location = new Location();
        location.activate(server);
    }

    @Test
    public void modelIsParsableXML() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(location.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }
}
