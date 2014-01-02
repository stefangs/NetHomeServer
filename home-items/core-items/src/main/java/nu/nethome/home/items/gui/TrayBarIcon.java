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

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("GUI")
public class TrayBarIcon extends HomeItemAdapter implements HomeItem {

    private static Logger logger = Logger.getLogger(TrayBarIcon.class.getName());
    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"TrayBarIcon\" Category=\"GUI\" >"
            + "  <Attribute Name=\"ShowWelcome\" Type=\"Boolean\" Get=\"isShowingWelcomeMessage\" 	Set=\"setShowingWelcomeMessage\" />"
            + "  <Attribute Name=\"Status\" Type=\"Boolean\" Get=\"getStatus\" />"
            + "  <Attribute Name=\"StatusMessage\" Type=\"String\" Get=\"getStatusMessage\"  />"
            + "</HomeItem> ");
    private SystemTray tray;
    private TrayIcon trayIcon;
    private JFrame logListFrame;
    private LogTableModel logTableModel;
    private long lastLogRowCount = 0;
    private Timer logRefreshTimer;
    private boolean showingWelcomeMessage = true;
    private boolean activated = false;
    private String status = "Ok";

    private static final String welcomeMessage =
            "Welcome to NetHomeServer\n\n" +
                    "NetHomeServer is a home control and automation software.\n" +
                    "The server runs in the background and you can access the user interface\n" +
                    "via a web browser on the address: http://127.0.0.1:8020/home\n" +
                    "Via the tray bar icon you can stop the server and see the log\n" +
                    "For more information please visit http://www.nethome.nu\n ";

    public String getModel() {
        return MODEL;
    }

    public void activate() {
        activated = false;
        if (SystemTray.isSupported()) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not set look and feel", e);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createTrayBarIcon();
                }
            });
        } else {
            status = "SystemTray is not supported on this system";
            logger.warning(status);
        }
    }

    public void stop() {
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
        if (logListFrame != null) {
            logListFrame.dispose();
        }
        if (logRefreshTimer != null) {
            logRefreshTimer.stop();
        }
    }

    private void createTrayBarIcon() {
        createLogWindow();
        PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(createImageIcon("home16.png", "NetHome Server").getImage());
        trayIcon.setImageAutoSize(false);
        trayIcon.setToolTip("NetHome Server");
        tray = SystemTray.getSystemTray();

        MenuItem welcomeItem = new MenuItem("Welcome");
        MenuItem logItem = new MenuItem("View log");
        MenuItem exitItem = new MenuItem("Stop Server");
        popup.add(welcomeItem);
        popup.addSeparator();
        popup.add(logItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            status = "Could not add tray Icon";
            logger.log(Level.WARNING, status, e);
            return;
        }

        welcomeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showWelcomeMessage();
            }
        });

        logItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTableModel.refresh(server.getState().getCurrentLogRecords());
                logListFrame.setVisible(true);
            }
        });

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        logRefreshTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshLogWindow();
            }
        });
        logRefreshTimer.start();
        activated = true;
        if (showingWelcomeMessage) {
            showWelcomeMessage();
        }
    }

    private void stopServer() {
        server.stopServer();
    }

    private void refreshLogWindow() {
        if (logListFrame.isVisible() && lastLogRowCount != server.getState().getTotalLogRecordCount()) {
            lastLogRowCount = server.getState().getTotalLogRecordCount();
            logTableModel.refresh(server.getState().getCurrentLogRecords());
        }
    }

    private void showWelcomeMessage() {
        JCheckBox checkbox = new JCheckBox("Do not show this message at startup");
        checkbox.setSelected(!showingWelcomeMessage);
        Object[] params = {welcomeMessage, checkbox};
        JOptionPane.showMessageDialog(null,
                params, "NetHome Server", JOptionPane.INFORMATION_MESSAGE, createImageIcon("home.png", "NetHome Server"));
        showingWelcomeMessage = !checkbox.isSelected();
    }

    private void createLogWindow() {
        logListFrame = new JFrame("NetHome server log");
        JTable table = new JTable();
        logTableModel = new LogTableModel();
        JScrollPane scrollPane = new JScrollPane(table);
        table.setModel(logTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.getColumnModel().getColumn(0).setMinWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setMaxWidth(160);
        table.getColumnModel().getColumn(1).setMinWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);
        table.setRowHeight(18);
        table.setShowGrid(false);
        logListFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        logListFrame.setIconImage(createImageIcon("home16.png", "NetHome Server").getImage());
        logListFrame.pack();
        logListFrame.setSize(800, 400);
        logListFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private ImageIcon createImageIcon(String path, String description) {
        URL imageURL = TrayBarIcon.class.getResource(path);
        if (imageURL == null) {
            logger.warning("Image not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description));
        }
    }

    public String isShowingWelcomeMessage() {
        return showingWelcomeMessage ? "True" : "False";
    }

    public void setShowingWelcomeMessage(String showingWelcomeMessage) {
        this.showingWelcomeMessage = showingWelcomeMessage.equalsIgnoreCase("true") || showingWelcomeMessage.equalsIgnoreCase("yes");
    }

    public String getStatusMessage() {
        return status;
    }

    public String getStatus() {
        return activated ? "Active" : "Inactive";
    }
}
