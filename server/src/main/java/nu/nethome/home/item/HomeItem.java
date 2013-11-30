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
 * Interface for the Item-classes in the HomeManager system. The basic interface needed for the
 * framework to handle the class.
 *
 * @author Stefan
 */
public interface HomeItem {
    /**
     * Receive and process the Event.
     *
     *
     * @param event the Event to process
     * @return true if the Item handled the Event
     */
    boolean receiveEvent(Event event);

    /**
     * Get the definition of the HomeItem model. The model is returned as an XML-String
     *
     * @return XML formatted model description
     */
    String getModel();

    /**
     * Get the instance name of the HomeItem
     *
     * @return Name
     */
    String getName();

    /**
     * Set the instance name of the HomeItem. This method is called by the
     * framework before the activate() method is called.
     *
     * @param name
     */
    void setName(String name);

    /**
     * Get the unique identity of the HomeItem. This identity is unique within
     * one server instance and does never change.
     *
     * @return Identity
     */
    long getItemId();

    /**
     * Set the unique identity of the HomeItem. This identity is unique within
     * one server instance and do never change. This method is called by the framework
     * before the activate() method is called.
     *
     * @param id Identity
     */
    void setItemId(long id);

    /**
     * Activate the HomeItem. This is called once by the framework after the instance is created
     * and any configuration is written via the set methods. The HomeItem shall enter an active state.
     * At this stage the HomeItem may start allocating resources and start threads or timers it needs.
     */
    void activate(HomeService server);

    /**
     * Stop the HomeItem. This is called by the framework when the server is shutting down.
     * The HomeItem shall close and return any allocated resources and stop any internally
     * running threads.
     */
    void stop();
}
