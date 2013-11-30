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

import nu.nethome.home.system.DirectoryEntry;

/**
 * Directory entry when listing instances of HomeItems
 */
public class InternalDirectoryEntry implements DirectoryEntry {
    private String instanceName;
    private long instanceId;
    private String category;

    public InternalDirectoryEntry(String instanceName, long instanceId, String category) {
        this.instanceName = instanceName;
        this.instanceId = instanceId;
        this.category = category;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public String getCategory() {
        return category;
    }
}
