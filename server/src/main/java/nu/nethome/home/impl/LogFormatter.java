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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Format log records
 *
 * @author Stefan
 */
public class LogFormatter extends Formatter {

	/* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.S ");
    private String lineSeparator = System.getProperty("line.separator");

    public synchronized String format(LogRecord record) {
        String className = record.getSourceClassName();
        int lastDotIndex = (className != null) ? className.lastIndexOf('.') : 0;
        if (lastDotIndex > 0) {
            className = className.substring(lastDotIndex + 1);
        } else {
            className = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(formatter.format(new Date(record.getMillis())));
        sb.append(record.getLevel().getName());
        sb.append(":");
        sb.append(record.getMessage());
        sb.append(" (");
        sb.append(className);
        sb.append(" ");
        sb.append(record.getSequenceNumber());
        sb.append(", TID");
        sb.append(record.getThreadID());
        sb.append(") ");
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }
}
