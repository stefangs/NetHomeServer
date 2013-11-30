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

import nu.nethome.home.impl.relation.RelationCache;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.system.DirectoryEntry;

import java.util.*;

public class ItemDirectory {

    public static final String RELATED_SEARCH_KEYWORD = "@related=";
    private volatile List<HomeItem> homeItems = new LinkedList<HomeItem>();
    private volatile AbstractMap<String, HomeItem> homeItemNameMap = new TreeMap<String, HomeItem>();
    private volatile AbstractMap<Long, HomeItem> homeItemIDMap = new TreeMap<Long, HomeItem>();
    private volatile AbstractMap<Long, String> categoryMap = new TreeMap<Long, String>();
    private volatile RelationCache relationCache = new RelationCache();

    public ItemDirectory() {
    }

    Iterator<HomeItem> iterator() {
        return homeItems.iterator();
    }

    final List<HomeItem> getHomeItems() {
        return Collections.unmodifiableList(homeItems);
    }

    public synchronized int registerInstance(HomeItem item) {
        String name = item.getName();
        if (name == null) {
            return 1;
        }
        if (homeItemNameMap.containsKey(name)) {
            return 2;
        }
        if (homeItemIDMap.containsKey(item.getItemId()) || (item.getItemId() == 0)) {
            return 3;
        }
        homeItems.add(item);
        homeItemNameMap.put(name, item);
        homeItemIDMap.put(item.getItemId(), item);
        try {
            categoryMap.put(item.getItemId(), StaticHomeItemModel.getModel(item).getCategory());
            relationCache.addItem(item);
        } catch (ModelException e) {
            return 4;
        }
        return 0;
    }

    public synchronized List<DirectoryEntry> listInstances(String pattern) {
        LinkedList<DirectoryEntry> result = new LinkedList<DirectoryEntry>();
        List<HomeItem> itemsToList = getFilteredItemList(pattern);
        for (HomeItem current : itemsToList) {
                result.add(new InternalDirectoryEntry(current.getName(),
                        current.getItemId(),
                        categoryMap.get(current.getItemId())));
        }
        return result;
    }

    private List<HomeItem> getFilteredItemList(String pattern) {
        if (pattern.length() == 0) {
            return homeItems;
        } else if (pattern.startsWith(RELATED_SEARCH_KEYWORD) && pattern.length() > RELATED_SEARCH_KEYWORD.length()) {
            return relationCache.getRelatedTo(pattern.split("=")[1]);
        }
        return Collections.emptyList();
    }

    public synchronized HomeItem findInstance(String name) {
        HomeItem item = homeItemNameMap.get(name);
        if (item == null) {
            try {
                item = homeItemIDMap.get(Long.parseLong(name));
            } catch (NumberFormatException n) {
                // Do Dinada
            }
        }
        return item;
    }

    public synchronized boolean renameInstance(String fromInstanceName, String toInstanceName) {
        HomeItem item = findInstance(fromInstanceName);
        if (item == null) {
            // Item does not exist
            return false;
        }

        if (homeItemNameMap.containsKey(toInstanceName)) {
            // New name already exists, quit
            return false;
        }

        // Register under the new name
        homeItemNameMap.put(toInstanceName, item);

        // Remove registration under the old name
        homeItemNameMap.remove(fromInstanceName);

        // Rename the instance
        item.setName(toInstanceName);

        return true;
    }

    public synchronized HomeItem removeInstance(String instanceName) {
        HomeItem item = findInstance(instanceName);
        if (item == null) {
            // Item does not exist
            return null;
        }

        // Remove registration of instance
        categoryMap.remove(item.getItemId());
        homeItemIDMap.remove(item.getItemId());
        homeItemNameMap.remove(instanceName);
        homeItems.remove(item);
        relationCache.removeItem(item.getItemId());
        return item;
    }

    public synchronized void clear() {
        categoryMap.clear();
        homeItems.clear();
        homeItemNameMap.clear();
        homeItemIDMap.clear();
    }
}