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

package nu.nethome.home.items.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

class LogTableModel extends AbstractTableModel {

    List<LogRecord> logList;
    String headerList[] = new String[]{"", "Time", "Source", "Message"};
    private SimpleDateFormat dateFormat;
    private ImageIcon info;
    private ImageIcon warn;
    private ImageIcon critical;

    public LogTableModel() {
        logList = Arrays.asList(new LogRecord(Level.INFO, "Initial"));
        dateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        info = createIcon("info.png", "Info");
        warn = createIcon("warn.png", "Info");
        critical = createIcon("critical.png", "Info");
    }

    public void refresh(Collection<LogRecord> logList) {
        this.logList = new ArrayList<LogRecord>(logList);
        this.fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return logList.size();
    }

    // this method is called to set the value of each cell
    @Override
    public Object getValueAt(int row, int column) {
        LogRecord record;
        record = logList.get(row);
        switch (column) {
            case 0: {
                int level = record.getLevel().intValue();
                if (level >= Level.SEVERE.intValue()) {
                    return critical;
                } else if (level >= Level.WARNING.intValue()) {
                    return warn;
                } else {
                    return info;
                }
            }
            // return record.getLevel().getLocalizedName();
            case 1:
                return dateFormat.format(new Date(record.getMillis()));
            case 2: {
                String[] separatedName = record.getSourceClassName().split(Pattern.quote("."));
                return separatedName.length > 0 ? separatedName[separatedName.length - 1] : "";
            }
            case 3:
                return record.getMessage();
            default:
                return "";
        }
    }

    public String getColumnName(int col) {
        return headerList[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex == 0) ? ImageIcon.class : String.class;
    }

    private ImageIcon createIcon(String path, String description) {
        URL imageURL = LogTableModel.class.getResource(path);
        if (imageURL == null) {
            return null;
        } else {
            return (new ImageIcon(imageURL, description));
        }
    }

}