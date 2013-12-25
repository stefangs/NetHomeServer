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

package nu.nethome.home.start;

import nu.nethome.home.item.HomeItem;

/**
 * Contains information about the underlying POJO for a Home Item
 *
 * @author Stefan Str�mberg
 */
public class HomeItemClassInfo {

    private String className;
    private Class<?> clazz;
    private boolean isPublic;

    public String getClassName() {
        return className;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public HomeItemClassInfo(String className, Class<?> clazz) {
        this.className = className;
        this.clazz = clazz;
        isPublic = true;
    }

    public HomeItemClassInfo(String className, Class<?> clazz, boolean aPublic) {
        this.className = className;
        this.clazz = clazz;
        isPublic = aPublic;
    }

    public HomeItem createHomeItem() throws IllegalAccessException, InstantiationException {
        return (HomeItem) clazz.newInstance();
    }

    public Class<?> getItemClass() {
        return clazz;
    }


}
