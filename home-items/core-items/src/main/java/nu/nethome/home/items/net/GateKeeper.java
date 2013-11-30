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

package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


/**
 * The GateKeeper is a simple security measure to use instead of a firewall.
 * It allows remote activation and de-activation of functions for example TCPProxies, to make sure the
 * services are only active when really needed.
 * After a preset time the activated functions are automatically deactivated. It is used together with
 * the TCPListener Item which can receive messages over the net which may be used to start and stop.
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
public class GateKeeper extends HomeItemAdapter implements HomeItem {

    private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"GateKeeper\" Category=\"Ports\" >"
            + "	<Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "	<Attribute Name=\"Auto Open Time (Min)\" Type=\"String\" Get=\"getOpenTime\" Set=\"setOpenTime\" />"
            + "	<Attribute Name=\"Open Keyword\" Type=\"String\" Get=\"getOpenString\" Set=\"setOpenString\" />"
            + "	<Attribute Name=\"Close Keyword\" Type=\"String\" Get=\"getCloseString\" Set=\"setCloseString\" />"
            + "	<Attribute Name=\"Open Command\" Type=\"Command\" Get=\"getOpenCommand\" Set=\"setOpenCommand\" />"
            + "	<Attribute Name=\"Close Command\" Type=\"Command\" Get=\"getCloseCommand\" Set=\"setCloseCommand\" />"
            + "	<Action Name=\"openGate\" Method=\"openGate\" Default=\"true\" >"
            + "		<Argument Name=\"None\" Type=\"String\" />"
            + "	</Action>"
            + "	<Action Name=\"closeGate\" Method=\"closeGate\" />"
            + "</HomeItem>");

    private static Logger logger = Logger.getLogger(GateKeeper.class.getName());

    private class CloseTask extends TimerTask {
        protected GateKeeper keeper;

        public CloseTask(GateKeeper keeper) {
            this.keeper = keeper;
        }

        public void run() {
            logger.info("Closing gate due to timeout");
            keeper.closeGate();
        }
    }

    private Timer openTimer = new Timer();
    private CloseTask closer = new CloseTask(this);
    private CommandLineExecutor commandLineExecutor;

    // Public attributes
    private int openTime = 1 * 60 * 60 * 1000; // One hour
    private boolean isOpen = false;
    private String openString = "Open:";
    private String closeString = "Close:";
    private String openCommand = "";
    private String closeCommand = "";

    public GateKeeper() {
    }

    public void activate(HomeService service) {
        super.activate(service);
        commandLineExecutor = new CommandLineExecutor(service, true);
    }

    public boolean receiveEvent(Event event) {
        String eventType = event.getAttribute("Type");
        String data = event.getAttribute("Value");
        if (eventType != null && data != null) {
            if (eventType.equals("TCPMessage")) {
                int index = data.indexOf(openString);
                if (index != -1) {
                    logger.info("Receiving open request from " + event.getAttribute("IPAddress"));
                    openGate();
                }
                index = data.indexOf(closeString);
                if (index != -1) {
                    logger.info("Receiving close request from " + event.getAttribute("IPAddress"));
                    closeGate();
                }
                return true;
            }
        }
        return false;
    }

    public String getModel() {
        return m_Model;
    }

    public String getOpenTime() {
        return String.valueOf(openTime / 60000);
    }

    public void setOpenTime(String openTime) {
        this.openTime = Integer.parseInt(openTime) * 60000;
    }

    void setOpenTime(int openTimeMs) {
        this.openTime = openTimeMs;
    }

    public String getState() {
        if (isOpen) {
            return "Open";
        }
        return "Closed";
    }

    public void openGate() {
        logger.fine("Opening Gate");

        // Execute the open command
        commandLineExecutor.executeCommandLine(openCommand);

        // Cancel any ongoing close timers
        closer.cancel();

        // Create a new closing timer task
        closer = new CloseTask(this);
        openTimer.schedule(closer, openTime);
        isOpen = true;
    }

    public void closeGate() {
        logger.fine("Closing Gate");

        // Cancel any ongoing close timers
        closer.cancel();

        // Execute the close command
        commandLineExecutor.executeCommandLine(closeCommand);
        isOpen = false;
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        if (isActivated()) {
            closeGate();
        }
        openTimer.cancel();
    }

    public String getCloseString() {
        return closeString;
    }

    public void setCloseString(String closeString) {
        this.closeString = closeString;
    }

    public String getOpenString() {
        return openString;
    }

    public void setOpenString(String openString) {
        this.openString = openString;
    }

    public String getOpenCommand() {
        return openCommand;
    }

    public void setOpenCommand(String openCommand) {
        this.openCommand = openCommand;
    }

    public String getCloseCommand() {
        return closeCommand;
    }

    public void setCloseCommand(String closeCommand) {
        this.closeCommand = closeCommand;
    }
}
