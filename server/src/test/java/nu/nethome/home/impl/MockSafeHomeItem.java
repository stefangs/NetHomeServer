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

import nu.nethome.home.item.HomeItem;

public class MockSafeHomeItem extends MockHomeItem implements HomeItem {

    private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MockSafeHomeItem\" Category=\"Lamps\"  Morphing=\"true\" >"
            + "  <Attribute Name=\"AttValueFoo\" Type=\"String\" Get=\"getAttValueFoo\" Set=\"setAttValueFoo\"/>"
            + "  <Attribute Name=\"AttSet\" Type=\"String\" Get=\"getAttSet\" 	Set=\"setAttSet\" />"
            + "  <Action Name=\"ReturnFoo\" 	Method=\"returnFoo\"/>"
            + "  <Action Name=\"IsDefault\" 	Method=\"isDefault\"  Default=\"true\"/>"
            + "  <Action Name=\"Extra\" 	Method=\"isDefault\"  />"
            + "  <Attribute Name=\"AttInit\" Type=\"String\" Get=\"getAttInit\" 	Init=\"setAttInit\" />"
            + "  <Attribute Name=\"AttReadOnly\" Type=\"String\" Get=\"getAttReadOnly\" />"
            + "  <Attribute Name=\"AttList\" Type=\"StringList\" Get=\"getAttList\" 	Set=\"setAttList\">"
            + "     <item>Foo</item> <item>Fie</item> <item>Fum</item> </Attribute>"
            + "</HomeItem> ");

    private String attList = "";
    public String modelAddition = "";

    @Override
    public String getModel() {
        return m_Model + modelAddition;
    }

    public String isDefault() {
        m_CalledMethods.add("isDefault");
        return "Foo";
    }

    public String getAttList() {
        return attList;
    }

    public void setAttList(String attList) {
        this.attList = attList;
    }
}

