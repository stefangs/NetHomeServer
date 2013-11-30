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


import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * HomeItem class which listens for TCP/IP-connections on the specified port and sends
 * the content of the message as an event of type <b>TCPMessage</b> and the message in the
 * <b>Value</b>-attribute.
 * @author Stefan
 */
@Plugin
public class UDPCommandPort extends HomeItemAdapter implements HomeItem, Runnable {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"UDPCommandPort\" Category=\"Ports\" >"
			+ "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Default=\"true\" />"
			+ "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
			+ "</HomeItem> "); 

	/*
	 * Externally visible attributes
	 */ 
	protected int m_ListenPort = 8005;
	protected int m_MessageCount = 0;

	/*
	 * Internal attributes
	 */ 
    private static Logger logger = Logger.getLogger(UDPCommandPort.class.getName());
	protected Thread listenThread;
	protected boolean isRunning = false;
	protected DatagramSocket socket;
    protected CommandLineExecutor executor;

	public UDPCommandPort() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server,false);
		try {
			socket = new DatagramSocket(m_ListenPort);
		}
		catch (SocketException s){
			logger.warning("Failed creating socket in UDPListener " + s + ". Not activating");
			return;
		}
		isRunning = true;
		listenThread = new Thread(this, "UDPListenThread");
		listenThread.start();
	}

	public void stop() {
		isRunning = false;
		if (socket != null) {
			socket.close();
		}
	}

	/*
	 * Internal implementation methods
	 */ 
	
	/**
	 * @return Returns the listenPort.
	 */
	public String getListenPort() {
		return String.valueOf(m_ListenPort);
	}
	
	public void run() {
		while (isRunning) {
			try {
				// Create receive datagram
				byte data[] = new byte[1024];
				DatagramPacket datagram = new DatagramPacket(data, 1024);
				// Wait for a new datagram
				socket.receive(datagram);
				// Get the data as a string
				String inString = new String(datagram.getData());
				inString = inString.substring(0, datagram.getLength());
				logger.fine("Received: " + inString + " from " + datagram.getSocketAddress().toString());
				inString = inString.trim();
				// Execute the command
				String result = executor.executeCommandLine(inString);
				// Create a result datagram
				byte resultData[] = result.getBytes();
				DatagramPacket resultPacket = new DatagramPacket(resultData, resultData.length);
				resultPacket.setAddress(datagram.getAddress());
				resultPacket.setPort(datagram.getPort());
				// Send result
				socket.send(resultPacket);
				m_MessageCount++;
			}
			catch (Exception e) {
				if (isRunning){
					logger.warning("Failed reading from socket in UDPListener " + e);
				}
			}
		}
	}
	
	/**
	 * @return Returns the messageCount.
	 */
	public String getMessageCount() {
		return String.valueOf(m_MessageCount);
	}	
}
