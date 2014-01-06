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

package nu.nethome.home.impl.relation;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.impl.ModelException;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class RelationItem {
    private HomeItem realItem;
    private HomeItemProxy proxy;
    private List<RelationAttribute> attributes = new ArrayList<RelationAttribute>();

    public class ItemRelation implements RelationAttribute {
        String attributeName;

        public ItemRelation(Attribute attribute) {
            this.attributeName = attribute.getName();
        }

        @Override
        public boolean hasRelationTo(String itemIdToFind) {
            return proxy.getAttributeValue(attributeName).equals(itemIdToFind);
        }
    }

    public class ItemsRelation implements RelationAttribute {
        String attributeName;

        public ItemsRelation(Attribute attribute) {
            this.attributeName = attribute.getName();
        }

        @Override
        public boolean hasRelationTo(String itemIdToFind) {
            List<String> ids = Arrays.asList(proxy.getAttributeValue(attributeName).split(","));
            return ids.contains(itemIdToFind);
        }
    }

    public class ActionRelation implements RelationAttribute {
        String attributeName;

        public ActionRelation(Attribute attribute) {
            this.attributeName = attribute.getName();
        }

        @Override
        public boolean hasRelationTo(String itemIdToFind) {
            String actionParams[] = proxy.getAttributeValue(attributeName).split(",");
            return actionParams != null && actionParams.length >= 2 && actionParams[1].equals(itemIdToFind);
        }
    }

    RelationItem(HomeItem itemToDecorate) throws ModelException {
        proxy = new LocalHomeItemProxy(itemToDecorate);
        realItem = itemToDecorate;
        for (Attribute attribute : proxy.getAttributeValues()) {
            if (attribute.getType().equals("Item")) {
                attributes.add(new ItemRelation(attribute));
            } else if (attribute.getType().equals("Items")) {
                attributes.add(new ItemsRelation(attribute));
            } else if (attribute.getType().equals("Command")) {
                attributes.add(new ActionRelation(attribute));
            }
        }
    }

    public boolean hasRelationTo(String item) {
        for (RelationAttribute attribute : attributes) {
            if (attribute.hasRelationTo(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRelation() {
        return attributes.size() > 0;
    }

    HomeItem getItem() {
        return realItem;
    }
}
