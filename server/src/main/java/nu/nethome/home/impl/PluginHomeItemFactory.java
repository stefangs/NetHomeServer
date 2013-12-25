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
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.util.plugin.PluginProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

/**
 * HomeItem-factory which loads HomeItems as plugins
 */
public class PluginHomeItemFactory implements HomeItemFactory {

    private Map<String, Class<?>> pluginsClasses = new TreeMap<String, Class<?>>();
    private List<HomeItemInfo> pluginsClassInfo = new ArrayList<HomeItemInfo>();
    private static Logger logger = Logger.getLogger(PluginHomeItemFactory.class.getName());

    public PluginHomeItemFactory(PluginProvider provider) {
        for (Class<? extends HomeItem> c : provider.getPluginsForInterface(HomeItem.class)) {
            pluginsClasses.put(c.getSimpleName(), c);
            pluginsClassInfo.add(new HomeItemClassInfo(c));
        }
    }

    public HomeItem createInstance(String className) {
        HomeItem result = null;
        Class<?> c = pluginsClasses.get(className);
        if (null != c) {
            try {
                // get the constructor with no parameters
                java.lang.reflect.Constructor<?> constructor = c.getConstructor();

                // create an instance
                Object invoker = constructor.newInstance();
                result = (HomeItem) invoker;
            } catch (NoSuchMethodException e) {
                logger.warning("Correct constructor in " + className
                                + " not found");
            } catch (InstantiationException e) {
                logger.warning("Class " + className + " could not be instantiated " + e.getMessage());
            } catch (InvocationTargetException e) {
                logger.warning("Class " + className + " could not be created/invoked " + e.getMessage());
            } catch (IllegalAccessException e) {
                logger.warning("Not allowed to call constructor in class "
                        + className);
            }
        }
        return result;
    }

    public List<String> listClasses(boolean includeHidden) {
        return new LinkedList<String>(pluginsClasses.keySet());
    }

    @Override
    public List<HomeItemInfo> listItemTypes() {
        return Collections.unmodifiableList(pluginsClassInfo);
    }
}
