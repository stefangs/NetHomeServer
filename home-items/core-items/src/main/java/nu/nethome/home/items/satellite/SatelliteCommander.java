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

package nu.nethome.home.items.satellite;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * 
 * SatelliteCommander
 * 
 * @author Peter Lagerhem 2010-03-15
 */
public class SatelliteCommander extends HomeItemAdapter implements HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"SatelliteCommander\"  Category=\"Ports\" >"
			+ "  <Attribute Name=\"Satellite Service URL\" Type=\"String\" Get=\"getServiceUrl\" 	Set=\"setServiceUrl\" />"
			// +
			// "  <Attribute Name=\"Username\" Type=\"String\" Get=\"getUsername\" 	Set=\"setUsername\" />"
			+ "  <Attribute Name=\"Password\" Type=\"String\" Get=\"getPassword\" 	Set=\"setPassword\" />"
			+ "  <Attribute Name=\"Key\" Type=\"String\" Get=\"getKey\" 	Set=\"setKey\" />"
			+ "  <Attribute Name=\"HomeItems\" Type=\"Items\" Get=\"getHomeItems\" 	Set=\"setHomeItems\" />"
			+ "  <Attribute Name=\"Logfile\" Type=\"String\" Get=\"getLogfile\" 	Set=\"setLogfile\" />"
			// +
			// "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" 	Set=\"setLastUpdate\" />"
			+ "  <Attribute Name=\"TickInterval in seconds\" Type=\"String\" Get=\"getTickInterval\" 	Set=\"setTickInterval\" />"
			+ "  <Attribute Name=\"Active\" Type=\"String\" Get=\"getActive\" Init=\"setActive\" Default=\"true\" />"
			+ "  <Attribute Name=\"Verified\" Type=\"String\" Get=\"getVerified\" 	/>"
			+ "  <Action Name=\"Verify Service\"	Method=\"verifyService\" />"
			+ "  <Action Name=\"Start Service\"		Method=\"startService\" />"
			+ "  <Action Name=\"Stop Service\"		Method=\"stopService\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(SatelliteCommander.class
			.getName());
	protected Timer m_TickTimer = null;
	protected String m_TimeFormat = "yyyy.MM.dd HH:mm:ss Z";

	// Public attributes
	protected String m_ServiceURL = "http://www.lagerhem/hms/service";
	protected String m_Username = "your registered username";
	protected String m_Password = "MySecret";
	protected String m_Key = "your registered key";
	protected String m_Logfile = "./satellite_commander.txt";
	protected String m_LastUpdate = "";
	protected float m_TickIntervalSecs = 10.0f;
	protected boolean m_Active = false;
	protected String m_Verified = "";
	protected String m_HomeItems = "HomeItem1,HomeItem2,etc";
	private String m_HomeItemsCopy = "";
	private CommandLineExecutor m_commandLineExecutor;
	static boolean m_AlreadyInService = false;
	private boolean m_StatusConsoleFlag = true;

	public String getHomeItems() {
		return m_HomeItems;
	}

	public void setHomeItems(String mHomeItems) {
		m_HomeItems = mHomeItems;
	}

	public SatelliteCommander() {
	}

	public String getModel() {
		return m_Model;
	}

	public void activate(HomeService server) {
        super.activate(server);
        m_commandLineExecutor = new CommandLineExecutor(server, false);
		if (m_Active) {
			m_Active = false;
			// Will set to true in method...
			startService();
		}
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		stopService();
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return m_Username;
	}

	/**
	 * @param Username
	 *            The username to set.
	 */
	public void setUsername(String Username) {
		m_Username = Username;
	}

	/**
	 * @return Returns the m_Key.
	 */
	public String getKey() {
		return m_Key;
	}

	/**
	 * @param Key
	 *            The m_Key to set.
	 */
	public void setKey(String Key) {
		m_Key = Key;
	}

	/**
	 * @return Returns the m_Password.
	 */
	public String getPassword() {
		return m_Password;
	}

	/**
	 * @param Password
	 *            The m_Password to set.
	 */
	public void setPassword(String Password) {
		m_Password = Password;
	}

	/**
	 * @return Returns the m_Logfile.
	 */
	public String getLogfile() {
		return m_Logfile;
	}

	/**
	 * @param Logfile
	 *            The m_Logfile to set.
	 */
	public void setLogfile(String Logfile) {
		m_Logfile = Logfile;
	}

	/**
	 * @return Returns the m_LastUpdate.
	 */
	public String getLastUpdate() {
		return m_LastUpdate;
	}

	/**
	 * @param LastUpdate
	 *            The m_LastUpdate to set.
	 */
	public void setLastUpdate(String LastUpdate) {
		m_LastUpdate = LastUpdate;
	}

	/**
	 * Returns if the satellite is currently active (running) or not.
	 * 
	 * @return Returns the active state, "Yes" or "No".
	 */
	public String getActive() {
		return m_Active ? "Yes" : "No";
	}

	public void setActive(String active) {
		m_Active = active.compareToIgnoreCase("yes") == 0;
	}

	public String getVerified() {
		return m_Verified;
	}

	/**
	 * Gets the URL to the Satellite Service
	 * 
	 * @return the URL
	 */
	public String getServiceUrl() {
		return m_ServiceURL;
	}

	/**
	 * Sets the URL to the Satellite Service
	 * 
	 * @param url
	 */
	public void setServiceUrl(String url) {
		m_ServiceURL = url;
	}

	/**
	 * @return Returns the m_TickIntervalSecs.
	 */
	public String getTickInterval() {
		return Float.toString(m_TickIntervalSecs);
	}

	/**
	 * @param TickInterval
	 *            The m_TickIntervalSecs to set.
	 */
	public void setTickInterval(String TickInterval) {
		try {
			float temp = Float.parseFloat(TickInterval);
			// Don't allow ZERO value
			m_TickIntervalSecs = temp > 0f ? temp : m_TickIntervalSecs;
		} catch (NumberFormatException e) {
			// DoDinada, bad input
		}
	}

	/**
	 * Gets called every 10th second (default). The postToService() method will
	 * sit in a communication link with the satellite service until it receives
	 * a command, or a disconnect. Therefore, any recursive call is not let in.
	 */
	public void tickTimerExpired() {
		while (postToService(false)) {
			// Call again while returning true
		}
	}

	public void verifyService() {
		postToService(true);
	}

	public void startService() {
		if (m_Active) {
			return;
		}

		m_HomeItemsCopy = "";
		if (m_TickIntervalSecs != 0.0f) {
			Calendar tickDate = Calendar.getInstance();
			tickDate.add(Calendar.MILLISECOND,
					(int) (1000 * m_TickIntervalSecs));
			m_TickTimer = new Timer();
			// Schedule the job at m_Time minutes interval
			m_TickTimer.schedule(new TimerTask() {
				public void run() {
					tickTimerExpired();
				}
			}, tickDate.getTime(), (int) (1000 * m_TickIntervalSecs));
		}
		// Perform the start command
		postToService(false);
		m_Active = true;
	}

	public void stopService() {
		if (!m_Active) {
			return;
		}
		m_Active = false;
		if (m_TickTimer != null) {
			m_TickTimer.cancel();
			m_TickTimer = null;
		}
	}

	/**
	 * Prints the main thermometer value display page
	 * 
	 * Send data to service
	 * 
	 * The xml-rpc data format is: (not completed)
	 * 
	 * <code>
	 * <struct>
	 *   <member>
	 *     <name>key</key>
	 *     <value>761237868123BACD512365</value>
	 *   </member>
	 *   <member>
	 *     <name>item</key>
	 *     <value>
	 *       <struct>
	 *         <member>
	 *           <name>name</name>
	 *           <value>Thermometer</value>
	 *         </member>
	 *         <member>
	 *           <name>properties</name>
	 *           <value>
	 *             <struct>
	 *               <member>
	 *                 <name>Temperature</name>
	 *                 <value>12,0 C</value>
	 *               </member>
	 *               <member>
	 *                 <name>Humidity</name>
	 *                 <value>49%</value>
	 *               </member>
	 *             </struct>
	 *           </value>
	 *         </member>
	 *       </struct>
	 *     </value>
	 *   </member>
	 * </struct>
	 * </code>
	 * 
	 * @param verifyKey
	 *            boolean to have the function verify the service connection.
	 *            Otherwise do a push operation.
	 */
	protected boolean postToService(boolean verifyKey) {
		if (m_AlreadyInService)
			return false;
		m_AlreadyInService = true;
		boolean bResult = false;
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(getServiceUrl()));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			if (verifyKey) {
				Object[] params = new Object[] { new String(getKey()) };
				HashMap result = (HashMap) client.execute(
						"verifyKey", params);
				Set<?> set = result.entrySet();
				Iterator<?> i = set.iterator();

				while (i.hasNext()) {
					Map.Entry me = (Map.Entry) i.next();
					String resultKey = (String) me.getKey();
					String resultValue = (String) me.getValue();
					if (resultKey.compareToIgnoreCase("result") == 0) {
						m_Verified = resultValue;
					}
				}
			} else {
				HashMap result;

				List<Object> params = new ArrayList<Object>();

				// A complete data block
				HashMap<String, Object> param = new HashMap<String, Object>();

				// Store the key value
				param.put("Key", getKey());

				// Do model push if it has changed, or the service was restarted
				if (m_HomeItems.compareToIgnoreCase(m_HomeItemsCopy) != 0) {
					m_HomeItemsCopy = m_HomeItems;

					System.out.print("Pushing (new/changed) MODEL to service...\n");

					String temps[] = m_HomeItems.split(",");

					// A home item consisting of properties
					List<Object> homeItems = new ArrayList<Object>();

					// The date/time of when this snap shot was taken
					SimpleDateFormat formatter = new SimpleDateFormat(
							m_TimeFormat);
					Date currentTime = new Date();
					String dateString = formatter.format(currentTime);

					// Loop through all homeitem names
					for (int i = 0; i < temps.length; i++) {

						// Find the corresponding homeitem
						HomeItemProxy item = server.openInstance(temps[i]);

						// Skip if we cannot open it
						if (item == null)
							continue;

						// Get the homeitem model by the hidden attribute name
						String stringModel = item.getAttributeValue("Model");

						// A property item
						HashMap<String, String> homeItemProp = new HashMap<String, String>();

						// A home item consisting of properties
						HashMap<String, Object> homeItem = new HashMap<String, Object>();
						homeItem.put("Name", temps[i]);
						homeItem.put("Time", dateString);
						homeItem.put("Model", stringModel);
						homeItems.add(homeItem);
					}

					param.put("Items", homeItems);
					params.add(param);

					result = (HashMap) client.execute("push_model",
							params);
					// Parse reply
					Set<?> set = result.entrySet();
					Iterator<?> i = set.iterator();

					Boolean bVerified = false;
					while (i.hasNext() && !bVerified) {
						Map.Entry me = (Map.Entry) i.next();
						String resultKey = (String) me.getKey();
						String resultValue = (String) me.getValue();
						System.out.print("xml-rpc reply: '" + resultKey
								+ "', value: '" + resultValue + "'\n");
						bVerified = (resultKey.compareToIgnoreCase("msg") == 0 && resultValue
								.compareToIgnoreCase("ok") == 0);
					}

					System.out.print("Model push was "+(bVerified?"accepted":"rejected")+"\n");

					bResult = true;
				} else {
					// Go wait for a remote command
					params.add(param);
					if(m_StatusConsoleFlag)
						System.out.print("COMMAND-CHECK: Any command available at the Satellite Service, at "+getServiceUrl()+"?\n");
					result = (HashMap) client.execute(
							"wait_for_command", params);
					// Parse reply
					Set<?> set = result.entrySet();
					Iterator<?> i = set.iterator();

					Boolean bVerified;
					String code = null, cmd = null, cmd_id = null;
					while (i.hasNext()) {
						Map.Entry me = (Map.Entry) i.next();
						String resultKey = (String) me.getKey();
						String resultValue = (String) me.getValue();
//						System.out.print("xml-rpc reply: '" + resultKey
//								+ "', value: '" + resultValue + "'\n");

						if (resultKey.compareToIgnoreCase("code") == 0)
							code = resultValue;
						if (resultKey.compareToIgnoreCase("cmd") == 0)
							cmd = resultValue;
						if (resultKey.compareToIgnoreCase("id") == 0)
							cmd_id = resultValue;
					}

					if (code != null && code.compareToIgnoreCase("0") == 0 && cmd != null
							&& cmd_id != null) {
						m_StatusConsoleFlag = true;
						System.out.print("Perform command: '" + cmd + "'\n");
						String reply = m_commandLineExecutor
								.executeCommandLine(cmd);
						System.out.print("  Got reply: '" + reply + "'\n");

						// Send back reply
						param.put("cmd-id", cmd_id);
						param.put("reply", reply);
						result = (HashMap) client.execute(
								"got_command_reply", params);
						// Parse reply
						Set<?> set2 = result.entrySet();
						Iterator<?> i2 = set2.iterator();
						String msg = null;
						while (i2.hasNext()) {
							Map.Entry me = (Map.Entry) i2.next();
							String resultKey = (String) me.getKey();
							String resultValue = (String) me.getValue();
//							System.out.print("xml-rpc reply: '" + resultKey
//									+ "', value: '" + resultValue + "'\n");

							if (resultKey.compareToIgnoreCase("code") == 0)
								code = resultValue;
							if (resultKey.compareToIgnoreCase("msg") == 0)
								msg = resultValue;
						}
						if (code != null && code.compareToIgnoreCase("0") == 0 && msg != null) {
							System.out.print("  Got acknowledged service reply: '" + msg + "' with code:  "+ code + "\n");
							bResult = true;							
						} else {
							System.out.print("Unknown reply of ack command\n");							
						}
					} else {
						if(m_StatusConsoleFlag)
							System.out.print("COMMAND-CHECK: No command found this time. Will update this console upon next command found.\n");
						m_StatusConsoleFlag = false;
					}

				}
			}

			//				
			// Result is back!
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.print("Got MalformedURLException\n");
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			System.out.print("Got XmlRpcException\n");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.print("Got Exception\n");
			e.printStackTrace();
		}

		m_AlreadyInService = false;
		
		return bResult;
	}

	// TODO! Move to common-place, or retrieve from "TempWEB".
	private static final float WIND_DIR_CONV_CACTOR = (float) 22.5;
	final static String s_WindDirMap[] = { "   N", " NNE", "  NE", " ENE",
			"   E", " ESE", "  SE", " SSE", "   S", " SSW", "  SW", " WSW",
			"   W", " WNW", "  NW", " NNW", };

	/**
	 * Convert wind direction integer to source "display"-format where the wind
	 * direction are replaced with simulated LCD-images.
	 * 
	 * @param source
	 *            Integer, 0 - 337,5 that represent a wind direction.
	 * @return reformatted string
	 */
	protected String convertWindDir(float source) {
		String SubstStr = " ";
		float index = source / WIND_DIR_CONV_CACTOR;

		if (index < s_WindDirMap.length) {
			SubstStr = s_WindDirMap[Math.round(index)];
		}
		return SubstStr;
	}
}
