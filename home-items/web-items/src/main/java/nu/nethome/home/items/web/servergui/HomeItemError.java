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

/**
 * @author Peter Lagerhem
 * 
 * History:
 * 2010-10-27 pela Created.
 * 
 */
package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.Attribute;

/**
 * This class can be used to collect errors of home item attributes. You could
 * typically use it where IllegalFormat (and alike) exceptions are caught and
 * you want to record the cause for the error and the attribute.
 * 
 * The printPage() method of the EditItemPage class uses this class for
 * displaying the attribute errors.
 */
public class HomeItemError {
	
    private Attribute attribute;
    private String errorMessage;
    public ErrorType type;

	public enum ErrorType {
		general, attribute
	}

	/**
	 * @return the attribute
	 */
	public Attribute getAttribute() {
		return attribute;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public HomeItemError(Attribute attribute, String errorMessage) {
		this.attribute = attribute;
		this.errorMessage = errorMessage;
		type = ErrorType.attribute;
	}
	
	public HomeItemError(String errorMessage) {
		this.errorMessage = errorMessage;
		type = ErrorType.general;
	}
}
