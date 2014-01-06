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

import nu.nethome.home.item.*;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is replaced by LocalHomeItemProxy
 */
@Deprecated
public class InternalHomeItemProxy implements HomeItemProxy {

    // Name constants from the XML file specification
    public static final String ATTRIBUTE_NODE_NAME = "Attribute";
    public static final String NAME_ATTRIBUTE = "Name";
    public static final String TYPE_ATTRIBUTE = "Type";
    public static final String GET_ATTRIBUTE = "Get";
    public static final String SET_ATTRIBUTE = "Set";
    public static final String INIT_ATTRIBUTE = "Init";
    public static final String ACTION_NODE_NAME = "Action";
    public static final String METHOD_ATTRIBUTE = "Method";
    public static final String STRING_LIST_TYPE = "StringList";
    public static final String STRINGS_TYPE = "Strings";

    private HomeItem targetHomeItem;
    private HomeServer server;
    private static Logger logger = Logger.getLogger(HomeItemProxy.class.getName());

    public InternalHomeItemProxy(HomeItem item, HomeServer homeServer) {
        targetHomeItem = item;
        this.server = homeServer;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * ssg.home.items.HomeItemProxy#getAttributeValue(java.lang.String)
      */
    public String getAttributeValue(String attributeName) {
        List<Attribute> value = getAttributeValues(attributeName);
        if (value.size() == 1) {
            return value.get(0).getValue();
        }
        return "";
    }

    public List<Attribute> getAttributeValues() {
        return getAttributeValues("");
    }

    public List<Attribute> getAttributeValues(String attributeName) {
        List<Attribute> result = new LinkedList<Attribute>();

        // Check for hard coded attributes
        if (attributeName.equals("Model")) {
            Attribute model = new InternalAttribute(attributeName, targetHomeItem.getModel());
            result.add(model);
            return result;
        }
        if (attributeName.equals(NAME_ATTRIBUTE)) {
            Attribute name = new InternalAttribute(attributeName, targetHomeItem.getName());
            result.add(name);
            return result;
        }
        if (attributeName.equals("ID")) {
            Attribute name = new InternalAttribute(attributeName, Long.toString(targetHomeItem.getItemId()));
            result.add(name);
            return result;
        }

        Node firstNode = getRootDOMNode(targetHomeItem);
        NodeList elements = firstNode.getChildNodes();
        if (null == elements) {
            return result;
        }
        int numberOfChilds = elements.getLength();

        // Loop through all elements of the object, inspect the attributes
        for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
            InternalAttribute att = parseFromNode(elements.item(loopIndex));

            if ((null != att) && ((attributeName.length() == 0) || (attributeName.equals(att.getName())))) {
                checkStringList(elements.item(loopIndex), att);
                fetchAttributeValue(att);
                result.add(att);
            }
        }
        return result;
    }

    public List<Action> getActions() {
        List<Action> result = new LinkedList<Action>();

        Node firstNode = getRootDOMNode(targetHomeItem);
        NodeList elements = firstNode.getChildNodes();
        if (null == elements) {
            return result;
        }
        int numberOfChilds = elements.getLength();

        // Loop through all elements of the object, collect the actions
        for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
            InternalAction action = parseActionFromNode(elements.item(loopIndex));

            if (null != action) {
                result.add(action);
            }
        }
        return result;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * ssg.home.items.HomeItemProxy#setAttributeValue(java.lang.String,
      * java.lang.String)
      */
    public boolean setAttributeValue(String attributeName, String attributeValue)
            throws IllegalValueException {
        return setAttributeValue(attributeName, attributeValue, getAttributeValue(NAME_ATTRIBUTE).startsWith("#"));
    }

    public boolean initAttributeValue(String attributeName, String attributeValue)
            throws IllegalValueException {
        return setAttributeValue(attributeName, attributeValue, true);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * ssg.home.items.HomeItemProxy#setAttributeValue(java.lang.String,
      * java.lang.String)
      */
    private boolean setAttributeValue(String attributeName, String attributeValue, boolean init)
            throws IllegalValueException {

        boolean result = false;
        logger.config("Set '" + attributeName + "'='" + attributeValue
                + "' in '" + targetHomeItem.getName() + "'");

        // Handle special attributes
        if (attributeName.equals(NAME_ATTRIBUTE)) {
            targetHomeItem.setName(attributeValue);
            return true;
        }
        if (attributeName.equals("ID")) {
            targetHomeItem.setItemId(Long.parseLong(attributeValue));
            return true;
        }

        Node firstNode = getRootDOMNode(targetHomeItem);
        NodeList elements = firstNode.getChildNodes();
        if (null == elements) {
            return false;
        }
        int numberOfChilds = elements.getLength();

        // Loop through all elements of the object, inspect the attributes
        for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
            InternalAttribute att = parseFromNode(elements.item(loopIndex));

            if ((null != att) && (attributeName.equals(att.getName()))) {
                att.setValue(attributeValue);
                result = applyAttributeValue(att, init);
                break;
            }
        }
        return result;
    }

    public String callAction(String actionName) throws ExecutionFailure {
        String returnValue = "";
        logger.fine("Call '" + actionName + "' in '" + targetHomeItem.getName() + "'");
        if (actionName.equals("activate")) {
            targetHomeItem.activate(server);
            return "";
        }

        Node firstNode = getRootDOMNode(targetHomeItem);
        NodeList elements = firstNode.getChildNodes();
        if (null == elements) {
            return "";
        }
        int numberOfChilds = elements.getLength();

        // Loop through all elements of the object, collect the actions
        for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
            InternalAction action = parseActionFromNode(elements.item(loopIndex));

            if ((null != action) && (actionName.equals(action.getName()))) {
                returnValue = callItemAction(action);
            }
        }
        return returnValue;
    }

    public HomeItemModel getModel() {
        return new InternalHomeItemModel(targetHomeItem);
    }

    public Object getInternalRepresentation() {
        return targetHomeItem;
    }

    private void fetchAttributeValue(InternalAttribute att) {
        if (att.isWriteOnly()) {
            return;
        }
        Method getMethod;
        try {
            // fetch the Get-Method
            getMethod = targetHomeItem.getClass().getMethod(att.getGetMethodName(),
                    (Class[]) null);
            if (getMethod != null) {
                // Invoke the Get-Method
                att.setValue((String) getMethod.invoke(targetHomeItem, (Object[]) null));
            }
        } catch (NoSuchMethodException e) {
            logger.warning("No Such Get Method: "
                    + att.getGetMethodName() + " in class "
                    + targetHomeItem.getClass().getName());
        } catch (InvocationTargetException e) {
            logger.warning("Could not invoke Get Method in class "
                    + targetHomeItem.getClass()
                    .getName());
        } catch (IllegalAccessException e) {
            logger.warning("Could not access Get Method in class "
                    + targetHomeItem.getClass()
                    .getName());
        }
    }

    private boolean applyAttributeValue(InternalAttribute att, boolean init) throws IllegalValueException {
        Method setMethod;
        String methodName = init ? att.getInitMethodName() : att.getSetMethodName();
        if (methodName.length() == 0) {
            return false;
        }
        try {
            // fetch the Set-Method from the class
            setMethod = targetHomeItem.getClass().getMethod(methodName,
                    new Class[]{String.class});

            // Invoke the set method
            setMethod.invoke(targetHomeItem, att.getValue());
            return true; // We are done!
        } catch (InvocationTargetException e) {
            // Got an exception in the set method
            // rethrow it
            if (e.getCause() instanceof IllegalValueException) {
                throw (IllegalValueException) e.getCause();
            }
            // If it is not, something strange is happening.
            logger.warning("Could not invoke Set Method in class "
                    + targetHomeItem.getClass().getName());
        } catch (IllegalAccessException e) {
            logger.warning("Could not access Set Method in class "
                    + targetHomeItem.getClass().getName());
        } catch (NoSuchMethodException e) {
            logger.warning("No Such Set Method: "
                    + methodName + " in class "
                    + targetHomeItem.getClass().getName());
        }
        return false;
    }

    private String callItemAction(InternalAction action) throws ExecutionFailure {
        Method actionMethod;
        String returnValue = "";

        try {
            actionMethod = targetHomeItem.getClass().getMethod(action.getActionMethod(), (Class[]) null);
            returnValue = (String) actionMethod.invoke(targetHomeItem, (Object[]) null);
            return returnValue; // We are done!
        } catch (InvocationTargetException e) {
            ExecutionFailure exe = (ExecutionFailure) e.getCause();
            if (exe != null) {
                throw exe;
            } else {
                logger.warning("Could not invoke Action Method in class " + targetHomeItem.getClass().getName());
            }
        } catch (IllegalAccessException e) {
            logger.warning("Could not access Action Method in class " + targetHomeItem.getClass().getName());
        } catch (NoSuchMethodException e) {
            logger.warning("No Such Action Method: " + action.getActionMethod() + " in class " +
                    targetHomeItem.getClass().getName());
        }
        return returnValue;
    }

    private Node getRootDOMNode(HomeItem item) {
        // Start parsing the Items model description
        DOMParser parser = new DOMParser();
        ByteArrayInputStream byteStream;
        try {
            byteStream = new ByteArrayInputStream(item
                    .getModel().getBytes("UTF-8"));
            InputSource source = new InputSource(byteStream);
            parser.parse(source);
            Document document = parser.getDocument();
            return document.getDocumentElement();
        } catch (UnsupportedEncodingException e1) {
            logger.warning(e1.toString());
        } catch (Exception e) {
            logger.warning(e.toString());
        }
        return null;
    }

    /**
     * Parse out attribute object information from a DOM node. If the DOM node is not an attribute, null is returned.
     * Note that the actual value of the attribute is not returned.
     *
     * @param node DOM node to parse
     * @return InternalAttribute object (without value) or null
     */
    private InternalAttribute parseFromNode(Node node) {
        if (!node.getNodeName().equals(ATTRIBUTE_NODE_NAME)) {
            return null;
        }
        NamedNodeMap nodeAttributes = node.getAttributes();
        return new InternalAttribute(getNiceAttributeValue(nodeAttributes, NAME_ATTRIBUTE),
                getNiceAttributeValue(nodeAttributes, TYPE_ATTRIBUTE),
                getNiceAttributeValue(nodeAttributes, GET_ATTRIBUTE),
                getNiceAttributeValue(nodeAttributes, SET_ATTRIBUTE),
                getNiceAttributeValue(nodeAttributes, INIT_ATTRIBUTE));
    }


    private void checkStringList(Node item, InternalAttribute att) {
        if (att.getType().equals(STRING_LIST_TYPE) || att.getType().equals(STRINGS_TYPE)) {
            if (!item.hasChildNodes()) {
                logger.warning("Found empty StringList for attribute'"
                        + att.getName()
                        + "' in class "
                        + targetHomeItem.getClass().getName());
            } else {
                // Grab all the values of the model and save
                NodeList nodeList = item.getChildNodes();
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeName()
                            .compareToIgnoreCase("item") == 0) {
                        list.add(nodeList.item(
                                i).getTextContent());
                    }
                }
                att.setValueList(list);
            }
        }
    }

    /**
     * Parse out action object information from a DOM node. If the DOM node is not an action, null is returned.
     * Note that the actual method is not called.
     *
     * @param node DOM node to parse
     * @return InternalAction object or null
     */
    private InternalAction parseActionFromNode(Node node) {
        if (!node.getNodeName().equals(ACTION_NODE_NAME)) {
            return null;
        }
        NamedNodeMap nodeAttributes = node.getAttributes();

        return new InternalAction(getNiceAttributeValue(nodeAttributes, NAME_ATTRIBUTE),
                getNiceAttributeValue(nodeAttributes, METHOD_ATTRIBUTE));
    }

    /**
     * Parse out the value of an attribute DOM node found in the NamedNodeMap. If the map does not contain any
     * attribute with the specified name, or the attribute holds no value, an empty string is returned.
     *
     * @param map  NamedNodeMap to search for the attribute
     * @param name name of the attribute
     * @return attribute value or empty string
     */
    private String getNiceAttributeValue(NamedNodeMap map, String name) {
        String result = "";
        Node nodeAttribute = map.getNamedItem(name);
        if (null != nodeAttribute) {
            result = nodeAttribute.getNodeValue();
            if (null == result) {
                result = "";
            }
        }
        return result;
    }
}
