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

/*
 * SatelliteLogger
 * 
 * @author Peter Lagerhem 2010-03-15
 * 
 * History:
 * 2010-10-31 pela sets encoding to UTF-8 in postToService method and changed the name of the ThermometerList attribute to HomeItems
 * 2010-10-31 pela #2: added batterystatus and state
 */
package nu.nethome.home.items.satellite;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.util.plugin.Plugin;
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
 * SatelliteLogger
 * 
 */
@Plugin
public class SatelliteLogger extends HomeItemAdapter implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\" encoding=\"UTF-8\"?> \n"
			+ "<HomeItem Class=\"SatelliteLogger\"  Category=\"Ports\" >"
			+ "  <Attribute Name=\"SatelliteServiceURL\" Type=\"String\" Get=\"getServiceUrl\" 	Set=\"setServiceUrl\" />"
//			+ "  <Attribute Name=\"Username\" Type=\"String\" Get=\"getUsername\" 	Set=\"setUsername\" />"
//			+ "  <Attribute Name=\"Password\" Type=\"String\" Get=\"getPassword\" 	Set=\"setPassword\" />"
			+ "  <Attribute Name=\"Key\" Type=\"String\" Get=\"getKey\" 	Set=\"setKey\" />"
            + "  <Attribute Name=\"HomeItems\" Type=\"Items\" Get=\"getHomeItemList\" Set=\"setHomeItemList\" />"
			+ "  <Attribute Name=\"TickInterval in minutes\" Type=\"String\" Get=\"getTickInterval\" 	Set=\"setTickInterval\" />"
			+ "  <Attribute Name=\"Active\" Type=\"String\" Get=\"getActive\" Init=\"setActive\" Default=\"true\" />"
			+ "  <Attribute Name=\"Verified\" Type=\"String\" Get=\"getVerified\" 	/>"
			+ "  <Action Name=\"Verify Service\"	Method=\"verifyService\" />"
			+ "  <Action Name=\"Start Service\"		Method=\"startService\" />"
			+ "  <Action Name=\"Stop Service\"		Method=\"stopService\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(SatelliteLogger.class
			.getName());
	protected Timer m_TickTimer = null;
	protected String m_TimeFormat = "yyyy.MM.dd HH:mm:ss Z";

	// Public attributes
	protected String m_ServiceURL = "http://www.lagerhem/hms/service";
	protected String m_Username = "your registered username";
	protected String m_Password = "your registered password";
	protected String m_Key = "your registered key";
	protected String m_Logfile = "./satellite_logger.txt";
	protected String m_LastUpdate = "";
	protected float m_TickIntervalMin = 15.0f;
	protected boolean m_Active = false;
	protected String m_Verified = "";
	protected String m_HomeItemList = "";

	public SatelliteLogger() {
	}

	public String getModel() {
        return MODEL;
	}

	public void activate() {
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
		return Float.toString(m_TickIntervalMin);
	}

	/**
	 * @param TickInterval
	 *            The m_TickIntervalSecs to set.
	 */
	public void setTickInterval(String TickInterval) {
		try {
			float temp = Float.parseFloat(TickInterval);
			m_TickIntervalMin = temp >= 0f ? temp : m_TickIntervalMin;
		} catch (NumberFormatException e) {
			// DoDinada, bad input
		}
	}

	/**
	 * @return Returns the m_HomeItemList.
	 */
	public String getHomeItemList() {
		return m_HomeItemList;
	}

	/**
	 * @param homeItemList
	 *            The m_HomeItemList to set.
	 */
	public void setHomeItemList(String homeItemList) {
		m_HomeItemList = homeItemList;
	}

	public void tickTimerExpired() {
		postToService(false);
	}

	public void verifyService() {
		postToService(true);
	}

	public void startService() {
		if (m_Active) {
			return;
		}

		if (m_TickIntervalMin != 0.0f) {
			Calendar tickDate = Calendar.getInstance();
			tickDate.add(Calendar.MILLISECOND,
					(int) (1000 * 60 * m_TickIntervalMin));
			m_TickTimer = new Timer();
			// Schedule the job at m_Time minutes interval
			m_TickTimer.schedule(new TimerTask() {
				public void run() {
					tickTimerExpired();
				}
			}, tickDate.getTime(), (int) (1000 * 60 * m_TickIntervalMin));
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
	protected void postToService(boolean verifyKey) {
		System.out
				.print("SATELLITELOGGER: Contacting the Satellite Service, at "
						+ getServiceUrl() + "\n");
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(getServiceUrl()));
			config.setEncoding("UTF-8");
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			if (verifyKey) {
				Object[] params = new Object[] { new String(getKey()) };
				HashMap result = (HashMap) client.execute(
						"verifyKey", params);
				Set<?> set = result.entrySet();
				Iterator<?> i = set.iterator();

				Boolean bVerified;
				while (i.hasNext()) {
					Map.Entry me = (Map.Entry) i.next();
					String resultKey = (String) me.getKey();
					String resultValue = (String) me.getValue();
					if (resultKey.compareToIgnoreCase("result") == 0) {
						m_Verified = resultValue;
					}
				}
			} else {
				String temps[] = m_HomeItemList.split(",");
				// List<HashMap<String, String>> params = new
				// ArrayList<HashMap<String,String>>();

				// A complete data block
				List<Object> params = new ArrayList<Object>();
				HashMap<String, Object> param = new HashMap<String, Object>();

				// A home item consisting of properties
				List<Object> homeItems = new ArrayList<Object>();

				// The date/time of when this snap shot was taken
				SimpleDateFormat formatter = new SimpleDateFormat(m_TimeFormat);
				Date currentTime = new Date();
				String dateString = formatter.format(currentTime);

				// Loop through all homeitem names
				for (int i = 0; i < temps.length; i++) {

					// Find the corresponding homeitem
					HomeItemProxy item = server.openInstance(temps[i]);

					// Skip if we cannot open it
					if (item == null)
						continue;

					// A property item
					HashMap<String, String> homeItemProp = new HashMap<String, String>();

					// A home item consisting of properties
					HashMap<String, Object> homeItem = new HashMap<String, Object>();

					// Get the value(s)
					String stringLastUpdate = item.getAttributeValue(
							"LastUpdate").replace(',', '.');
					String stringTemp = item.getAttributeValue("Temperature")
							.replace(',', '.');
					String stringHum = item.getAttributeValue("Humidity")
							.replace(',', '.');
					String stringWindSpeed = item
							.getAttributeValue("WindSpeed").replace(',', '.');
					String stringWindDir = item.getAttributeValue(
							"WindDirection").replace(',', '.');
					String stringRain = item.getAttributeValue("Rainfall")
							.replace(',', '.');
					String stringBatteryStatus = item
							.getAttributeValue("BatteryStatus");
					String stringState = item
							.getAttributeValue("State");

					homeItem.put("Name", temps[i]);
					homeItem.put("Time", dateString);

					// The lastupdate value
					if (stringLastUpdate.length() != 0) {
						// homeItemProp.put("LastUpdate", stringLastUpdate);
					}
					// A thermometer's temperature value
					if (stringTemp.length() != 0) {
						float temp = Float.parseFloat(stringTemp);
						// homeItem.put("Channel", "CH"+Integer.toString(i +
						// 1));
						homeItemProp.put("Temperature",
								String.format("% 5.1fC", temp));
					}
					// Check valid humidity value
					if (stringHum.length() != 0) {
						float hum = Float.parseFloat(stringHum);
						homeItemProp.put("Humidity",
								String.format("% 5.1fP", hum));
						// p.println("CH" + Integer.toString(i + 1) + " " + );
					}

					// Check valid rainfall value
					if (stringRain.length() != 0) {
						float rainF = Float.parseFloat(stringRain);
						homeItemProp.put("Rainfall",
								String.format("% 5.1fR", rainF));
					}

					// Check valid speed value
					if (stringWindSpeed.length() != 0) {
						float windS = Float.parseFloat(stringWindSpeed);
						homeItemProp.put("WindSpeed",
								String.format("% 5.1fV", windS));
					}

					// Check valid direction value
					if (stringWindDir.length() != 0) {
						float windD = Float.parseFloat(stringWindDir);
						homeItemProp
								.put("WindDirection", convertWindDir(windD));
					}

					if (stringBatteryStatus.length() != 0) {
						homeItemProp.put("Battery Status", stringBatteryStatus);
					}
					if (stringState.length() != 0) {
						homeItemProp.put("State", stringState);
					}

					homeItem.put("Properties", homeItemProp);
					homeItems.add(homeItem);
				}

				// Store the key value
				param.put("Key", getKey());
				param.put("Items", homeItems);
				params.add(param);

				HashMap result = (HashMap) client.execute(
						"push", params);
				Set<?> set = result.entrySet();
				Iterator<?> i = set.iterator();

				Boolean bVerified;
				while (i.hasNext()) {
					Map.Entry me = (Map.Entry) i.next();
					String resultKey = (String) me.getKey();
					String resultValue = (String) me.getValue();
					System.out.print("xml-rpc reply: '" + resultKey
							+ "', value: '" + resultValue + "'\n");
					bVerified = (resultKey.compareToIgnoreCase("result") == 0 && resultValue
							.compareToIgnoreCase("true") == 0);
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
