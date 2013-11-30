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

import nu.nethome.home.item.Attribute;

import java.util.List;

public class InternalAttribute implements Attribute {
    private String setMethodName;
    private String getMethodName;
    private String initMethodName;
    private String name = "";
    private String value = "";
    private List<String> valueList = null;
    private String type = "";
    private boolean readOnly = true;
    private boolean canInit = false;
    private boolean writeOnly = true;

    public InternalAttribute(String name1, String value1) {
        name = name1;
        value = value1;
    }

    public InternalAttribute(String name, String type, String getMethodName, String setMethodName,
                             String initMethodName) {
        this.name = name;
        this.type = type;
        this.getMethodName = getMethodName;
        this.setMethodName = setMethodName;
        this.initMethodName = initMethodName;

        // Infer data
        this.canInit = (getSetMethodName().length() != 0) || (getInitMethodName().length() != 0);
        this.readOnly = getSetMethodName().length() == 0;
        this.writeOnly = getGetMethodName().length() == 0;
        if (getInitMethodName().length() == 0) {
            this.initMethodName = getSetMethodName();
        }
    }

    public String getSetMethodName() {
        return setMethodName;
    }

    public String getGetMethodName() {
        return getMethodName;
    }

    public String getInitMethodName() {
        return (null == initMethodName) ? setMethodName : initMethodName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    public String getType() {
        return type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isCanInit() {
        return canInit;
    }

    public boolean isWriteOnly() {
        return writeOnly;
    }
}
