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
 * History:
 * 2010-10-31 pela Changed ServletOutputStream to PrintWriter to support UTF-8 and encodings properly
 */
package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ServerFloor extends PortletPage {

    public ServerFloor(String mLocalURL) {
        super(mLocalURL);
    }

    public String getPageName() {
        // TODO Auto-generated method stub
        return "All Items";
    }

    public String getPageNameURL() {
        // TODO Auto-generated method stub
        return "server";
    }

    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {

        HomeGUIArguments arguments = new HomeGUIArguments(req);
        String selectedCategory;
        if (arguments.hasSubpage()) {
            selectedCategory = arguments.getSubpage();
        } else {
            selectedCategory = "Lamps";
        }

        PrintWriter p = res.getWriter();
        List<DirectoryEntry> directoryEntries = server.listInstances("");
        ArrayList<String> categoryItems = new ArrayList<String>();

        // Loop through all instances to find the rooms and categorize them after column
        for (DirectoryEntry directoryEntry : directoryEntries) {
            // Open the instance so we know class and category
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            if (model.getCategory().equals(selectedCategory)) {
                categoryItems.add(item.getAttributeValue("Name"));
            }
        }

        // Start left column
        printColumnStart(p, true);

        p.println("<table>");
        String isSelected = "";
        for (String category : HomeItemModel.HOME_ITEM_CATEGORIES) {
            isSelected = category.equals(selectedCategory) ? " selected" : "";
            p.println("<tr class=\"catlink" + isSelected + "\">");
            p.print("	 <td><img src=\"web/home/" + HomeGUI.itemIcon(category, false) + "\" /></td>" +
                    "<td><a href=\"" + localURL + "?page=server&subpage=" + category + "\">" + category + "</a></td>");
            p.println("</tr>");
        }
        p.println("</table>");

        // End left column
        printColumnEnd(p);

        // Start right column
        printColumnStart(p, false);

        if (selectedCategory != null) {
            printRoom(p, "server", arguments.getSubpage(), selectedCategory, null, null, categoryItems.toArray(new String[categoryItems.size()]), server);
        }

        // End right column
        printColumnEnd(p);
    }
}
