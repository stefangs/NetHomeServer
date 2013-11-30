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

/*
 * Copyright (C) 2007 Stefan Str�mberg
 *
 * File: DebugManager.java
 * Project: HomeManager
 *
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * History:
 * 2007 apr 9	Created
 */

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * DebugManager
 *
 * @author Stefan
 */
public class DebugManager extends HomeItemAdapter implements HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"DebugManager\"  Category=\"Ports\" >"
			+ "  <Attribute Name=\"LoggerContext\" Type=\"String\" Get=\"getLoggerContext\" 	Set=\"setLoggerContext\" />"
			+ "  <Attribute Name=\"ActualLogLevel\" Type=\"String\" Get=\"getActualLogLevel\" Default=\"true\" />"
			+ "  <Attribute Name=\"WantedLogLevel\" Type=\"StringList\" Get=\"getLogLevel\" 	Set=\"setLogLevel\" >"
			+ "   <item>INFO</item> <item>FINE</item> <item>FINER</item> <item>FINEST</item>"
			+ "  </Attribute>"
			+ "  <Action Name=\"ApplyLogLevel\" 	Method=\"applyLogLevel\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(DebugManager.class.getName());
	// Public attributes
	protected String m_LoggerContext = "";
	protected String m_LogLevel = "";

	public DebugManager() {
	}

	public String getModel() {
		return m_Model;
	}

	public void activate() {
		// When we start, we apply the wanted log level for the specified context
		applyLogLevel();
	}

	/**
	 * @return Returns the m_LoggerContext.
	 */
	public String getLoggerContext() {
		return m_LoggerContext;
	}
	/**
	 * @param LoggerContext The m_LoggerContext to set.
	 */
	public void setLoggerContext(String LoggerContext) {
		m_LoggerContext = LoggerContext;
	}

	/**
	 * The will get the actual current log level for the specified context.
	 *
	 * @return Returns the m_LogLevel.
	 */
	public String getActualLogLevel() {
		Logger current = Logger.getLogger(m_LoggerContext);
		return current.getLevel().getName();
	}

	/**
	 * The will get the current set log level for the specified context.
	 *
	 * @return Returns the m_LogLevel.
	 */
	public String getLogLevel() {
		return m_LogLevel;
	}

	/**
	 * This will update the internal log level cache, but NOT actually set the
	 * LogLevel to the specified context. The context is updated with the
	 * applyLogLevel()-method. This is because we cannot be sure in what order the
	 * LoggerContext and LogLevel attributes are updated on a set.
	 *
	 * @param LogLevel The m_LogLevel to set.
	 */
	public void setLogLevel(String LogLevel) {
		m_LogLevel = LogLevel;
	}

	/**
	 * Applies current LogLevel to current LogContext
	 */
	public void applyLogLevel() {
		Logger current = Logger.getLogger(m_LoggerContext);
		try {
			Level level = Level.parse(m_LogLevel);
			current.setLevel(level);
		} catch (IllegalArgumentException e) {
			logger.warning("Tried to set illegal log level:" + m_LogLevel);
		}
	}
}
