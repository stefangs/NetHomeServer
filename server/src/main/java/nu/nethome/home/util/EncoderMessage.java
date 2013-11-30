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

package nu.nethome.home.util;

import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EncoderMessage implements Message {

    private ArrayList<FieldValue> values = new ArrayList<FieldValue>();

    @Override
    public List<FieldValue> getFields() {
        return Collections.unmodifiableList(values);
    }

    public void addValue(FieldValue value) {
        values.add(value);
    }
}
