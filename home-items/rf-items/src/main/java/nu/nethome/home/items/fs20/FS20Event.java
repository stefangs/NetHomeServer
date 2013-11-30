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

package nu.nethome.home.items.fs20;

public class FS20Event {
	protected int houseCode = 0;
	protected byte function = 0;
	protected byte button = 0;
	
	
	public FS20Event(int houseCode, byte function, byte button) {
		super();
		this.houseCode = houseCode;
		this.function = function;
		this.button = button;
	}
	
	public String toString() {
		return "HouseCode: " + Integer.toHexString(houseCode) + 
		" Button: " + Integer.toString(button) + " Function: " + Integer.toString(function);
	}
	/**
	 * @return the button
	 */
	public byte getButton() {
		return button;
	}
	/**
	 * @param button the button to set
	 */
	public void setButton(byte button) {
		this.button = button;
	}
	/**
	 * @return the function
	 */
	public byte getFunction() {
		return function;
	}
	/**
	 * @param function the function to set
	 */
	public void setFunction(byte function) {
		this.function = function;
	}
	/**
	 * @return the houseCode
	 */
	public int getHouseCode() {
		return houseCode;
	}
	/**
	 * @param houseCode the houseCode to set
	 */
	public void setHouseCode(int houseCode) {
		this.houseCode = houseCode;
	}

}
