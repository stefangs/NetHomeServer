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
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
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
@HomeItemType("Ports")
public class UDPListener extends HomeItemAdapter implements Runnable, HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"UDPListener\" Category=\"Ports\" >"
			+ "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Init=\"setListenPort\" Default=\"true\" />"
			+ "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
			+ "</HomeItem> "); 

	/*
	 * Externally visible attributes
	 */ 
	protected int listenPort = 514;
	protected int messageCount = 0;

	/*
	 * Internal attributes
	 */ 
    private static Logger logger = Logger.getLogger(UDPListener.class.getName());
	protected Thread listenThread;
	protected boolean isRunning = false;
	protected DatagramSocket socket;
	// protected ServerSocket serverSocket;
	// protected Socket m_InSocket;

	public UDPListener() {
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	public void activate() {
		try {
			socket = new DatagramSocket(listenPort);
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
        super.stop();
	}

	/*
	 * Internal implementation methods
	 */ 
	
	/**
	 * @param listenPort the ListenPort to set
	 */
	public void setListenPort(String listenPort) {
		this.listenPort = Integer.parseInt(listenPort);
	}	
	
	/**
	 * @return Returns the listenPort.
	 */
	public String getListenPort() {
		return String.valueOf(listenPort);
	}
	
	public void run() {
		try {
			while (isRunning) {
		      try {
		    	byte data[] = new byte[1024];
		    	DatagramPacket datagram = new DatagramPacket(data, 1024);
		    	socket.receive(datagram);
		        String inString = new String(datagram.getData());
		        inString = inString.substring(0, datagram.getLength());
		        logger.fine("Received: " + inString + " from " + datagram.getSocketAddress().toString());
		        Event event = server.createEvent("UDPMessage", inString);
		        event.setAttribute(Event.EVENT_SENDER_ATTRIBUTE, name);
		        event.setAttribute("IPAddress", datagram.getSocketAddress().toString());
		        server.send(event);
		        messageCount++;
		      }
		      catch (Exception e) {
		      	if (isRunning){
		      		logger.warning("Failed reading from socket in UDPListener " + e);
		      	}
		      }
		    }
	      }
	    catch (Exception e) {
	    	logger.warning("Failed creating socket in UDPListener " + e);
	    }
	}

	/**
	 * @return Returns the messageCount.
	 */
	public String getMessageCount() {
		return String.valueOf(messageCount);
	}


}

