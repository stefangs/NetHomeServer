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

import nu.nethome.home.items.util.TstHomeItemProxy;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.apache.xerces.parsers.SAXParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Stefan
 * Date: 2012-05-29
 * Time: 20:56
 */
public class NexaLCRemapButtonTest {
 	// Local Members
	private HomeService server;
    private NexaLCRemapButton testItem;

	@Before
	public void setUp() throws Exception {
		server = new TstHomeService();
		testItem = new NexaLCRemapButton();
        testItem.setHoldOffTime("0");
	}

	@After
	public void tearDown() throws Exception {
		testItem = null;
	}

	@Test
	public void testGetModel() throws SAXException, IOException {
		SAXParser parser = new SAXParser();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(testItem.getModel().getBytes());
		InputSource source = new InputSource(byteStream);
		// Just verify that the XML is valid
		parser.parse(source);
	}

	@Test
	public void testSetName() {
		testItem.setName("FooBar");
		assertTrue(testItem.getName().equals("FooBar"));
	}


	@Test
	public void testGetSetOnCommand() {
		assertTrue(testItem.getOnCommand().equals(""));
		testItem.setOnCommand("FooBar");
		assertTrue(testItem.getOnCommand().equals("FooBar"));
	}

	@Test
	public void testGetOffCommand() {
		assertTrue(testItem.getOffCommand().equals(""));
		testItem.setOffCommand("FieBar");
		assertTrue(testItem.getOffCommand().equals("FieBar"));
	}

	@Test
	public void testAddressHouseCode() {
		assertEquals("0", testItem.getAddress());
		testItem.setAddress("17");
		assertEquals("17", testItem.getAddress());
		testItem.setAddress("I");
		assertEquals("17", testItem.getAddress());
	}

	@Test
	public void testGetSetButton() {
		assertEquals("1", testItem.getButton());
		testItem.setButton("3");
		assertEquals("3", testItem.getButton());
		testItem.setButton("32");
		assertEquals("32", testItem.getButton());
		testItem.setButton("33");
		assertEquals("32", testItem.getButton());
		testItem.setButton("olle");
		assertEquals("32", testItem.getButton());
	}

	@Test
	public void testReceiveOnCommand() throws InterruptedException {
		// Set up the remap button
		testItem.setOnCommand("call,Foo,OnMethod");
		testItem.setButton("1");
		testItem.setAddress("17");
		testItem.activate(server);

		// Create and "send" the on-event
		Event onEvent = server.createEvent("NexaL_Message", "");
		onEvent.setAttribute("Direction", "In");
		onEvent.setAttribute("NexaL.Address", "17");
		onEvent.setAttribute("NexaL.Button", "1");
		onEvent.setAttribute("NexaL.Command", "1");
		testItem.receiveEvent(onEvent);

		// Get the faked target instance and verify it has been called
		TstHomeItemProxy foo = (TstHomeItemProxy)server.openInstance("Foo");
		assertEquals(1, foo.getNumberOfCalledActions());
		assertEquals(1, foo.getNumberTimesCalled("OnMethod"));

		testItem.stop();
	}

	@Test
	public void testReceiveOffCommand() throws InterruptedException {
		// Set up the remap button
		testItem.setOffCommand("call,Foo,OffMethod");
		testItem.setButton("7");
		testItem.setAddress("5000");
		testItem.activate(server);

		// Create and "send" the on-event
		Event onEvent = server.createEvent("NexaL_Message", "");
		onEvent.setAttribute("Direction", "In");
		onEvent.setAttribute("NexaL.Address", "5000");
		onEvent.setAttribute("NexaL.Button", "7");
		onEvent.setAttribute("NexaL.Command", "0");
		testItem.receiveEvent(onEvent);

		// Get the faked target instance and verify it has been called
		TstHomeItemProxy foo = (TstHomeItemProxy)server.openInstance("Foo");
		assertEquals(1, foo.getNumberOfCalledActions());
		assertEquals(1, foo.getNumberTimesCalled("OffMethod"));

		testItem.stop();
	}


}
