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

package nu.nethome.home.item;

import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

/**
 * This Adapter is simply a helper to avoid some of the boiler plate code in the HomeItem
 * Using this you only really have to implement the getModel()-method.
 */
public abstract class HomeItemAdapter implements HomeItem {

    protected long id = 0;
    protected String name = "No Name Yet";
    protected HomeService server = null;

    public boolean receiveEvent(Event event) {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getItemId() {
        return id;
    }

    public void setItemId(long id) {
        this.id = id;
    }

    public void activate(HomeService server) {
        this.server = server;
        activate();
    }

    public void activate() {
    }

    public void stop() {
        this.server = null;
    }

    protected boolean isActivated() {
        return server != null;
    }
}
