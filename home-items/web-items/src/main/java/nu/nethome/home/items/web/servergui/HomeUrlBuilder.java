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

/**
 * Helper to buld URL:s which also is aware of some of the standard arguments in the application
 */
public class HomeUrlBuilder {
    private StringBuilder result;
    private char separator = '?';

    public HomeUrlBuilder(String localUrl) {
        result = new StringBuilder(localUrl);
    }

    public HomeUrlBuilder addParameter(String parameter, String value) {
        result.append(separator).append(parameter).append("=").append(value);
        separator = '&';
        return this;
    }

    public HomeUrlBuilder addParameterIfNotNull(String parameter, String value) {
        if (value != null) {
            addParameter(parameter, value);
        }
        return this;
    }

    public HomeUrlBuilder withPage(String page) {
        addParameter("page", page);
        return this;
    }

    public HomeUrlBuilder withAction(String action) {
        addParameter("a", action);
        return this;
    }

    public HomeUrlBuilder gotoReturnPage(HomeGUIArguments arguments) {
        if (arguments.hasReturnPage()) {
            addParameter("page", arguments.getReturnPage());
            preserveEditMode(arguments);
            if (arguments.hasReturnSubpage()) {
                addParameter("subpage", arguments.getReturnSubpage());
            }
        }
        return this;
    }

    public HomeUrlBuilder preserveReturnPage(HomeGUIArguments arguments) {
        if (arguments.hasReturnPage()) {
            addParameter("return", arguments.getReturnPage());
            preserveEditMode(arguments);
            if (arguments.hasReturnSubpage()) {
                addParameter("returnsp", arguments.getReturnSubpage());
            }
        }
        return this;
    }

    public HomeUrlBuilder preserveRoom(EditItemArguments arguments) {
        if (arguments.hasRoom()) {
            addParameter("room", arguments.getRoom());
        }
        return this;
    }

    public HomeUrlBuilder preserveEditMode(HomeGUIArguments arguments) {
        if (arguments.isEditMode()) {
            addParameter("mode", "edit");
        }
        return this;
    }

    @Override
    public String toString() {
        return result.toString();
    }

    public String toQuotedString() {
        return "\"" + result.toString() + "\"";
    }
}
