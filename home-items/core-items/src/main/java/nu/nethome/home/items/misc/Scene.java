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

package nu.nethome.home.items.misc;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;


/**
 * 
 * Scene
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Controls")
public class Scene extends HomeItemAdapter implements HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"Scene\"  Category=\"Controls\" >"
			+ "  <Attribute Name=\"Delay\" Type=\"String\" Get=\"getDelay\" 	Set=\"setDelay\" />"
			+ "  <Attribute Name=\"Command1\" Type=\"Command\" Get=\"getCommand1\" 	Set=\"setCommand1\" />"
			+ "  <Attribute Name=\"Command2\" Type=\"Command\" Get=\"getCommand2\" 	Set=\"setCommand2\" />"
			+ "  <Attribute Name=\"Command3\" Type=\"Command\" Get=\"getCommand3\" 	Set=\"setCommand3\" />"
			+ "  <Attribute Name=\"Command4\" Type=\"Command\" Get=\"getCommand4\" 	Set=\"setCommand4\" />"
			+ "  <Attribute Name=\"Command5\" Type=\"Command\" Get=\"getCommand5\" 	Set=\"setCommand5\" />"
			+ "  <Attribute Name=\"Command6\" Type=\"Command\" Get=\"getCommand6\" 	Set=\"setCommand6\" />"
			+ "  <Action Name=\"Action\" Method=\"action\" Default=\"true\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(Scene.class.getName());
	protected CommandLineExecutor m_Executor;
	

	// Public attributes
	protected long m_Delay = 0; // Delay in ms
	protected String m_Command1 = "";
	protected String m_Command2 = "";
	protected String m_Command3 = "";
	protected String m_Command4 = "";
	protected String m_Command5 = "";
	protected String m_Command6 = "";

	public Scene() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	/* Activate the instance
	 * @see ssg.home.HomeItem#activate()
	 */
	public void activate(HomeService server) {
        super.activate(server);
        m_Executor = new CommandLineExecutor(server, true);
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
	}
	
	public void action() {
		try {
			performCommand(m_Command1);
			if (m_Command2.length() != 0) Thread.sleep(m_Delay);
			performCommand(m_Command2);
			if (m_Command3.length() != 0) Thread.sleep(m_Delay);
			performCommand(m_Command3);
			if (m_Command4.length() != 0) Thread.sleep(m_Delay);
			performCommand(m_Command4);
			if (m_Command5.length() != 0) Thread.sleep(m_Delay);
			performCommand(m_Command5);
			if (m_Command6.length() != 0) Thread.sleep(m_Delay);
			performCommand(m_Command6);
		}
		catch (InterruptedException i) {}
	}
	
	protected void performCommand(String commandString) {
		String result = m_Executor.executeCommandLine(commandString);
		if (!result.startsWith("ok")) {
			logger.warning(result);
		}
	}
	  
	/**
	 * @return Returns the m_Delay.
	 */
	public String getDelay() {
		return Double.toString(m_Delay / 1000.0);
	}
	/**
	 * @param Delay The m_Delay to set.
	 */
	public void setDelay(String Delay) {
		m_Delay = Math.round(Double.parseDouble(Delay) * 1000);
	}

	/**
	 * @return Returns the m_Command2.
	 */
	public String getCommand2() {
		return m_Command2;
	}
	/**
	 * @param Command2 The m_Command2 to set.
	 */
	public void setCommand2(String Command2) {
		m_Command2 = Command2;
	}
	/**
	 * @return Returns the m_Command1.
	 */
	public String getCommand1() {
		return m_Command1;
	}
	/**
	 * @param Command1 The m_Command1 to set.
	 */
	public void setCommand1(String Command1) {
		m_Command1 = Command1;
	}	
	/**
	 * @return Returns the m_Command3.
	 */
	public String getCommand3() {
		return m_Command3;
	}
	/**
	 * @param Command3 The m_Command3 to set.
	 */
	public void setCommand3(String Command3) {
		m_Command3 = Command3;
	}	
	/**
	 * @return Returns the m_Command4.
	 */
	public String getCommand4() {
		return m_Command4;
	}
	/**
	 * @param Command4 The m_Command4 to set.
	 */
	public void setCommand4(String Command4) {
		m_Command4 = Command4;
	}	
	public String getCommand5() {
		return m_Command5;
	}
	public void setCommand5(String Command5) {
		m_Command5 = Command5;
	}
	public String getCommand6() {
		return m_Command6;
	}
	public void setCommand6(String Command6) {
		m_Command6 = Command6;
	}
}
