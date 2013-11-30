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

import nu.nethome.home.item.Action;
import nu.nethome.home.item.HomeItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionModel implements Action {
    private final String name;
    private final Method actionMethod;

    public ActionModel(String actionName, String actionMethod, Class<? extends HomeItem> clazz) throws NoSuchMethodException {
		this.name = actionName;
        this.actionMethod = clazz.getMethod(actionMethod, (Class[]) null);
	}

    public String getName() {
        return name;
    }

    public String call(HomeItem item) throws InvocationTargetException, IllegalAccessException {
        return (String) actionMethod.invoke(item, (Object[]) null);
    }
}
