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

package nu.nethome.home.items.pronto;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * 
 * @author Stefan
 */
@Plugin
public class ProntoLamp extends HomeItemAdapter implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ProntoLamp\" Category=\"Lamps\" >"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
			+ "  <Attribute Name=\"OnCode\" Type=\"String\" Get=\"getOnCode\" 	Set=\"setOnCode\" />"
			+ "  <Attribute Name=\"OffCode\" Type=\"String\" Get=\"getOffCode\" 	Set=\"setOffCode\" />"
			+ "  <Attribute Name=\"Repeat\" Type=\"String\" Get=\"getRepeat\" 	Set=\"setRepeat\" />"
			+ "  <Attribute Name=\"UseModulation\" Type=\"String\" Get=\"getUseModulation\" 	Set=\"setUseModulation\" />"
			+ "  <Action Name=\"on\" 	Method=\"on\" />"
			+ "  <Action Name=\"off\" 	Method=\"off\" />"
			+ "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(ProntoLamp.class.getName());

    protected boolean itemState = false;
    protected String onCode = "";
    protected String offCode = "";
    private int repeat = 5;
    private boolean useModulation = true;

	public ProntoLamp() {
	}

	public String getModel() {
		return MODEL;
	}

	/**
	 * @return Returns the State.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getState() {
		if (itemState) {
			return "On";
		}
		return "Off";
	}

	/**
	 * @return Returns the OffCode.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getOffCode() {
		return offCode;
	}
	
	/**
	 * @param OffCode The OffCode to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setOffCode(String OffCode) {
		offCode = OffCode;
	}
	
	/**
	 * @return Returns the OnCode.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getOnCode() {
		return onCode;
	}
	/**
	 * @param OnCode The OnCode to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setOnCode(String OnCode) {
		onCode = OnCode;
	}
	
	@SuppressWarnings("UnusedDeclaration")
    public String getRepeat() {
		return Integer.toString(repeat);
	}

	@SuppressWarnings("UnusedDeclaration")
    public void setRepeat(String repeat) {
		this.repeat = Integer.parseInt(repeat);
	}

	public void sendCommand(String command) {
		Event ev = server.createEvent("Pronto_Message", "");
		ev.setAttribute("Direction", "Out");
		if (useModulation) {
			ev.setAttribute("Modulation", "On");
		}
		ev.setAttribute("Pronto.Message",command);
		ev.setAttribute("Repeat", repeat);
		server.send(ev);
	}

	public void on() {
		logger.fine("Switching on " + name);
		sendCommand(onCode);
		itemState = true;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void off() {
		logger.fine("Switching off " + name);
		sendCommand(offCode);
		itemState = false;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void toggle() {
		logger.fine("Toggling " + name);
		itemState = !itemState;
		sendCommand(itemState ? onCode : offCode);
	}
	
	/**
	 * @return Returns the m_UseModulation.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getUseModulation() {
		return useModulation ? "True" : "False";
	}
	/**
	 * @param UseModulation The m_UseModulation to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setUseModulation(String UseModulation) {
		useModulation = UseModulation.equalsIgnoreCase("True");
	}
}
