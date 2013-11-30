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

package nu.nethome.home.items.util;

import nu.nethome.home.impl.InternalHomeItemProxy;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ItemAttributeTester {
    public static void testAttributes(HomeItem item, String... attributes) throws IllegalValueException {
        HomeItemProxy proxy = new InternalHomeItemProxy(item, null);

        for (String attributeValuePair : attributes) {
            String attributeAndValue[] = attributeValuePair.split("=");
            proxy.setAttributeValue(attributeAndValue[0], attributeAndValue[1]);
            assertThat("Failed to get attribute " + attributeAndValue[0], proxy.getAttributeValue(attributeAndValue[0]),
                    is(attributeAndValue[1]));
        }
    }
}
