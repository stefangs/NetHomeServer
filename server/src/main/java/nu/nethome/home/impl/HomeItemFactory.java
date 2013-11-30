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

package nu.nethome.home.impl;

import nu.nethome.home.item.HomeItem;

import java.util.List;

/**
 * Interface of factory class for creating HomeItems
 */
public interface HomeItemFactory {
    /**
     * Create an instance of the HomeItem specified by className. For backward compatibility
     * the fully qualified Java class name is also accepted, the use of this is
     * however considered deprecated.
     * @param className Name of the class of which an instance shall be created
     * @return A HomeItem instance or null
     */
    HomeItem createInstance(String className);

    /**
     * Return a list of all supported class names
     * @param includeHidden Include hidden classes
     * @return Class Names
     */
    List<String> listClasses(boolean includeHidden);
}
