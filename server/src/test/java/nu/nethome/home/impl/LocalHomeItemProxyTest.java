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

package nu.nethome.home.impl;

import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;

public class LocalHomeItemProxyTest {

    LocalHomeItemProxy itemProxy;
    MockHomeItem item;
    HomeService server;

    @Before
    public void setUp() throws Exception {
        item = new MockHomeItem();
        server = mock(HomeService.class);
        itemProxy = new LocalHomeItemProxy(item, server);
    }

    @After
    public void tearDown() throws Exception {
        itemProxy = null;
    }

    @Test
    public void canGetAttributeValue() throws Exception {
        assertEquals(item.getAttValueFoo(), itemProxy.getAttributeValue("AttValueFoo"));
        assertEquals(2, item.getCalledMethods().size());
        assertEquals("getAttValueFoo", item.getCalledMethods().get(0));
        assertEquals("getAttValueFoo", item.getCalledMethods().get(1));
    }

    @Test
    public void ignoresUnknownAttribute() throws Exception {
        assertEquals("", itemProxy.getAttributeValue("AttBadGet"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void ignoresNoGet() throws Exception {
        assertEquals("", itemProxy.getAttributeValue("AttNoGet"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void ignoresPrivateGet() throws Exception {
        assertEquals("", itemProxy.getAttributeValue("AttPrivate"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void ignoresExceptionOnGet() throws Exception {
        assertEquals("", itemProxy.getAttributeValue("AttException"));
        assertEquals(1, item.getCalledMethods().size());
        assertEquals("getAttException", item.getCalledMethods().get(0));
    }

    @Test
    public void ignoresNonExisting() throws Exception {
        assertEquals("", itemProxy.getAttributeValue("NonExisting"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void canSetAttributeValue() throws Exception {
        assertTrue(itemProxy.setAttributeValue("AttValueFoo", "Fie"));
        assertEquals("Fie", item.getAttValueFoo());
        assertEquals(2, item.getCalledMethods().size());
        assertEquals("setAttValueFoo", item.getCalledMethods().get(0));
        assertEquals("getAttValueFoo", item.getCalledMethods().get(1));
    }

    @Test
    public void returnsFalseIfBadSet() throws Exception {
        assertThat(itemProxy.setAttributeValue("AttBadGet", "Fie"), is(false));
        assertThat(item.getCalledMethods().size(), is(0));
    }

    @Test
    public void returnsFalseIfNoSetMethod() throws Exception {
        assertThat(itemProxy.setAttributeValue("AttNoGet", "Fie"), is(false));
        assertThat(item.getCalledMethods().size(), is(0));
    }

    @Test
    public void returnsFalseIfSetPrivateAttribute() throws Exception {
        assertThat(itemProxy.setAttributeValue("AttPrivate", "Fie"), is(false));
        assertThat(item.getCalledMethods().size(), is(0));
    }

    @Test
    public void passesIllegalValueExceptionOnSet() throws Exception {
        try {
            assertFalse(itemProxy.setAttributeValue("AttException", "Fie"));
        } catch (IllegalValueException e) {
            assertEquals("Foo", e.getMessage());
            assertEquals("Fie", e.getValue());
            assertEquals(1, item.getCalledMethods().size());
            assertEquals("setAttException", item.getCalledMethods().get(0));
            return;
        }
        fail();
    }

    @Test
    public void testFailSetInitAttribute() throws Exception {
        assertThat(itemProxy.setAttributeValue("AttInit", "Fie"), is(false));
        assertThat(item.getAttInit(), is("Init"));
        assertThat(item.getCalledMethods().size(), is(1));
    }

    @Test
    public void testSetInitAttribute() throws Exception {
        item.setName("#Foo");
        assertTrue(itemProxy.setAttributeValue("AttInit", "Fie"));
        assertEquals("Fie", item.getAttInit());
        assertEquals(2, item.getCalledMethods().size());
    }

    @Test
    public void cannotInitName() throws Exception {
        item.setName("#Foo");
        assertThat(itemProxy.setAttributeValue("Name", "Foo"), is(false));
        assertThat(item.getName(), not(is("Foo")));
    }

    @Test
    public void cannotInitID() throws Exception {
        item.setName("#Foo");
        assertThat(itemProxy.setAttributeValue("ID", "555"), is(false));
        assertThat(item.getItemId(), not(is(555L)));
    }

    @Test
    public void testInitAttributeBadSet() throws Exception {
        item.setName("#Foo");
        assertFalse(itemProxy.setAttributeValue("AttBadGet", "Fie"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testInitAttributeNoSet() throws Exception {
        item.setName("#Foo");
        assertFalse(itemProxy.setAttributeValue("AttNoGet", "Fie"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testInitPrivateAttribute() throws Exception {
        item.setName("#Foo");
        assertFalse(itemProxy.setAttributeValue("AttPrivate", "Fie"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testInitAttributeException() throws Exception {
        item.setName("#Foo");
        try {
            assertFalse(itemProxy.setAttributeValue("AttException", "Fie"));
        } catch (IllegalValueException e) {
            assertEquals("Foo", e.getMessage());
            assertEquals("Fie", e.getValue());
            assertEquals(1, item.getCalledMethods().size());
            assertEquals("setAttException", item.getCalledMethods().get(0));
            return;
        }
        fail();
    }

    @Test
    public void testCallAction() throws Exception {
        assertEquals("Foo", itemProxy.callAction("ReturnFoo"));
        assertEquals(1, item.getCalledMethods().size());
        assertEquals("returnFoo", item.getCalledMethods().get(0));
    }

    @Test
    public void testCallActionBadSignature() throws Exception {
        assertEquals("", itemProxy.callAction("BadSignature"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testCallBadAction() throws Exception {
        assertEquals("", itemProxy.callAction("BadAction"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testCallNoAction() throws Exception {
        assertEquals("", itemProxy.callAction("NoAction"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testCallPrivateAction() throws Exception {
        assertEquals("", itemProxy.callAction("PrivateAction"));
        assertEquals(0, item.getCalledMethods().size());
    }

    @Test
    public void testCallActionException() throws Exception {
        try {
            assertEquals("", itemProxy.callAction("ExceptionAction"));
            assertEquals(0, item.getCalledMethods().size());
        } catch (ExecutionFailure executionFailure) {
            assertEquals("Foo", executionFailure.getMessage());
        }
    }

    @Test
    public void testGetAllAttributes() {
        List<Attribute> atts = itemProxy.getAttributeValues();
        assertEquals(9, atts.size());
    }

    @Test
    public void testGetAttributeSpec() {
        Attribute foo = getSpecificAttribute(itemProxy, "AttValueFoo");
        assertEquals("AttValueFoo", foo.getName());
        assertEquals("String", foo.getType());
        assertTrue(foo.isCanInit());
        assertFalse(foo.isReadOnly());
        assertFalse(foo.isWriteOnly());
    }

    @Test
    public void testGetAttributeSpecReadOnly() {
        Attribute foo = getSpecificAttribute(itemProxy, "AttReadOnly");
        assertEquals("AttReadOnly", foo.getName());
        assertEquals("String", foo.getType());
        assertFalse(foo.isCanInit());
        assertTrue(foo.isReadOnly());
        assertFalse(foo.isWriteOnly());
    }

    @Test
    public void testGetAllActions() {
        List<Action> actions = itemProxy.getModel().getActions();
        assertEquals(1, actions.size());
        assertEquals("ReturnFoo", actions.get(0).getName());
    }

    @Test
    public void testGetAttributeSpecStringList() {
        Attribute foo = getSpecificAttribute(itemProxy, "AttBadGet");
        assertEquals(3, foo.getValueList().size());
        assertEquals("Foo", foo.getValueList().get(0));
        assertEquals("Fie", foo.getValueList().get(1));
        assertEquals("Fum", foo.getValueList().get(2));
    }

    @Test
    public void callToActivateActivatesItem() throws ExecutionFailure {
        itemProxy.callAction("activate");
        assertThat(item.server, is(server));
        assertThat(item.getCalledMethods(), hasItem("activate"));
    }

    @Test
    public void canGetModel() {
        assertThat(itemProxy.getAttributeValue(LocalHomeItemProxy.MODEL_ATTRIBUTE), is(item.getModel()));
    }

    @Test
    public void cannotSetModel() throws IllegalValueException {
        assertThat(itemProxy.setAttributeValue(LocalHomeItemProxy.MODEL_ATTRIBUTE, "Foo"), is(false));
        assertThat(itemProxy.getAttributeValue(LocalHomeItemProxy.MODEL_ATTRIBUTE), not(is("Foo")));
    }

    @Test
    public void canGetName() {
        assertThat(itemProxy.getAttributeValue(LocalHomeItemProxy.NAME_ATTRIBUTE), is(item.getName()));
    }

    @Test
    public void cannotSetName() throws IllegalValueException {
        assertThat(itemProxy.setAttributeValue(LocalHomeItemProxy.NAME_ATTRIBUTE, "Foo"), is(false));
        assertThat(itemProxy.getAttributeValue(LocalHomeItemProxy.NAME_ATTRIBUTE), not(is("Foo")));
    }

    @Test
    public void canGetId() {
        assertThat(Long.parseLong(itemProxy.getAttributeValue(LocalHomeItemProxy.ID_ATTRIBUTE)), is(item.getItemId()));
    }

    @Test
    public void cannotSetId() throws IllegalValueException {
        assertThat(itemProxy.setAttributeValue(LocalHomeItemProxy.ID_ATTRIBUTE, "17"), is(false));
        assertThat(itemProxy.getAttributeValue(LocalHomeItemProxy.ID_ATTRIBUTE), not(is("17")));
    }

    private Attribute getSpecificAttribute(LocalHomeItemProxy proxy, String name) {
         for (Attribute att : proxy.getAttributeValues()) {
             if (att.getName().equals(name)) {
                 return att;
             }
         }
         return null;
     }
}