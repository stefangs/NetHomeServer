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

package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class LogPage extends PortletPage {

    private final HomeService server;
    private final SimpleDateFormat dateFormat;

    public LogPage(String mLocalURL, HomeService server) {
        super(mLocalURL);
        this.server = server;
        dateFormat = new SimpleDateFormat("yy.MM.dd'&nbsp;'HH:mm:ss");
    }

    @Override
    public String getPageNameURL() {
        return "log";
    }

    public String getPageName() {
        return "Log";
    }

    @Override
    public String getIconUrl() {
        return "web/home/log32.png";
    }

    /**
     * This is the main entrance point of the class. This is called when a http
     * request is routed to this servlet.
     */
    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        PrintWriter p = res.getWriter();
        HomeGUIArguments pageArguments = new HomeGUIArguments(req);

        printLogHeading(p);

        printLog(p);

        printLogFooter(p);
    }

    private void printLog(PrintWriter p) {
        p.println("<div class=\"logrows\">");
        p.println(" <table>");
        Collection<LogRecord> logRows = server.getState().getCurrentLogRecords();
        p.println("  <tr class=\"logrowsheader\"><td></td><td>Time</td><td>Source</td><td>Message</td></tr>");
        for (LogRecord logRow : logRows) {
            p.println("  <tr>");
            p.println("   <td><img src=\"web/home/" + getIconName(logRow.getLevel().intValue()) + "\" /></td>");
            p.println("   <td>" + dateFormat.format(new Date(logRow.getMillis())) + "</td>");
            String[] separatedName = logRow.getSourceClassName().split(Pattern.quote("."));
            p.println("   <td>" + (separatedName.length > 0 ? separatedName[separatedName.length - 1] : "") + "</td>");
            p.println("   <td>" + logRow.getMessage() + "</td>");
            p.println("  </tr>");
        }
        p.println(" </table>");
        p.println("</div>");
    }

    private String getIconName(int level) {
        String iconName;
        if (level >= Level.SEVERE.intValue()) {
            iconName = "critical.png";
        } else if (level >= Level.WARNING.intValue()) {
            iconName = "warn.png";
        } else {
            iconName = "info.png";
        }
        return iconName;
    }

    private void printLogFooter(PrintWriter p) {
        p.println("        <div class=\"footer thin\"></div>");
        p.println("    </div>");
        p.println("</div>");
        p.println("</div>");
    }

    protected void printLogHeading(PrintWriter p) {
        p.println("<div class=\"itemcolumn log\">");
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader thin\">");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>Server Log</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
    }
}
