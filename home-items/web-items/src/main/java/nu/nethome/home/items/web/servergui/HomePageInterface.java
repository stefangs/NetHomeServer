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
import java.util.List;

/**
 * Interface for the HomeGUI page plugin classes
 *
 * @author Stefan Stromberg
 */
public interface HomePageInterface {

    /**
     * @return the names of the css-file used by the plugin. returns null if none.
     */
    public abstract List<String> getCssFileNames();

    /**
     * @return the names of the .js-file used by the plugin. returns null if none.
     */
    public abstract List<String> getJavaScriptFileNames();

    /**
     * @return the URL-name of the page
     */
    public abstract String getPageNameURL();

    /**
     * @return the visible name of the page
     */
    public abstract String getPageName();

    /**
     * @return true if the page supports an edit mode
     */
    public abstract boolean supportsEdit();

    public abstract List<String> getEditControls();

    /**
     * Print the page. Note that header and footer of the page is printed by the caller
     *
     * @param req    The HTTP request
     * @param res    The HTTP response
     * @param server The server interface to be able to open Items
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public abstract void printPage(HttpServletRequest req,
                                   HttpServletResponse res, HomeService server)
            throws ServletException, IOException;
}