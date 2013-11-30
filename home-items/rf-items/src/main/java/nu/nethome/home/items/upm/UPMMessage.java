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

package nu.nethome.home.items.upm;

/*
 * My current understanding of the UPM data message:
 * The message consists of four bytes.
 * 
 * c = House Code (0 - 15)
 * d = Device Code (1 - 4) ?
 * p = Primary value - Temperature/Rain/Wind speed value (low bits)
 * P = Primary value - Temperature/Rain/Wind speed value (high bits)
 * s = Secondary value - Humidity/Wind direction (low bits)
 * S = Secondary value - Humidity/Wind direction (high bits)
 * b = Low battery indication
 * x = ?
 * y = ?
 * 
 * If HouseCode = 10 and deviceCode = 2, then p and P is Wind speed
 * and s and S is Wind direction
 * 
 * If HouseCode = 10 and deviceCode = 3, then p and P is rain
 * 
 * ____Byte 0_____  ____Byte 1_____  ____Byte 2_____  ____Byte 3_____
 * 7 6 5 4 3 2 1 0  7 6 5 4 3 2 1 0  7 6 5 4 3 2 1 0  7 6 5 4 3 2 1 0
 * x x x x c c c c  d d y y b H H H  h h h h h T T T  t t t t t t t t
 *                                            
 * Temp (C) = RawValue * 0.0624031 - 49.8356589
 * Humidity (%) = RawValue * 0.5
 */

/**
 * Represents and decodes the data message from the UPM-thermometer
 * @author Stefan
 */
public class UPMMessage {
	int houseCode  = 0;
	int deviceCode = 0;
	int primary = 0;
	int secondary = 0;
	int lowBattery = 0;
	int x = 0;
	int y = 0;

	public UPMMessage(int[] data){
		houseCode = data[0] & 0x0F;
		deviceCode = ((data[1] >> 6) & 0x03) + 1;
		primary = ((data[2] & 0x07) << 8) + data[3];
		secondary = ((data[1] & 0x07) << 5) + (data[2] >> 3);
		lowBattery = (data[1] >> 3) & 0x01;
		y = (data[1] >> 4) & 0x03;
		x = data[0] >> 4;
	}
	
	/**
	 * @return Returns the deviceCode.
	 */
	public int getDeviceCode() {
		return deviceCode;
	}
	/**
	 * @return Returns the houseCode.
	 */
	public int getHouseCode() {
		return houseCode;
	}
	
	/**
	 * @return Returns the primary value
	 */
	public int getPrimary() {
		return primary;
	}
	
	/**
	 * @return Returns the secondary value
	 */
	public int getSecondary() {
		return secondary;
	}
	
	/**
	 * @return Returns the low battery warning status.
	 */
	public int getLowBattery() {
		return lowBattery;
	}

	/**
	 * @return Returns the unknown bits.
	 */
	public String getUnknown() {
		String text = "x=";
		text += Integer.toHexString(x);
		text += " y=";
		text += Integer.toHexString(y);
		return text;
	}
}
