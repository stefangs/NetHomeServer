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
/*
 * Copyright (C) 2009 Stefan Stromberg
 * 
 * Project: HomeManager
 *  
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 */

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * MockHomeItem
 * 
 * @author Stefan
 */
public class MockHomeItem implements HomeItem {

	private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"MockHomeItem\" Category=\"Lamps\" >"
			+ "  <Attribute Name=\"AttValueFoo\" Type=\"String\" Get=\"getAttValueFoo\" Set=\"setAttValueFoo\"/>"
			+ "  <Attribute Name=\"AttBadGet\" Type=\"StringList\" Get=\"getAttPrivateXXX\" 	Set=\"setAttBadGetXXX\">"
            + "     <item>Foo</item> <item>Fie</item> <item>Fum</item> </Attribute>"
            + "  <Attribute Name=\"AttNoGet\" Type=\"String\" />"
			+ "  <Attribute Name=\"AttPrivate\" Type=\"String\" Get=\"getAttPrivate\" 	Set=\"setAttPrivate\" />"
			+ "  <Attribute Name=\"AttSet\" Type=\"String\" Get=\"getAttSet\" 	Set=\"setAttSet\" />"
            + "  <Action Name=\"ReturnFoo\" 	Method=\"returnFoo\" />"
			+ "  <Attribute Name=\"AttException\" Type=\"String\" Get=\"getAttException\" 	Set=\"setAttException\" />"
            + "  <Attribute Name=\"AttInit\" Type=\"String\" Get=\"getAttInit\" 	Init=\"setAttInit\" />"
            + "  <Action Name=\"BadSignature\" 	Method=\"badSignature\" />"
            + "  <Action Name=\"BadAction\" 	Method=\"badXXX\" />"
            + "  <Attribute Name=\"AttSet\" Type=\"String\" Get=\"getAttSet\" 	Set=\"setAttSet\" />"
            + "  <Action Name=\"NoAction\" 	/>"
            + "  <Action Name=\"PrivateAction\" 	Method=\"privateAction\" />"
            + "  <Attribute Name=\"AttReadOnly\" Type=\"String\" Get=\"getAttReadOnly\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(MockHomeItem.class.getName());
	protected String m_Name = "NoNameYet";
	protected long m_ID = 0L;
    protected HomeService server;

	// Public attributes
	protected String m_AttValueFoo = "Foo";
	protected String m_AttPrivate = "2";
	protected String m_AttSet = "4";
    protected String m_AttInit = "Init";
    protected String attReadOnly = "Foo";

    private List<String> m_CalledMethods = new ArrayList<String>();

    public List<String> getCalledMethods() {
        return m_CalledMethods;
    }

	/**
	 */
	public MockHomeItem() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
	 */
	public boolean receiveEvent(Event event) {
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("MyEvent")){
				logger.info("Received MyEvent");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#setName(java.lang.String)
	 */
	public void setName(String name) {
		m_Name = name;
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getName()
	 */
	public String getName() {
		return m_Name;
	}

	/* (non-Javadoc)
	 * @see nu.nethome.home.item.HomeItem#getID()
	 */
	public long getItemId() {
		return m_ID;
	}

	/* (non-Javadoc)
	 * @see nu.nethome.home.item.HomeItem#setID(long)
	 */
	public void setItemId(long id) {
		m_ID = id;
	}

    /* Activate the instance
      * @see ssg.home.HomeItem#activate()
      */
	public void activate(HomeService server) {
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
	}
	  
	/**
	 * @return Returns the m_AttValueFoo.
	 */
	public String getAttValueFoo() {
        m_CalledMethods.add("getAttValueFoo");
		return m_AttValueFoo;
	}

    /**
     * @param AttSet The m_AttSet to set.
     */
    public void setAttValueFoo(String AttSet) {
        m_CalledMethods.add("setAttValueFoo");
        m_AttValueFoo = AttSet;
    }

	/**
	 * @return Returns the m_AttPrivate.
	 */
	private String getAttPrivate() {
        m_CalledMethods.add("getAttPrivate");
		return "private";
	}

    /**
     * @param AttSet The m_AttSet to set.
     */
    private void setAttPrivate(String AttSet) {
        m_CalledMethods.add("setAttPrivate");
        m_AttPrivate = AttSet;
    }

	/**
	 * @return Returns the m_AttSet.
	 */
	public String getAttSet() {
        m_CalledMethods.add("getAttSet");
		return m_AttSet;
	}
	/**
	 * @param AttSet The m_AttSet to set.
	 */
	public void setAttSet(String AttSet) {
        m_CalledMethods.add("setAttSet");
		m_AttSet = AttSet;
	}	
	/**
	 * @return Returns the m_AttException.
     * @throws IllegalValueException always
	 */
	public String getAttException() throws IllegalValueException{
        m_CalledMethods.add("getAttException");
        throw new IllegalValueException("Foo", "fie");
	}
	/**
	 * @param AttException The m_AttException to set.
     * @throws IllegalValueException always
	 */
	public void setAttException(String AttException) throws IllegalValueException {
        m_CalledMethods.add("setAttException");
        throw new IllegalValueException("Foo", AttException);
	}

    public String getAttInit() {
        m_CalledMethods.add("getAttInit");
        return m_AttInit;
    }

    public void setAttInit(String m_AttInit) {
        m_CalledMethods.add("setAttInit");
        this.m_AttInit = m_AttInit;
    }

    public String returnFoo() {
        m_CalledMethods.add("returnFoo");
        return "Foo";
    }

    public int badSignature(String s) {
        m_CalledMethods.add("badSignature");
        return 1;
    }

    private String privateAction() {
        m_CalledMethods.add("privateAction");
        return "Foo";
    }

    public void exceptionAction(String AttException) throws IllegalValueException, ExecutionFailure {
        m_CalledMethods.add("exceptionAction");
        throw new ExecutionFailure("Foo");
    }

    public String getAttReadOnly() {
        return attReadOnly;
    }

}


