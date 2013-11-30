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

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

/**
 * This is a simple example of a HomeItem. It demonstrates how to receive and send events.
 * Events are used to inform other Items about events that have occurred in the system.
 * One example of that is the "minute event" that the server sends once every minute and which can be
 * used for simple time keeping without having to create a timer in the Item.
 *
 * Note that the class is annotated as a "Plugin" which makes it possible for the NetHomeServer to load it
 * dynamically. All you have to do is to pack the class in a jar and place the jar in the "plugins" folder.
 *
 * @author Stefan Strömberg
 *
 */
@Plugin
public class Example2 extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"Example2\" Category=\"Controls\" >"
            + "  <Attribute Name=\"MinuteCount\" 		Type=\"String\" Get=\"getMinuteCount\" Default=\"true\" />"
            + "  <Action Name=\"Reset\" 	Method=\"reset\" Default=\"true\" />"
            + "</HomeItem> ");

    private int minuteCount = 0;

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            minuteCount++;
            return true;
        }
        return false;
    }

    public String getMinuteCount() {
        return Integer.toString(minuteCount);
    }

    public void reset() {
        minuteCount = 0;
        Event resetEvent = server.createEvent("ResetEvent", "");
        server.send(resetEvent);
    }
}
