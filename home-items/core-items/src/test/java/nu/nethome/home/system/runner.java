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

package nu.nethome.home.system;

import nu.nethome.home.impl.HomeManager;

/**
 * Created by IntelliJ IDEA.
 * User: Stefan
 * Date: 2011-10-12
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class runner extends HomeManager {

    public static void main(String[] args) {
        HomeManager me = new HomeManager();
        me.go(args);
    }
}
