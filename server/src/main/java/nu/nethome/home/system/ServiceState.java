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

import java.util.Collection;
import java.util.logging.LogRecord;

/**
 * Holds information about the current state of the service
 */
public interface ServiceState {

    /**
     * Retrieves the last log records generated in the system. The list is reset at system start. The list is bounded
     * so only the last records generated are returned. Log records are also stored in log files.
     * @return last log entries
     */
    Collection<LogRecord> getCurrentLogRecords();

    /**
     * Returns number of current log records with severity WARNING or higher
     * @return number of records
     */
    int getCurrentAlarmCount();

    /**
     * Returns the total number of log records that has been generated since server was started
     * @return
     */
    long getTotalLogRecordCount();
}
