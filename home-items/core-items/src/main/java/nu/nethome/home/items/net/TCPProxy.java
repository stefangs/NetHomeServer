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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
public class TCPProxy extends HomeItemAdapter implements Runnable, HomeItem {

    private class ProxyThread extends Thread {
		private Socket inSock = null;
		private Socket outSock = null;
		private boolean closingDown = false;
		private ProxyThread peer = null;
		private static final int BUFFER_SIZE = 4096;

		public ProxyThread(Socket from, Socket to) {
			super("TCPProxy");
			inSock = from;
			outSock = to;
		}
		
		public void setPeer(ProxyThread proxy) {
			peer = proxy;
		}
		
		public void run() {
		    byte[] buffer = new byte[BUFFER_SIZE];
		    int count;
		    try {
		    	// Connect streams to the sockets
		    	InputStream in = inSock.getInputStream();
		    	OutputStream out = outSock.getOutputStream();
		    	try {
		    		// Here is the actual work loop. Read data from the in  socket and
		    		// write it to the out socket.
		    		while(((count = in.read(buffer)) > 0) && !closingDown) {
		    			out.write(buffer,0,count);
		    			synchronized(lock) {
		    				totalBytes += count;
		    			}
		    		}
		    	} catch(Exception xc) {
		    		// Do dinada - we were interrupted, just quit
			    } finally {
			    	// The input and output streams will be closed when the sockets themselves
			    	// are closed.
			    	out.flush();
			    }
		    } catch(Exception xc) {
		    	logger.warning("Proxy thread failed copying data");
		    }
		    synchronized(lock) {
		    	try {
		    		if (!closingDown) {
		    			// If we are not forced to do this, we have to close our in-socket and contact our peer
		    			peer.closeConnection();
		    			inSock.close();
		    		}
		    	} catch(Exception xc) {
			    	logger.warning("Proxy thread failed closing connection");
		    	} finally {
		    		proxyThreads.remove(this);
		    	}
		    }
		}
		
		public void closeConnection() {
		    synchronized(lock) {
		    	closingDown = true;
		    	try {
		    		inSock.close();
		    	} catch(Exception xc) {
		    		logger.warning("Proxy thread failed closing connection");
		    	}
		    }
		}
	}

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"TCPProxy\" Category=\"Ports\" >"
			+ "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Set=\"setListenPort\" />"
			+ "  <Attribute Name=\"TargetAddress\" Type=\"String\" Get=\"getTargetAddress\" Set=\"setTargetAddress\" />"
			+ "  <Attribute Name=\"TargetPort\" Type=\"String\" Get=\"getTargetPort\" Set=\"setTargetPort\" />"
			+ "  <Attribute Name=\"TotalBytes\" Type=\"String\" Get=\"getTotalBytes\" />"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
			+ "  <Action Name=\"OpenProxy\" Method=\"openProxy\" Default=\"true\" />"
			+ "  <Action Name=\"CloseProxy\" Method=\"closeProxy\" />"
			+ "</HomeItem> "); 

    public static final int LINGER_TIME = 180;

	/*
	 * Externally visible attributes
	 */ 
	private int listenPort = 8021;
	private String targetAddress = "ssgserver2";
	private int targetPort = 5900;
	private boolean isActive = false;
	private int totalBytes = 0;

	/*
	 * Internal attributes
	 */ 
    private static Logger logger = Logger.getLogger(TCPProxy.class.getName());
	private Thread listenThread;
	private boolean isRunning = false;
	private ServerSocket serverSocket = null;
	private List<ProxyThread> proxyThreads = new LinkedList<ProxyThread>();
    private final Object lock = new Object();

	public TCPProxy() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return MODEL;
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
	 */
	public boolean receiveEvent(Event event) {
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("OpenProxy")){
			openProxy();
            return true;
		}
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("CloseProxy")){
			closeProxy();
            return true;
		}
		return false;
	}

	public void activate() {
		if (isActive) {
			openProxy();
		}
		isRunning = true;
	}
	
	public void openProxy() {
		if (isActive && isRunning) return;
		listenThread = new Thread(this, "TCPProxyListenThread");
		isActive = true;
		listenThread.start();
	}
	
	public void closeProxy() {
		if (!isActive) return;
		isActive = false;
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} 
		catch (IOException e){
			logger.warning(e.toString());
		}
		synchronized(lock) {
            for (ProxyThread p : proxyThreads) {
                p.closeConnection();
            }
		}
	}
	

	public void stop() {
		isRunning = false;
		closeProxy();
        super.stop();
	}

	/*
	 * Internal implementation methods
	 */ 
	
	/**
	 * @return Returns the listenPort.
	 */
	public String getListenPort() {
		return String.valueOf(listenPort);
	}

	public void setListenPort(String lp) {
		listenPort = Integer.parseInt(lp);
	}

	/* 
	 * Start the thread that listens for new incoming connection
	 */
	public void run() {
		try {
			// Create the initial server socket
	        serverSocket = new ServerSocket(listenPort);
			while (isActive && !listenThread.isInterrupted()) {
		      try {
		    	  // Listen for new incoming connections
		        Socket inSocket = serverSocket.accept();
		        logger.info("TCPProxy " + name + " accepted connection from " + inSocket.getRemoteSocketAddress().toString());
		        
		        // Create and connect the client socket
			    Socket outSocket=new Socket(targetAddress, targetPort);
                  outSocket.setSoLinger(true, LINGER_TIME);
		        logger.fine("TCPProxy " + name + " redirected to " + outSocket.getRemoteSocketAddress().toString());
			    
			    // Create the proxy threads which will copy the actual data
			    ProxyThread server2Client = new ProxyThread(inSocket,outSocket);
			    ProxyThread client2Server = new ProxyThread(outSocket,inSocket);
			    server2Client.setPeer(client2Server);
			    client2Server.setPeer(server2Client);
			    synchronized(lock) {
			    	proxyThreads.add(client2Server);
			    	proxyThreads.add(server2Client);
			    	server2Client.start();
			    	client2Server.start();
			    }
		      } catch (Exception e) {
		      	if (isRunning && isActive){
		      		logger.warning("Failed reading from socket in TCPProxy " + e);
		      	}
		      }
		    }
	      }
	    catch (Exception e) {
	    	logger.warning("Failed creating socket in TCPProxy " + e);
	    }
	}
	
	/**
	 * @return Returns the messageCount.
	 */
	public String getTotalBytes() {
		return String.valueOf(totalBytes);
	}

	/**
	 * @return the m_TargetAddress
	 */
	public String getTargetAddress() {
		return targetAddress;
	}

	/**
	 * @param targetAddress the m_TargetAddress to set
	 */
	public void setTargetAddress(String targetAddress) {
		this.targetAddress = targetAddress;
	}

	/**
	 * @return the m_TargetPort
	 */
	public String getTargetPort() {
		return Integer.toString(targetPort);
	}

	/**
	 * @param targetPort the m_TargetPort to set
	 */
	public void setTargetPort(String targetPort) {
		this.targetPort = Integer.parseInt(targetPort);
	}

	/**
	 * @return the active
	 */
	public String getState() {
		return isActive ? "Open" : "Closed";
	}
	
	public void setState(String a) {
		isActive = a.equalsIgnoreCase("Open");
	}
}


