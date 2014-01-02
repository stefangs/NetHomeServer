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

package nu.nethome.home.items.waveman;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.nexa.NexaLamp;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
* 
* Represents a switch (typically connected to a lamp) which is controlled by 
* the Waveman RF protocol. The WavemanLamp requires a port which can send Waveman protocol
* messages as RF signals.
* 
* <br>
* TODO: UnitTest
* 
* @author Stefan
*/
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "Waveman_Message")
public class WavemanLamp extends NexaLamp implements HomeItem {

	public WavemanLamp() {
        super();
        logger = Logger.getLogger(WavemanLamp.class.getName());
	}
	
        private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
				+ "<HomeItem Class=\"WavemanLamp\" Category=\"Lamps\" >"
				+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
				+ "  <Attribute Name=\"HouseCode\" Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
				+ "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
				+ "  <Action Name=\"on\" 	Method=\"on\" />"
				+ "  <Action Name=\"off\" 	Method=\"off\" />"
				+ "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
				+ "</HomeItem> ");

    @Override
    public String getModel() {
        return MODEL;
    }

    protected String getProtocolName() {
        return "Waveman_Message";
    }

    protected String getHouseCodeName() {
        return "Waveman.HouseCode";
    }

    protected String getButtonName() {
        return "Waveman.Button";
    }

    protected String getCommandName() {
        return "Waveman.Command";
    }
}
