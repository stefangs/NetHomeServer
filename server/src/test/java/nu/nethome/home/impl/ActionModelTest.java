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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.hamcrest.core.Is.is;

public class ActionModelTest {

    ActionModel action;
    MockHomeItem target;

    @Before
    public void setUp() throws Exception {
        target = new MockHomeItem();
    }

    @Test
    public void callsAction() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        action = new ActionModel("ReturnFoo", "returnFoo", target.getClass());
        action.call(target);
        assertThat(target.getCalledMethods(), hasItem("returnFoo"));
    }

    @Test
    public void returnsActionResult() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        action = new ActionModel("ReturnFoo", "returnFoo", target.getClass());
        String result = action.call(target);
        assertThat(result, is("Foo"));
    }
}
