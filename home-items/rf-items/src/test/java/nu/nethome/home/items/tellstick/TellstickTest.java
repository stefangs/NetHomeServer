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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TellstickTest {

    Tellstick tellstick;
    int testMessage[] = {20, 40, 60, 80, 64, 100, 20, 40, 60, 80, 64, 100};
    int copy[];

    @Before
    public void setUp() throws Exception {
        tellstick = new Tellstick();
        copy = Arrays.copyOf(testMessage, testMessage.length);
    }

    @Test
    public void notConnectedAfterCreation() {
        assertThat(tellstick.getState(), is("Not connected"));
    }
}
