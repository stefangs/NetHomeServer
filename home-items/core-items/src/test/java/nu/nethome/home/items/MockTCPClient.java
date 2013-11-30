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

package nu.nethome.home.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Helper class for unit tests. It acts as a TCP-Client and can send 
 * and receive text over a TCP/IP-connection
 * @author Stefan
 */
public class MockTCPClient {
	
	private Socket m_Sock;

	/**
	 * Creates the client and connects to the server.
	 * @param address TCP/IP-address to connect to
	 * @param port TCP/IP-port
	 * @throws UnknownHostException
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public MockTCPClient(String address, int port) throws UnknownHostException, SocketTimeoutException, IOException{
		 // Create a socket with a timeout
	     InetAddress addr = InetAddress.getByName(address);
	     SocketAddress sockaddr = new InetSocketAddress(addr, port);
	    
	     // Create an unbound socket
	     m_Sock = new Socket();
	    
	     // This method will block no more than timeoutMs.
	     // If the timeout occurs, SocketTimeoutException is thrown.
	     int timeoutMs = 500;   // 1/2 second
	     m_Sock.connect(sockaddr, timeoutMs);
	     m_Sock.setSoTimeout(50);
	}
	
	/**
	 * Disconnect the TCP/IP-connection. The client cannot be reused after this.
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		m_Sock.close();
	}
	
	/**
	 * Reads a line of text ended with <CR<LF> from the TCP/IP-connection.
	 * @return read text line
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(m_Sock.getInputStream()));
		String result = reader.readLine();
		System.out.println(result);
		return result;
	}

	/**
	 * Verifies that there is no data to read from the socket
	 * @return true if there was no data, false if there was data
	 * @throws IOException
	 */
	public boolean readEmptyLine() throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(m_Sock.getInputStream()));
			reader.readLine();
		} catch (SocketTimeoutException e) {
			return true;
		}
		return false;
	}

	/**
	 * Writes a line of text to the TCP/IP-connection
	 * @param line text to write
	 * @throws IOException
	 */
	public void writeLine(String line) throws IOException {
		m_Sock.getOutputStream().write(line.getBytes());
		System.out.print(line);
	}

}
