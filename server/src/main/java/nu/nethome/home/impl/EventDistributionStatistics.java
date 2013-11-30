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

/**
 * Keeps track of Event distribution timing and statistics
 * @author Stefan Str�mberg
 */
public class EventDistributionStatistics {
	
	public static final double NANO_PER_MILLI = 1000000.0;
	private volatile long m_RoundStartTime;
	private volatile boolean m_RoundActive = false;
	private volatile long m_ItemStartTime;
	private volatile boolean m_ItemActive = false;
	
	private volatile long m_MaxRoundTime = 0;
	private volatile long m_MinRoundTime = Long.MAX_VALUE;
	private volatile long m_SumRoundTime = 0;
	private volatile long m_NumberOfRounds = 0;

	private volatile long m_MaxItemTime = 0;
	private volatile long m_MinItemTime = Long.MAX_VALUE;
	private volatile long m_SumItemTime = 0;
	private volatile long m_NumberOfItems = 0;
	private volatile String m_CurrentItem = "";
	private volatile String m_MaxItem = "";
	
	/**
	 * Reset all statistics values
	 */
	public void resetStatistics() {
		synchronized (this) {
			m_MaxRoundTime = 0;
			m_MinRoundTime = Long.MAX_VALUE;
			m_SumRoundTime = 0;
			m_NumberOfRounds = 0;
			m_MaxItemTime = 0;
			m_MinItemTime = Long.MAX_VALUE;
			m_SumItemTime = 0;
			m_NumberOfItems = 0;
			m_MaxItem = "";
		}
	}

	/**
	 * Mark the start of an event distribution round
	 */
	public void startDistributionRound() {
		synchronized (this) {
			m_RoundStartTime = System.nanoTime();
			m_RoundActive = true;
		}
	}

	/**
	 * Mark the end of an event distribution round
	 */
	public void endDistributionRound() {
		synchronized (this) {
			m_RoundActive = false;
			long time = System.nanoTime() - m_RoundStartTime;
			if (time > m_MaxRoundTime) {
                m_MaxRoundTime = time;
            }
			if (time < m_MinRoundTime) {
                m_MinRoundTime = time;
            }
			m_SumRoundTime+= time;
			m_NumberOfRounds++;
		}
	}
	
	/**
	 * Mark the start of distributing an event to an item
	 */
	public void startItemDistribution(String itemName) {
		synchronized (this) {
			m_ItemStartTime = System.nanoTime();
			m_CurrentItem = itemName;
			m_ItemActive = true;
		}		
	}

	/**
	 * Mark the end of distributing an event to an item
	 */
	public void endItemDistribution() {
		synchronized (this) {
			m_ItemActive = false;
            long time = currentItemProcessingTime();
            if (time > m_MaxItemTime) {
				m_MaxItemTime = time;
				m_MaxItem = m_CurrentItem;
			}
			if (time < m_MinItemTime) {
                m_MinItemTime = time;
            }
			m_SumItemTime+= time;
			m_NumberOfItems++;
			m_CurrentItem = "";
		}		
	}

    public long currentItemProcessingTime() {
        return System.nanoTime() - m_ItemStartTime;
    }

    public boolean isItemCurrentlyProcessingEvent() {
        return m_ItemActive;
    }

	/**
	 * Get the time in ms the current distribution round has been active. Returns 0 if
	 * no round is active.
	 * @return time in ms
	 */
	public double getCurrentRoundTime() {
		synchronized(this) {
			if (!m_RoundActive) {
                return 0;
            }
			return (System.nanoTime() - m_RoundStartTime) / NANO_PER_MILLI;
		}
	}

	/**
	 * Get the time in ms the distribution of the event to the current item has been
	 * going on. Returns 0 if no Item distribution is active. 
	 * @return time in ms
	 */
	public double getCurrentItemTime() {
		synchronized(this) {
			if (!m_ItemActive) {
                return 0;
            }
			return (System.nanoTime() - m_ItemStartTime) / NANO_PER_MILLI;
		}
	}

	/**
	 * Get max distribution round time
	 * @return time in ms
	 */
	public double getMaxRoundTime() {
		return m_MaxRoundTime / NANO_PER_MILLI;
	}

	/**
	 * Get min distribution round time
	 * @return time in ms
	 */
	public double getMinRoundTime() {
		return m_MinRoundTime / NANO_PER_MILLI;
	}

	/**
	 * Get the average distribution round time 
	 * @return time in ms
	 */
	public double getAvarageRoundTime() {
		synchronized (this) {
			if (m_NumberOfRounds == 0) {
                return 0;
            }
			return (m_SumRoundTime / m_NumberOfRounds) / NANO_PER_MILLI;
		}
	}

	/**
	 * Get number of rounds distributed
	 * @return number of rounds
	 */
	public long getNumberOfRounds() {
		return m_NumberOfRounds;
	}

	/**
	 * Get the maximum time spent for an Item processing an event
	 * @return time in ms
	 */
	public double getMaxItemTime() {
		return m_MaxItemTime / NANO_PER_MILLI;
	}

	/**
	 * Get the minimum time spent for an Item processing an event
	 * @return time in ms
	 */
	public long getMinItemTime() {
		return m_MinItemTime;
	}

	/**
	 * Get the average time spent for Items processing events
	 * @return time in ms
	 */
	public double getAvarageItemTime() {
		synchronized (this) {
			if (m_NumberOfItems == 0) {
                return 0;
            }
			return (m_SumItemTime / m_NumberOfItems) / NANO_PER_MILLI;
		}
	}

	/**
	 * Get total number of Item Event deliveries
	 * @return number
	 */
	public long getNumberOfItems() {
		return m_NumberOfItems;
	}

	/**
	 * Get the name of the current Item receiving an Event. "" if no Event processing
	 * is currently on going. 
	 * @return name of Item or ""
	 */
	public String getCurrentItemName() {
		return m_CurrentItem;
	}

	/**
	 * Get the name of the Item currently having the longest Event processing time
	 * @return
	 */
	public String getMaxItemName() {
		return m_MaxItem;
	}
}
