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

package nu.nethome.home.items.nexa;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Calendar;
import java.util.logging.Logger;

/**
 * Nexa Smoke Detector represents a wireless smoke detector sold by Nexa. It can invoke an action when the
 * alarm triggers and can also trigger the alarm on the device.
 *
 * @author Stefan
 */
@Plugin
public class NexaSmokeDetector extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaSmokeDetector\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\"  Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"AlarmCommand\" Type=\"Command\" Get=\"getAlarmCommand\" 	Set=\"setAlarmCommand\" />"
            + "  <Attribute Name=\"AlarmTime\" Type=\"String\" Get=\"getRepeatTime\" 	Set=\"setRepeatTime\" />"
            + "  <Action Name=\"Alarm\" 	Method=\"sendAlarm\" />"
            + "  <Action Name=\"LearnAddress\" 	Method=\"learnAddress\" />"
            + "</HomeItem> ");


    private CommandLineExecutor commandExecutor;
    private static Logger logger = Logger.getLogger(NexaSmokeDetector.class.getName());
    private Calendar lastAlarmTime = null;
    private boolean isLearning;

    // Public attributes
    private int itemAddress = 0;
    private String alarmCommand = "";
    private int repeatTime = 5;

    public NexaSmokeDetector() {
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        // If it is a NexaFire_Message addressed to us or we have specified our
        // address empty (0, 0, 0) then raise the alarm.
        if (event.getAttribute("Type").equals("NexaFire_Message") &&
                event.getAttribute("Direction").equals("In")) {

            // If we are in learning mode, learn the address
            if (isLearning) {
                itemAddress = event.getAttributeInt("NexaFire.Address");
                isLearning = false;
            }
            // If this is directed to us - raise the alarm!
            if ((itemAddress == 0) || (event.getAttributeInt("NexaFire.Address") == itemAddress)) {
                raiseAlarm();
            }
            return true;
        }
        return false;
    }

    public String getModel() {
        return MODEL;
    }

    /**
     * Raise the alarm by invoking the Alarm Command. If this has already been called within the
     * repeat time, this action is ignored and the Alarm Command is not executed
     */
    private void raiseAlarm() {
        // Get current time
        Calendar now = Calendar.getInstance();

        // Check if we have already triggered the alarm action within the repeat time. Ignore in that case
        if ((lastAlarmTime != null) &&
                ((now.getTimeInMillis() - lastAlarmTime.getTimeInMillis()) < repeatTime * 1000)) {
            return;
        }
        logger.finer("NexaSmokeDetector executing: " + alarmCommand);
        // Execute command
        String result = commandExecutor.executeCommandLine(alarmCommand);
        logger.finer("Result:" + result);
        lastAlarmTime = now;
    }

    /**
     * This method will send the alarm message to the smoke detector and cause it to sound the alarm signal
     */
    @SuppressWarnings("UnusedDeclaration")
    public void sendAlarm() {
        // Set alarm time, so we don't trigger on our own alarm
        lastAlarmTime = Calendar.getInstance();

        // Reset learning mode
        isLearning = false;

        // Fill in the Event and send it
        Event alarmMessage = server.createEvent("NexaFire_Message", "");
        alarmMessage.setAttribute("Direction", "Out");
        alarmMessage.setAttribute("NexaFire.Address", itemAddress);
        server.send(alarmMessage);
    }

    /**
     * Set Item in address learning mode. The Item will assume the address of the
     * next NexaFire-message that is received.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void learnAddress() {
        isLearning = true;
    }

    /* Getters and Setters for the attributes */

    /**
     * @return Returns the Address.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getState() {
        if (isLearning) {
            return "Learning";
        } else if ((lastAlarmTime != null) &&
                ((Calendar.getInstance().getTimeInMillis() - lastAlarmTime.getTimeInMillis()) < repeatTime * 1000)) {
            // If we are within the active time from the last alarm trigger - then we are in alarm state
            return "Alarm";
        }
        return "Idle";
    }

    /**
     * @return Returns the Address.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
        return Integer.toString(itemAddress);
    }

    /**
     * @param address The Address to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address) {
        itemAddress = Integer.parseInt(address);
    }

    /**
     * @return Returns the AlarmCommand.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getAlarmCommand() {
        return alarmCommand;
    }

    /**
     * @param alarmCommand The AlarmCommand to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAlarmCommand(String alarmCommand) {
        this.alarmCommand = alarmCommand;
    }

    /**
     * @return Returns the RepeatTime.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getRepeatTime() {
        return Integer.toString(repeatTime);
    }

    /**
     * @param repeatTime The RepeatTime to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRepeatTime(String repeatTime) {
        this.repeatTime = Integer.parseInt(repeatTime);
    }
}
