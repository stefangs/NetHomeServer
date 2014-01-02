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
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * 
 * GenericProntoCommander is a generic Pronto code sender. It reads a configuration file with
 * names of operations and corresponding pronto codes, and presents an action for each
 * operation in the file. When an action is called, the corresponding Pronto code is sent
 * as an internal event.
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Controls")
public class GenericProntoCommander extends HomeItemAdapter implements HomeItem {
	
	private static final int MAX_ACTIONS = 20;

	/**
	 * Used to cache command names and corresponding Pronto string
	 * @author Stefan
	 */
	private class ProntoCommand {
		private String m_CommandName;
		private String m_ProntoString1;
		
		public ProntoCommand(String commandName, String prontoString) {
			super();
			m_CommandName = commandName;
			m_ProntoString1 = prontoString;
		}
		public String getCommandName() {
			return m_CommandName;
		}
		public void setCommandName(String commandName) {
			m_CommandName = commandName;
		}
		public String getProntoString() {
			return m_ProntoString1;
		}
		public void setProntoString1(String prontoString1) {
			m_ProntoString1 = prontoString1;
		}
	}

	private final String m_ModelStart = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"GenericProntoCommander\"  Category=\"Controls\"  Morphing=\"true\" >"
			+ "  <Attribute Name=\"ConfigFile\" Type=\"String\" Get=\"getConfigFile\" 	Set=\"setConfigFile\" />"			
			+ "  <Attribute Name=\"PortName\" Type=\"String\" Get=\"getPortName\" 	Set=\"setPortName\" />"
			+ "  <Attribute Name=\"UseModulation\" Type=\"String\" Get=\"getUseModulation\" 	Set=\"setUseModulation\" />"
			+ "  <Attribute Name=\"Repeat\" Type=\"String\" Get=\"getRepeat\" 	Set=\"setRepeat\" />");

	private final String m_ModelEnd = ("</HomeItem> "); 

	private static Logger logger = Logger.getLogger(GenericProntoCommander.class.getName());
	protected ArrayList<ProntoCommand> m_ProntoCommands = new ArrayList<ProntoCommand>();

	// Public attributes
	protected String m_ConfigFile = "";
	protected String m_PortName = "";
	protected boolean m_UseModulation = true;
	protected int m_Repeat = 0;

	public GenericProntoCommander() {
	}
	
	public String getModel() {
		StringBuilder result = new StringBuilder(m_ModelStart);

		// Read all commands and add them as actions in the Item model
		int actionNumber = 0;
		for (ProntoCommand command : m_ProntoCommands) {
			if (actionNumber < MAX_ACTIONS) {
				result.append(String.format("  <Action Name=\"%s\" Method=\"action%d\" />", 
						command.getCommandName(),
						actionNumber++));
			}
		}
		
		// Add the end of the model
		result.append(m_ModelEnd);
		
		return result.toString();
	}

	/**
	 * Read the command file and cache the commands
	 */
	void readConfigFile() {
		m_ProntoCommands.clear();

		// Check if there is any configuration file set
		if (m_ConfigFile.length() > 0) {
			try{
				// Open the file
				FileInputStream fstream = new FileInputStream(m_ConfigFile);
				
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;

				//Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					String commands[] = strLine.split(",");
					if ((commands != null) && (commands.length > 1) && (commands.length < 4)) {
						ProntoCommand command= new ProntoCommand(commands[0], commands[1]);
						m_ProntoCommands.add(command);
					} else {
						logger.warning("Bad format in action file: " + m_ConfigFile + " on line: " +
								strLine);
					}
				}
				
				// Close the input stream
				in.close();
			} catch (Exception e){//Catch exception if any
				logger.warning("Failed reading action file: " + m_ConfigFile);
			}
		}
	}

	/**
	 * @return Returns the m_ConfigFile.
	 */
	public String getConfigFile() {
		return m_ConfigFile;
	}
	/**
	 * @param ConfigFile The m_ConfigFile to set.
	 */
	public void setConfigFile(String ConfigFile) {
		m_ConfigFile = ConfigFile;

		// Cache the config file
		readConfigFile();
	}

	/**
	 * @return Returns the m_UseModulation.
	 */
	public String getUseModulation() {
		return m_UseModulation ? "True" : "False";
	}
	/**
	 * @param UseModulation The m_UseModulation to set.
	 */
	public void setUseModulation(String UseModulation) {
		m_UseModulation = UseModulation.equalsIgnoreCase("True");
	}
	
	/**
	 * @return Returns the m_PortName.
	 */
	public String getPortName() {
		return m_PortName;
	}
	/**
	 * @param PortName The m_PortName to set.
	 */
	public void setPortName(String PortName) {
		m_PortName = PortName;
	}
	
	public String getRepeat() {
		return Integer.toString(m_Repeat);
	}

	public void setRepeat(String repeat) {
		m_Repeat = Integer.parseInt(repeat);
	}

	/**
	 * Perform the command with the specified number. The Pronto commands are read from the 
	 * configuration file.
	 * @param number the command number. The first command in the file is command number 0
	 */
	protected void performAction(int number) {
		if (number < m_ProntoCommands.size()) {
			Event prontoEvent = server.createEvent("Pronto_Message", "");
			prontoEvent.setAttribute("Direction", "Out");
			prontoEvent.setAttribute("Pronto.Message", m_ProntoCommands.get(number).getProntoString());
			if (m_UseModulation) {
				prontoEvent.setAttribute("Modulation", "On");
			}
			if (m_Repeat > 0) {
				prontoEvent.setAttribute("Repeat", m_Repeat);
			}
			server.send(prontoEvent);
		}
	}
	
	/**
	 * Generic action1
	 */
	public void action0() {
		performAction(0);
	}	
	public void action1() {
		performAction(1);
	}	
	public void action2() {
		performAction(2);
	}	
	public void action3() {
		performAction(3);
	}	
	public void action4() {
		performAction(4);
	}	
	public void action5() {
		performAction(5);
	}	
	public void action6() {
		performAction(6);
	}	
	public void action7() {
		performAction(7);
	}	
	public void action8() {
		performAction(8);
	}	
	public void action9() {
		performAction(9);
	}	
	public void action10() {
		performAction(10);
	}	
	public void action11() {
		performAction(11);
	}	
	public void action12() {
		performAction(12);
	}	
	public void action13() {
		performAction(13);
	}	
	public void action14() {
		performAction(14);
	}	
	public void action15() {
		performAction(15);
	}	
	public void action16() {
		performAction(16);
	}	
	public void action17() {
		performAction(17);
	}	
	public void action18() {
		performAction(18);
	}	
	public void action19() {
		performAction(19);
	}	
}

