/*
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
import nu.nethome.home.item.HomeItemType;

/**
 * Holds static HomeITem class info
 */
public class HomeItemClassInfo implements HomeItemInfo {

    private Class<? extends HomeItem> itemClass;

    public HomeItemClassInfo(Class<? extends HomeItem> itemClass) {
        this.itemClass = itemClass;
    }

    @Override
    public String getClassName() {
        return itemClass.getSimpleName();
    }

    @Override
    public String getCategory() {
        HomeItemType type = itemClass.getAnnotation(HomeItemType.class);
        if (type != null) {
            return type.value();
        } else {
            return "Lamps";
        }
    }

    @Override
    public String[] getCreationEventTypes() {
        HomeItemType type = itemClass.getAnnotation(HomeItemType.class);
        if (type != null && !type.creationEvents().isEmpty()) {
            return type.creationEvents().split(",");
        } else {
            return new String[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HomeItemClassInfo that = (HomeItemClassInfo) o;

        if (!itemClass.getSimpleName().equals(that.itemClass.getSimpleName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return itemClass.getSimpleName().hashCode();
    }
}
