/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.hue;

/**
 *
 */
public class LightId {

    public final String lampId;
    public final String lampName;

    public LightId(String lampId, String lampName) {
        this.lampId = lampId;
        this.lampName = lampName;
    }

    public String getLampId() {
        return lampId;
    }

    public String getLampName() {
        return lampName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightId lightId = (LightId) o;

        if (!lampId.equals(lightId.lampId)) return false;
        if (!lampName.equals(lightId.lampName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lampId.hashCode();
    }
}
