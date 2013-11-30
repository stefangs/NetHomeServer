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

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemModel;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class can process the HomeItem model description XML-file and extract the
 * information into Java objects. It is used both internally by the server for model
 * processing and by HomeItems via the HomeItemModel interface
 */
public class InternalHomeItemModel implements HomeItemModel {

    private static Logger logger = Logger.getLogger(InternalHomeItemModel.class.getName());

	private String className = "";
	private String category = "";
	private int startOrder = 5;
	private String defaultAction = "";
	private String defaultAttribute = "";
    private HomeItem item;
	
	public InternalHomeItemModel(HomeItem item) {

        this.item = item;
        String modelXML = item.getModel();
		// Start parsing the Items model description
		DOMParser parser = new DOMParser();
		ByteArrayInputStream byteStream;
		try {
			byteStream = new ByteArrayInputStream(modelXML.getBytes("UTF-8"));
			InputSource source = new InputSource(byteStream);
			parser.parse(source);
		} catch (UnsupportedEncodingException e1) {
            logger.log(Level.WARNING, "Failed parsing model for " + item.getName(), e1);
			return;
		}
		catch (Exception e){
            logger.log(Level.WARNING, "Failed parsing model for " + item.getName(), e);
			return;
		}
		Document document = parser.getDocument();
		Node homeItem = document.getDocumentElement();
		// Verify that it really is a HomeItemNode
		if (!homeItem.getNodeName().equals("HomeItem")) {
			return;
		}
		int numberAttributes = 0;
		if (homeItem.getAttributes() != null) {
			numberAttributes = homeItem.getAttributes().getLength(); 
		}
		// Loop through all the attribute element's attributes and process them
		for (int elemAttIndex = 0; elemAttIndex < numberAttributes; elemAttIndex++) {
			Attr attribute = (Attr)homeItem.getAttributes().item(elemAttIndex);
			String name = attribute.getNodeName();
			String value = attribute.getNodeValue();
			// Check for Name Element
			if (name.equals("Class") ) {
				className = value;
			}
			if (name.equals("Category") ) {
				category = value;
			}
			if (name.equals("StartOrder") ) {
				startOrder = Integer.parseInt(value);
			}
		}
		// Get the child nodes of the Item - the attributes and actions and get the defaults
		NodeList elements = homeItem.getChildNodes();
		if (elements != null) {
			int numberOfChilds = elements.getLength();
			// Loop through all elements of the object, get the attributes and actions
			for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
				// Find out if the found element is an action and process it if it is
				if (elements.item(loopIndex).getNodeName().equals("Action")) {
					// Get the attributes of the Action element
					NamedNodeMap attributes = elements.item(loopIndex).getAttributes();
					// Check if there is an "default" attribute
					if ((attributes != null) && (attributes.getNamedItem("Default") != null) && 
							(attributes.getNamedItem(InternalHomeItemProxy.NAME_ATTRIBUTE) != null)) {
						defaultAction = attributes.getNamedItem(InternalHomeItemProxy.NAME_ATTRIBUTE).getNodeValue();
					}
				}
				// Find out if the found element is an attribute and process it if it is
				if (elements.item(loopIndex).getNodeName().equals("Attribute")) {
					// Get the attributes of the Action element
					NamedNodeMap attributes = elements.item(loopIndex).getAttributes();
					// Check if there is an "Default" attribute
					if ((attributes != null) && (attributes.getNamedItem("Default") != null) && 
							(attributes.getNamedItem(InternalHomeItemProxy.NAME_ATTRIBUTE) != null)) {
						defaultAttribute = attributes.getNamedItem(InternalHomeItemProxy.NAME_ATTRIBUTE).getNodeValue();
					}
				}
			}
		}
	}

	/**
	 * @return the Category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the ClassName
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the startOrder
	 */
	public int getStartOrder() {
		return startOrder;
	}

	/**
	 * @return the default action of the class. Empty string if none.
	 */
	public String getDefaultAction() {
		return defaultAction;
	}

	/**
	 * @return the default attribute of the class. Empty string if none.
	 */
	public String getDefaultAttribute() {
		return defaultAttribute;
	}

    /**
     * TODO: make more efficient, move here?
     * @param attributeName Name of the attribute or an empty string
     * @return
     */
    public List<Attribute> getAttributeValues(String attributeName) {
        return new InternalHomeItemProxy(item, null).getAttributeValues();
    }

    public List<Action> getActions() {
        return new InternalHomeItemProxy(item, null).getActions();
    }

    public static String[] getAllCategories() {
		return HOME_ITEM_CATEGORIES;
	}
}
