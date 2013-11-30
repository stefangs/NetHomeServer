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

package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;


/**
 * HomeItem class which listens for TCP/IP-connections on the specified port and sends
 * the content of the message as an event of type <b>TCPMessage</b> and the message in the
 * <b>Value</b>-attribute.
 * @author Stefan
 */
@Plugin
public class TCPListener extends HomeItemAdapter implements Runnable, HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"TCPListener\" Category=\"Ports\" >"
			+ "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Init=\"setListenPort\" Default=\"true\" />"
			+ "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
			+ "</HomeItem> "); 

	/*
	 * Externally visible attributes
	 */ 
	protected int m_ListenPort = 8001;
	protected int m_MessageCount = 0;

	/*
	 * Internal attributes
	 */ 
    private static Logger logger = Logger.getLogger(TCPListener.class.getName());
	protected Thread listenThread;
	protected boolean isRunning = false;
	protected ServerSocket serverSocket;
	protected Socket inSocket;

	public TCPListener() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	public void activate() {
		isRunning = true;
		listenThread = new Thread(this, "TCPListenThread");
		listenThread.start();
	}

	public void stop() {
		isRunning = false;
		try {
			if (inSocket != null) {
				inSocket.close();
			}
		} 
		catch (IOException e){
			logger.warning(e.toString());
		}
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} 
		catch (IOException e){
			logger.warning(e.toString());
		}
        super.stop();
	}
	
	/**
	 * @return Returns the listenPort.
	 */
	public String getListenPort() {
		return String.valueOf(m_ListenPort);
	}
	
	/**
	 * Set the listen port
	 * @param port
	 */
	public void setListenPort(String port) {
		m_ListenPort = Integer.parseInt(port);
	}
	
	public void run() {
		try {
	        serverSocket = new ServerSocket(m_ListenPort);
			while (isRunning) {
		      try {
		        inSocket = serverSocket.accept();
		        BufferedReader in = new BufferedReader(new InputStreamReader(inSocket.
		            getInputStream()));
		        String inString = in.readLine();
		        inSocket.close();
		        logger.fine("Received: " + inString + " from " + inSocket.getRemoteSocketAddress().toString());
		        Event event = server.createEvent("TCPMessage", inString);
		        event.setAttribute("IPAddress", inSocket.getRemoteSocketAddress().toString());
		        server.send(event);
		        m_MessageCount++;
		      }
		      catch (Exception e) {
		      	if (isRunning){
		      		logger.warning("Failed reading from socket in TCPListener " + e);
		      		isRunning = false;
		      	}
		      }
		    }
	      }
	    catch (Exception e) {
	    	logger.warning("Failed creating socket in TCPListener " + e);
	    }
	}

	/**
	 * @return Returns the messageCount.
	 */
	public String getMessageCount() {
		return String.valueOf(m_MessageCount);
	}	
}

