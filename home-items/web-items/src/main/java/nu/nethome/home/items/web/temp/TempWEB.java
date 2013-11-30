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

package nu.nethome.home.items.web.temp;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.web.HomeWebServer;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * WEB GUI Item which presents thermometer values. The GUI emulates the look of
 * an UPM-thermometer with display windows. It can display the values from multiple
 * thermometers. The thermometers may be any item which has an attribute called "Temperature".
 * <p/>
 * For the thermometers which have an attribute called "LogFile" a graph page can
 * be presented, where graphs of the log file is presented.
 *
 * @author Stefan Str�mberg
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
public class TempWEB extends HttpServlet implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"TempWEB\" Category=\"GUI\" >"
            + "  <Attribute Name=\"WEBServer\" Type=\"Item\" Get=\"getWEBServer\" 	Set=\"setWEBServer\" />"
            + "  <Attribute Name=\"LocalURL\" Type=\"String\" Get=\"getLocalURL\" 	Set=\"setLocalURL\" Default=\"true\" />"
            + "  <Attribute Name=\"ThermometerList\" Type=\"Items\" Get=\"getThermometerList\" 	Set=\"setThermometerList\" />"
            + "</HomeItem> ");

    private static final long HOURS24 = 24L * 60L * 60L * 1000L;
    private static final long WEEK = 7L * 24L * 60L * 60L * 1000L;
    private static final long MONTH = 31L * 24L * 60L * 60L * 1000L;
    private static final long YEAR = 365L * 24L * 60L * 60L * 1000L;
    private static final float WIND_DIR_CONV_CACTOR = (float) 22.5;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    protected String itemName = "NoNameYet";
    protected long itemId = 0L;
    protected HomeService server;
    protected HashMap<Character, String> characterMap = new HashMap<Character, String>();

    // Public attributes
    protected String webServer = "WEB-Server";
    protected String localURL = "/temp";
    protected String thermometerList = "Thermometer";

    public TempWEB() {
        for (int i = 0; i < s_CharMap.length; i += 2) {
            characterMap.put(s_CharMap[i].charAt(0), s_CharMap[i + 1]);
        }
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return MODEL;
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getName()
      */
    public String getName() {
        return itemName;
    }

    /* (non-Javadoc)
      * @see nu.nethome.home.HomeItem#getID()
      */
    public long getItemId() {
        return itemId;
    }

    /* (non-Javadoc)
      * @see nu.nethome.home.HomeItem#setID(long)
      */
    public void setItemId(long id) {
        itemId = id;
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#setName(java.lang.String)
      */
    public void setName(String name) {
        itemName = name;
    }

    public boolean receiveEvent(Event event) {
        return false;
    }


    public void activate(HomeService homeServer) {
        server = homeServer;
        // On activation we register this WEB-Component at the specified WEB-Server
        HomeItemProxy webServerItem = server.openInstance(webServer);
        if (webServerItem != null) {
            Object possibleWebServer = webServerItem.getInternalRepresentation();
            if (possibleWebServer != null && possibleWebServer instanceof HomeWebServer) {
                ((HomeWebServer) possibleWebServer).registerServlet(localURL, this);
            }
        }
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        ServletOutputStream p = res.getOutputStream();

        // Analyze arguments
        boolean isPopup = false;
        String action = req.getParameter("a");
        String popup = req.getParameter("p");
        if ((popup != null) && popup.equals("popup")) {
            isPopup = true;
        }

        // Print header HTML
        printHeader(p, isPopup);

        if ((action != null) && action.equals("graph")) {
            printGraphPage(p, req);
        } else {
            printTempPage(p, isPopup);
        }

        // Print end of page
        printFooter(p);
        p.flush();
        p.close();
    }

    /**
     * Prints the HTML-header part of the page.
     *
     * @param p       Stream the output is printed on
     * @param refresh If this is set to true, page is automatically refreshed every 10 seconds
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void printHeader(ServletOutputStream p, boolean refresh) throws ServletException, IOException {
        p.println("<html>");
        p.println("<head>");
        p.println("	<title>Weather data</title>");
        p.println("	<link rel=\"stylesheet\" type=\"text/css\" href=\"web/temp/style.css\" />");
        if (refresh) {
            p.println("	<meta http-equiv=\"refresh\" content=\"10\" />");
        }
        p.println("</head>");
        p.println("<body class=\"main\">");
    }

    /**
     * Prints the log graph page. All configured thermometers which has an "LogFile"
     * attribute will be presented here. The actual graph is generated by the
     * {@link nu.nethome.home.items.web.GraphServlet}.
     *
     * @param p   Stream the output will be printed on
     * @param req The HTTP-Request
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void printGraphPage(ServletOutputStream p, HttpServletRequest req) throws ServletException, IOException {

        // Get all logged thermometers and verify that there are any
        Map<String, String> loggedThermometers = findThermometersWithLog();
        if (loggedThermometers.isEmpty()) {
            p.println("There are no thermometers with log history");
            return;
        }

        // Analyze arguments

        // Current thermometer
        String thermometer = req.getParameter("therm");
        if ((thermometer != null) && (thermometer.length() != 0)) {
            thermometer = fromURL(thermometer);
        } else {
            thermometer = loggedThermometers.keySet().iterator().next();
        }

        // End Time
        Date stopTime = null;
        String stopTimeString = req.getParameter("stop");
        if (stopTimeString != null) {
            // End time specified, try to parse it
            try {
                stopTime = dateFormat.parse(stopTimeString);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        if (stopTime == null) {
            // No valid end time found, use the end of current day
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            stopTime = cal.getTime();
        }

        // Time window and start time
        Date startTime = new Date();
        String windowString = req.getParameter("window");
        if ((windowString != null) && (windowString.equals("day"))) {
            startTime.setTime(stopTime.getTime() - HOURS24 + 1000);
        } else if ((windowString != null) && (windowString.equals("week"))) {
            startTime.setTime(stopTime.getTime() - WEEK + 1000L);
        } else if ((windowString != null) && (windowString.equals("month"))) {
            startTime.setTime(stopTime.getTime() - MONTH + 1000L);
        } else if ((windowString != null) && (windowString.equals("year"))) {
            startTime.setTime(stopTime.getTime() - YEAR + 1000L);
        } else {
            startTime.setTime(stopTime.getTime() - WEEK + 1000L);
            windowString = "week";
        }
        p.println("<img class=\"top\" src=\"web/temp/longtop.png\">");
        p.println("<div class=\"graphpanel\">");
        p.println("<table><tr><td>");
        p.println("<div class=\"graphdisplay\">");
        p.print("<img src=\"Graph?file=");
        p.print(loggedThermometers.get(thermometer));
        p.print("&start=" + dateFormat.format(startTime));
        p.print("&stop=" + dateFormat.format(stopTime));
        p.println("\">");
        p.println("</div></td><td><div class=\"buttoncolumn\">");
        p.println("Day<br><a href=\"" + toGraphLink(thermometer, stopTime, "day") + "\">" + buttonImage(windowString.equals("day")) + "</a>");
        p.println("<br><br>Week<br><a href=\"" + toGraphLink(thermometer, stopTime, "week") + "\">" + buttonImage(windowString.equals("week")) + "</a>");
        p.println("<br><br>Month<br><a href=\"" + toGraphLink(thermometer, stopTime, "month") + "\">" + buttonImage(windowString.equals("month")) + "</a>");
        p.println("<br><br>Year<br><a href=\"" + toGraphLink(thermometer, stopTime, "year") + "\">" + buttonImage(windowString.equals("year")) + "</a>");
        p.println("</div>");
        p.println("</td></tr></table>");
        p.println("<br>");
        p.println("<div class=\"graphbuttonrow\">");
        Set<String> names = loggedThermometers.keySet();
        for (String itemId : names) {
            HomeItemProxy item = server.openInstance(itemId);
            if (item != null) {
                p.print(item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE));
                p.println("  <a href=\"" + toGraphLink(item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE), stopTime, windowString) + "\">" + buttonImage(thermometer.equals(itemId)) + "</a>");
            }
        }
        p.println("</div>");
    }

    /**
     * Helper function to generate link to a graph page
     *
     * @param name   Thermometer to print graph for
     * @param to     End of period to print
     * @param window Time window
     * @return HTML-Code
     */
    String toGraphLink(String name, Date to, String window) {
        String result = localURL;
        result += "?therm=" + forURL(name);
        result += "&a=graph";
        result += "&stop=" + dateFormat.format(to);
        result += "&window=" + window;
        return result;
    }

    /**
     * Helper function, prints button with on/off LED
     *
     * @param on state of LED
     * @return HTML-Code
     */
    String buttonImage(boolean on) {
        String result = "<img class=\"button\" src=\"web/temp/";
        result += (on ? "silverButtonOn.png" : "silverButtonOff.png");
        result += "\">";
        return result;
    }

    /**
     * Prints the main thermometer value display page
     *
     * @param p       Stream to print result on
     * @param isPopup If true, the popup button is disabled
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void printTempPage(ServletOutputStream p, boolean isPopup) throws ServletException, IOException {
        p.println("<img class=\"top\" src=\"web/temp/top.png\">");
        p.println("<div class=\"panel\">");
        boolean isDisplay = false;
        boolean foundItemToDisplay = false;

        String temps[] = thermometerList.split(",");

        // Loop through all thermometer names
        for (int i = 0; i < temps.length; i++) {
            boolean foundValueToDisplay = false;
            // Find the corresponding thermometer
            HomeItemProxy item = server.openInstance(temps[i]);
            // Skip if we cannot open it
            if (item == null) continue;
            foundItemToDisplay = true;
            // Get the value(s)
            String stringTemp = item.getAttributeValue("Temperature").replace(',', '.');
            String stringHum = item.getAttributeValue("Humidity").replace(',', '.');
            String stringWindSpeed = item.getAttributeValue("WindSpeed").replace(',', '.');
            String stringWindDir = item.getAttributeValue("WindDirection").replace(',', '.');
            String stringRain = item.getAttributeValue("Rainfall").replace(',', '.');
            if (stringHum.length() == 0) {
                stringHum = item.getAttributeValue("Moisture").replace(',', '.');
            }

            if (isDisplay) {
                p.println("</div><br>");
                isDisplay = false;
            }
            // Print thermometer name
            p.println(item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE));
            if (!isDisplay) {
                p.println("<div class=\"display\">");
                isDisplay = true;
            }
            // Print thermometer's temperature value
            if (stringTemp.length() != 0) {
                float temp = Float.parseFloat(stringTemp);
                p.println("CH" + Integer.toString(i + 1) + " " + convertString(String.format("% 5.1fC", temp)));
                p.println("<div class=\"devider\"></div>");
                foundValueToDisplay = true;
            }
            // If the thermometer has a valid humidity value - print that
            if (stringHum.length() != 0) {
                float hum = Float.parseFloat(stringHum);
                p.println("CH" + Integer.toString(i + 1) + " " + convertString(String.format("% 5.1fP", hum)));
                p.println("<div class=\"devider\"></div>");
                foundValueToDisplay = true;
            }

            // If the rain has a valid rainfall value - print that
            if (stringRain.length() != 0) {
                float rainF = Float.parseFloat(stringRain);
                p.println("CH" + Integer.toString(i + 1) + " " + convertString(String.format("% 5.1fR", rainF)));
                p.println("<div class=\"devider\"></div>");
                foundValueToDisplay = true;
            }

            // If the Wind speed has a valid speed value - print that
            if (stringWindSpeed.length() != 0) {
                float windS = Float.parseFloat(stringWindSpeed);
                p.println("CH" + Integer.toString(i + 1) + " " + convertString(String.format("% 5.1fV", windS)));
                p.println("<div class=\"devider\"></div>");
                foundValueToDisplay = true;
            }

            // If the Wind direction has a valid direction value - print that
            if (stringWindDir.length() != 0) {
                float windD = Float.parseFloat(stringWindDir);
                p.println("CH" + Integer.toString(i + 1) + " " + convertWindDir(windD));
                p.println("<div class=\"devider\"></div>");
                foundValueToDisplay = true;
            }

            if (!foundValueToDisplay) {
                // If we have found no valid value to display, print an empty display
                p.println("CH" + Integer.toString(i + 1) + " " + convertString(" --,-"));
                p.println("<div class=\"devider\"></div>");
            }
        }
        if (!foundItemToDisplay) {
            // If we have found no valid thermometers, print an empty display
            p.println("<div class=\"display\">");
            p.println(convertString(" --,-"));
        }
        p.println("</div>"); // Display
        p.println("<br>");
        p.println("<div class=\"buttonrow\">");
        p.println("Graph  <a href=\"javascript: void(0)\" onclick=\"window.open('" + localURL + "?a=graph', 'graph', 'left=100, top=100, width=870,height=570, location=no, menubar=no, resizable=yes, scrollbars=no, status=no, titlebar=no, toolbar=no'); return false;\">");
        p.println("<img class=\"button\" src=\"web/temp/buttonSilver.png\"></a>");
        p.println("</div>");
        if (!isPopup) {
            p.println("<div class=\"buttonrow\">");
            p.println("Popup  <a href=\"javascript: void(0)\" onclick=\"window.open('" + localURL + "?p=popup', 'one', 'left=100, top=100, width=240,height=300, location=no, menubar=no, resizable=yes, scrollbars=no, status=no, titlebar=no, toolbar=no'); return false;\">");
            p.println("<img class=\"button\" src=\"web/temp/buttonSilver.png\"></a>");
            p.println("</div>");
        }
        p.println("</div>");
    }

    protected void printFooter(ServletOutputStream p) throws ServletException, IOException {
        p.println("</body>");
        p.println("</html>");
    }

    /**
     * Find which thermometers has a valid log file and return the log file names
     *
     * @return map from thermometer name to log file name
     */
    protected Map<String, String> findThermometersWithLog() {
        String temps[] = thermometerList.split(",");
        Map<String, String> result = new TreeMap<String, String>();

        // Loop through all thermometer names
        for (String temp : temps) {
            // Find the corresponding thermometer
            HomeItemProxy item = server.openInstance(temp);
            // Skip if we cannot open it
            if (item == null) continue;
            // Get the value(s)
            String logFile = item.getAttributeValue("LogFile");
            // Skip if it has no valid log file
            if (logFile == null || logFile.length() == 0) continue;
            result.put(temp, logFile);
        }
        return result;
    }

    final static String s_CharMap[] = {
            "N", "<img src=\"web/temp/dnh30.png\">",
            "E", "<img src=\"web/temp/deh30.png\">",
            "S", "<img src=\"web/temp/dsh30.png\">",
            "W", "<img src=\"web/temp/dwh30.png\">",
            "0", "<img src=\"web/temp/0h30.png\">",
            "1", "<img src=\"web/temp/1h30.png\">",
            "2", "<img src=\"web/temp/2h30.png\">",
            "3", "<img src=\"web/temp/3h30.png\">",
            "4", "<img src=\"web/temp/4h30.png\">",
            "5", "<img src=\"web/temp/5h30.png\">",
            "6", "<img src=\"web/temp/6h30.png\">",
            "7", "<img src=\"web/temp/7h30.png\">",
            "8", "<img src=\"web/temp/8h30.png\">",
            "9", "<img src=\"web/temp/9h30.png\">",
            " ", "<img src=\"web/temp/Sh30.png\">",
            "-", "<img src=\"web/temp/mh30.png\">",
            ",", "<img src=\"web/temp/Dh30.png\">",
            "C", "<img src=\"web/temp/Ch30.png\">", //deg celsius
            "P", "<img src=\"web/temp/Ph30.png\">", // %
            "V", "<img src=\"web/temp/WSh30.png\">",//wind speed
            "R", "<img src=\"web/temp/rth30.png\">" //rain total
    };

    /**
     * Convert a string to "display"-format where digits are replaced with simulated
     * LCD-images.
     *
     * @param source String to be display formated
     * @return reformatted string
     */
    protected String convertString(String source) {
        String result = "";

        for (int i = 0; i < source.length(); i++) {
            if (characterMap.containsKey(source.charAt(i))) {
                result += characterMap.get(source.charAt(i));
            } else {
                result += source.charAt(i);
            }
        }
        return result;
    }

    final static String s_WindDirMap[] = {
            "   N",
            " NNE",
            "  NE",
            " ENE",
            "   E",
            " ESE",
            "  SE",
            " SSE",
            "   S",
            " SSW",
            "  SW",
            " WSW",
            "   W",
            " WNW",
            "  NW",
            " NNW",
    };

    /**
     * Convert wind direction integer to source "display"-format where the wind direction
     * are replaced with simulated LCD-images.
     *
     * @param source Integer, 0 - 337,5 that represent a wind direction.
     * @return reformatted string
     */
    protected String convertWindDir(float source) {
        String SubstStr = " ";
        float index = source / WIND_DIR_CONV_CACTOR;

        if (index < s_WindDirMap.length) {
            SubstStr = s_WindDirMap[Math.round(index)];
        }
        return convertString(SubstStr);
    }

    public static String forURL(String aURLFragment) {
        String result;
        try {
            result = URLEncoder.encode(aURLFragment, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }

    public static String fromURL(String aURLFragment) {
        String result;
        try {
            result = URLDecoder.decode(aURLFragment, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }

    public String getWEBServer() {
        return webServer;
    }

    public void setWEBServer(String WEBServer) {
        webServer = WEBServer;
    }

    public String getLocalURL() {
        return localURL;
    }

    public void setLocalURL(String LocalURL) {
        localURL = LocalURL;
    }

    public String getThermometerList() {
        return thermometerList;
    }

    public void setThermometerList(String ThermometerList) {
        thermometerList = ThermometerList;
    }
}


