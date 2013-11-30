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

package nu.nethome.home.items.tellstick;

import java.util.Date;

/**
* Created by Stefan 2013-11-21
*/
public class RawMessage {
    private int[] data;
    private int repeat;
    private Date creationTime;

    RawMessage(int[] data, int repeat) {
        this(data, repeat, new Date());
    }

    public RawMessage(int[] data, int repeat, Date creationTime) {
        this.data = data;
        this.repeat = repeat;
        this.creationTime = creationTime;
    }

    int[] getData() {
        return data;
    }

    int getRepeat() {
        return repeat;
    }

    public Date getCreationTime() {
        return creationTime;
    }
}
