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

import nu.nethome.home.impl.ModelException;
import nu.nethome.home.item.HomeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track on all attributes that points to another HomeItem, for all HomeItems that are added to the cache.
 * With this information it can answer the question: which HomeItems points to HomeItem x?
 */
public class RelationCache {

    private List<RelationItem> relationItems = new ArrayList<RelationItem>();

    public void addItem(HomeItem item) throws ModelException {
        RelationItem relationItem = new RelationItem(item);
        if (relationItem.hasRelation()) {
            relationItems.add(relationItem);
        }
    }

    public void removeItem(long itemId) {
        for (int i = 0; i < relationItems.size(); i++) {
            if (relationItems.get(i).getItem().getItemId() == itemId) {
                relationItems.remove(i);
                return;
            }
        }
    }

    public List<HomeItem> getRelatedTo(String itemId) {
        List<HomeItem> result = new ArrayList<HomeItem>();

        for (RelationItem relationItem : relationItems) {
            if (relationItem.hasRelationTo(itemId)) {
                result.add(relationItem.getItem());
            }
        }
        return result;
    }
}
