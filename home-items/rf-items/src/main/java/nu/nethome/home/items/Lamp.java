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

package nu.nethome.home.items;

public interface Lamp {

	public abstract void on();

	public abstract void off();

	public abstract void bright();

	public abstract void dim();

	public abstract void toggle();

	public abstract void dimLoop();

	public String getState();
	
	/**
	 * @return Returns the m_LocationX.
	 */
	public String getLocationX();

	/**
	 * @param locationX The m_LocationX to set.
	 */
	public void setLocationX(String locationX);

	/**
	 * @return Returns the m_LocationY.
	 */
	public String getLocationY();

	/**
	 * @param locationY The m_LocationY to set.
	 */
	public void setLocationY(String locationY);

}