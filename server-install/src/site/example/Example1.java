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

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.home.item.HomeItemType;

/**
 * This is a simple example of a HomeItem. It demonstrates the model string used by the server
 * to present the GUI of a HomeItem. When an instance of this Item is presented in the GUI, it will
 * show the attributes and actions specified in the model string.
 *
 * Note that the class is annotated as a "Plugin" which makes it possible for the NetHomeServer to load it
 * dynamically. All you have to do is to pack the class in a jar and place the jar in the "plugins" folder.
 *
 * @author Stefan Strömberg
 *
 */
@Plugin
@HomeItemType("Controls")
public class Example1 extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"

            + "<HomeItem Class=\"Example1\" Category=\"Controls\" >"
            + "  <Attribute Name=\"Data\" Type=\"String\" Get=\"getData\" Set=\"setData\" Default=\"true\" />"
            + "  <Action Name=\"ToLower\" Method=\"toLower\" Default=\"true\" />"
            + "</HomeItem> ");

    private String data = "";

    @Override
    public String getModel() {
        return MODEL;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void toLower() {
        data = data.toLowerCase();
    }
}
