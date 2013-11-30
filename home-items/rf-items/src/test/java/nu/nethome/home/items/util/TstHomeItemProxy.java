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

package nu.nethome.home.items.util;

import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the HomeItemProxy-interface designed for test purposes.
 * It accepts calls to any methods and counts the number of calls to each.
 * 
 * @author Stefan
 *
 */
public class TstHomeItemProxy implements HomeItemProxy {

	protected String m_Name;
	protected Map<String, Integer> m_CalledActions = new HashMap<String, Integer>();
	private String m_LastCalled;
	
	public TstHomeItemProxy(String name) {
		m_Name = name;
	}
	
	public String callAction(String actionName) {
		int current;
		current = m_CalledActions.containsKey(actionName) ? 
				m_CalledActions.get(actionName) : 0;
		current += 1;
		m_CalledActions.put(actionName, current);
		m_LastCalled = actionName;
		if (actionName.contains("Sleep")) {
			try {
				Thread.sleep(80);
			} catch (InterruptedException e) {
				// Nothing to do...
			}
		}
		return "";
	}

    public HomeItemModel getModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getInternalRepresentation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumberTimesCalled(String actionName) {
		return m_CalledActions.containsKey(actionName) ? 
				m_CalledActions.get(actionName) : 0;			
	}
	
	public int getNumberOfCalledActions() {
		return m_CalledActions.size();
	}

	public List<Action> getActions() {
		return null;
	}

	public String getAttributeValue(String attributeName) {
		return null;
	}

	public List<Attribute> getAttributeValues() {
		return null;
	}

	public boolean setAttributeValue(String attributeName,
			String attributeValue) {
		return false;
	}

	public String getName() {
		return m_Name;
	}

	/**
	 * Returns the name of the last method that was called
	 * @return The name
	 */
	public String getLastCalled() {
		return m_LastCalled;
	}

}
