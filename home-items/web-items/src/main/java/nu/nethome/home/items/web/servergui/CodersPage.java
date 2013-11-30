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

import nu.nethome.coders.decoders.Decoders;
import nu.nethome.coders.encoders.Encoders;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolEncoder;
import nu.nethome.util.ps.ProtocolInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class CodersPage extends PortletPage {

    private final HomeService server;

    public CodersPage(String mLocalURL, HomeService server) {
        super(mLocalURL);
        this.server = server;
    }

    @Override
    public String getPageNameURL() {
        return "coders";
    }

    public String getPageName() {
        return "Coders";
    }

    @Override
    public String getIconUrl() {
        return "web/home/coder32.png";
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

        p.println("<div class=\"itemcolumn log\">");
        printEncoderPanel(p);
        p.println("<br>");
        printDecoderPanel(p);
        p.println("</div>");
    }

    private void printEncoderPanel(PrintWriter p) {
        try {
            printCoderPanelStart(p, "Installed Protocol Encoders");
            printEncoderRows(p, server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class), "plugin16.png");
            printEncoderRows(p, Encoders.getAllTypes(), "wave16.png");
            printCoderPanelEnd(p);
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void printDecoderPanel(PrintWriter p) {
        try {
            printCoderPanelStart(p, "Installed Protocol Decoders");
            printDecoderRows(p, server.getPluginProvider().getPluginsForInterface(ProtocolDecoder.class), "plugin16.png");
            printDecoderRows(p, Decoders.getAllTypes(), "wave16.png");
            printCoderPanelEnd(p);
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void printEncoderRows(PrintWriter p, Collection<Class<? extends ProtocolEncoder>> coderTypes, String icon) throws InstantiationException, IllegalAccessException {
        for (Class<? extends ProtocolEncoder> encoderClass : coderTypes) {
            ProtocolInfo info = encoderClass.newInstance().getInfo();
            printCoderRow(p, icon, info);
        }
    }

    private void printDecoderRows(PrintWriter p, Collection<Class<? extends ProtocolDecoder>> coderTypes, String icon) throws InstantiationException, IllegalAccessException {
        for (Class<? extends ProtocolDecoder> decoderClass : coderTypes) {
            ProtocolInfo info = decoderClass.newInstance().getInfo();
            printCoderRow(p, icon, info);
        }
    }

    private void printCoderRow(PrintWriter p, String icon, ProtocolInfo info) {
        p.println("  <tr>");
        p.println("   <td><img src=\"web/home/" + icon + "\" /></td>");
        p.println("   <td>" + info.getName() + "</td>");
        p.println("   <td>" + info.getType() + "</td>");
        p.println("   <td>" + info.getLength() + "</td>");
        p.println("   <td>" + info.getCompany() + "</td>");
        p.println("  </tr>");
    }

    protected void printCoderPanelStart(PrintWriter p, String header) {
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader thin\">");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>" + header + "</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"logrows coders\">");
        p.println(" <table>");
        p.println("  <tr class=\"logrowsheader\"><td></td><td>Name</td><td>Type</td><td>Bits</td><td>Manufacturer</td></tr>");

    }

    private void printCoderPanelEnd(PrintWriter p) {
        p.println(" </table>");
        p.println("</div>");
        p.println("        <div class=\"footer thin\"></div>");
        p.println("    </div>");
    }
}
