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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Stefan 2013-11-10
 */
public class SingleHomeItemFactory implements HomeItemFactory {

    Class<? extends HomeItem> itemClass;

    private static Logger logger = Logger.getLogger(SingleHomeItemFactory.class.getName());

    public SingleHomeItemFactory(Class<? extends HomeItem> itemClass) {
        this.itemClass = itemClass;
    }

    @Override
    public HomeItem createInstance(String s) {
        if (s.equals(itemClass.getSimpleName())) {
            try {
                return itemClass.newInstance();
            } catch (InstantiationException e) {
                logger.warning("Could not create Home Item: " + s);
            } catch (IllegalAccessException e) {
                logger.warning("Could not create Home Item: " + s);
            }
        }
        return null;
    }

    @Override
    public List<String> listClasses(boolean b) {
        return Arrays.asList(itemClass.getSimpleName());
    }
}
