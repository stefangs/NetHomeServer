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

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.util.TstHomeItemProxy;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * User: Stefan
 * Date: 2012-05-29
 * Time: 21:22
 */
public class NexaSmokeDetectorTest {

    	// Local Members
	private HomeService server;
    private NexaSmokeDetector testItem;
    private HomeItemProxy testProxy;

	@Before
	public void setUp() throws Exception {
		server = new TstHomeService();
		testItem = new NexaSmokeDetector();
        testItem.activate(server);
		testProxy = new LocalHomeItemProxy(testItem, null);
	}

	@After
	public void tearDown() throws Exception {
		testItem = null;
	}

	/**
	 * Test getters and setters via the proxy interface, so the whole chain via the
	 * model is tested.
	 * @throws Throwable
	 */
	@Test
	public void testGetSetAddress() throws IllegalValueException {
		assertEquals("0", testProxy.getAttributeValue("Address"));
		testProxy.setAttributeValue("Address", "12345");
		assertEquals("12345", testProxy.getAttributeValue("Address"));
	}

	@Test
	public void testGetSetAlarmCommand() throws IllegalValueException {
		assertEquals("", testProxy.getAttributeValue("AlarmCommand"));
		testProxy.setAttributeValue("AlarmCommand", "call,test,foo");
		assertEquals("call,test,foo", testProxy.getAttributeValue("AlarmCommand"));
	}

	@Test
	public void testAlarmEvent() throws IllegalValueException {
		Event alarm = server.createEvent("NexaFire_Message", "");
		alarm.setAttribute("Direction", "In");
		alarm.setAttribute("NexaFire.Address", "17");
		testProxy.setAttributeValue("Address", "17");
		testProxy.setAttributeValue("AlarmCommand", "call,foo,fie");
		assertEquals("Idle", testProxy.getAttributeValue("State"));
		testItem.receiveEvent(alarm);
		TstHomeItemProxy proxy = (TstHomeItemProxy) server.openInstance("foo");
		assertEquals(1, proxy.getNumberTimesCalled("fie"));
		assertEquals("Alarm", testProxy.getAttributeValue("State"));
	}

	@Test
	public void testNoAlarmEvent() throws IllegalValueException {
		Event alarm = server.createEvent("NexaFire_Message", "");
		alarm.setAttribute("Direction", "In");
		alarm.setAttribute("NexaFire.Address", "555");
		testProxy.setAttributeValue("Address", "17");
		testProxy.setAttributeValue("AlarmCommand", "call,foo,fie");
		assertEquals("Idle", testProxy.getAttributeValue("State"));
		testItem.receiveEvent(alarm);
		TstHomeItemProxy proxy = (TstHomeItemProxy) server.openInstance("foo");
		assertEquals(0, proxy.getNumberTimesCalled("fie"));
		assertEquals("Idle", testProxy.getAttributeValue("State"));
	}

	@Test
	public void testLearn() throws IllegalValueException, ExecutionFailure {
		// Create alarm event
		Event alarm = server.createEvent("NexaFire_Message", "");
		alarm.setAttribute("Direction", "In");
		alarm.setAttribute("NexaFire.Address", "555");

		// Configure Item and verify initial state
		testProxy.setAttributeValue("AlarmCommand", "call,foo,fie");
		assertEquals("0", testProxy.getAttributeValue("Address"));
		assertEquals("Idle", testProxy.getAttributeValue("State"));

		// Set Item in Learn mode and verify state
		testProxy.callAction("LearnAddress");
		assertEquals("Learning", testProxy.getAttributeValue("State"));

		// Send the alarm event
		testItem.receiveEvent(alarm);

		// Verify alarm state and new learned address
		assertEquals("Alarm", testProxy.getAttributeValue("State"));
		assertEquals("555", testProxy.getAttributeValue("Address"));

		// Verify alarm action is called
		TstHomeItemProxy proxy = (TstHomeItemProxy) server.openInstance("foo");
		assertEquals(1, proxy.getNumberTimesCalled("fie"));
	}

	@Ignore
	public void testGetRepeatTime() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testSetRepeatTime() {
		fail("Not yet implemented");
	}


}
