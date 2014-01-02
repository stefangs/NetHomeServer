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

package nu.nethome.home.items.pronto;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Controls")
public class ProntoDevice extends ProntoLamp implements HomeItem {

    private static Logger logger = Logger.getLogger(ProntoDevice.class.getName());
    public static final int COMMAND_COUNT = 10;

    private class ProntoCommand {
        private String commandName = "";
        private String prontoCode = "";

        public boolean isSet() {
            return (commandName.length() > 0) && (prontoCode.length() > 0);
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public String getProntoCode() {
            return prontoCode;
        }

        public void setProntoCode(String prontoCode) {
            this.prontoCode = prontoCode;
        }
    }

    private static final String MODEL_START = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ProntoDevice\" Category=\"Controls\"  Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"OnCode\" Type=\"String\" Get=\"getOnCode\" 	Set=\"setOnCode\" />"
            + "  <Attribute Name=\"OffCode\" Type=\"String\" Get=\"getOffCode\" 	Set=\"setOffCode\" />"
            + "  <Attribute Name=\"ToggleCode\" Type=\"String\" Get=\"getToggleCode\" 	Set=\"setToggleCode\" />"
            + "  <Attribute Name=\"StartDelay\" Type=\"Integer\" Get=\"getStartupTime\" 	Set=\"setStartupTime\" />"
            + "  <Attribute Name=\"Repeat\" Type=\"String\" Get=\"getRepeat\" 	Set=\"setRepeat\" />"
            + "  <Attribute Name=\"UseModulation\" Type=\"String\" Get=\"getUseModulation\" 	Set=\"setUseModulation\" />");

    private static final String MODEL_END = ("</HomeItem> ");

    private static final String COMMAND = (
            "  <Attribute Name=\"Command#\" Type=\"String\" Get=\"getCommand#Name\" Set=\"setCommand#Name\" />"
                    + "  <Attribute Name=\"CommandCode#\" Type=\"String\" Get=\"getCommand#Code\" Set=\"setCommand#Code\" />");

    private static final String ON_COMMAND = (
            "  <Action Name=\"on\" 	Method=\"on\" />");

    private static final String OFF_COMMAND = (
            "  <Action Name=\"off\" 	Method=\"off\" />");

    private static final String TOGGLE_COMMAND = (
            "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />");

    private static final String ACTION = (
            "  <Action Name=\"#1\" 	Method=\"command#2\" />");


    private String toggleCode = "";
    private int startupTime = 0;
    private Timer startTimer = new Timer("ProntoDeviceTimer", true);
    private volatile boolean isStartingUp = false;
    private Queue<String> commandQueue = new LinkedBlockingQueue<String>();
    private ProntoCommand[] commands = new ProntoCommand[COMMAND_COUNT];

    public ProntoDevice() {
        for (int i = 0; i < COMMAND_COUNT; i++) {
            commands[i] = new ProntoCommand();
        }
    }

    public String getModel() {
        StringBuilder builder = new StringBuilder(MODEL_START);
        for (int i = 0; i < COMMAND_COUNT; i++) {
            builder.append(COMMAND.replaceAll("#", Integer.toString(i)));
        }
        builder.append(ON_COMMAND);
        builder.append(OFF_COMMAND);
        builder.append(TOGGLE_COMMAND);
        for (int i = 0; i < COMMAND_COUNT; i++) {
            ProntoCommand command = commands[i];
            if (command.isSet()) {
                builder.append(ACTION.replaceAll("#1", command.getCommandName()).replaceAll("#2", Integer.toString(i)));
            }
        }
        builder.append(MODEL_END);
        return builder.toString();
    }

    @Override
    public void on() {
        boolean previousItemState = itemState;
        logger.fine("Switching on " + name);
        if (onCode.length() > 0) {
            sendCommand(onCode);
        } else if (!itemState && toggleCode.length() > 0) {
            sendCommand(toggleCode);
        }
        itemState = true;
        if (!previousItemState && !isStartingUp) {
            initiateStartupState();
        }
    }

    private void initiateStartupState() {
        if (startupTime > 0) {
            isStartingUp = true;
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    endStartupState();
                }
            }, startupTime);
        }
    }

    private void endStartupState() {
        isStartingUp = false;
        for (String command : commandQueue) {
            sendCommand(command);
        }
    }

    @Override
    public void sendCommand(String command) {
        if (!isStartingUp) {
            super.sendCommand(command);
        } else {
            commandQueue.add(command);
        }
    }

    @Override
    public void off() {
        logger.fine("Switching off " + name);
        if (offCode.length() > 0) {
            sendCommand(offCode);
        } else if (itemState && toggleCode.length() > 0) {
            sendCommand(toggleCode);
        }
        itemState = false;
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        if (toggleCode.length() > 0) {
            sendCommand(toggleCode);
        } else if (itemState && offCode.length() > 0) {
            sendCommand(offCode);
        } else if (!itemState && onCode.length() > 0) {
            sendCommand(onCode);
        }
        itemState = !itemState;
        if (itemState && !isStartingUp) {
            initiateStartupState();
        }
    }

    public String getToggleCode() {
        return toggleCode;
    }

    public void setToggleCode(String toggleCode) {
        this.toggleCode = toggleCode;
    }

    public String getStartupTime() {
        return Integer.toString(startupTime / 1000);
    }

    public void setStartupTime(String startupTime) {
        this.startupTime = Integer.parseInt(startupTime) * 1000;
    }

    void setStartupTime(int startupTime) {
        this.startupTime = startupTime;
    }

    private void sendCommandNumber(int commandNumber) {
        sendCommand(commands[commandNumber].getProntoCode());
    }

    public String getCommand0Name() {
        return commands[0].getCommandName();
    }

    public void setCommand0Name(String commandName) {
        commands[0].setCommandName(commandName);
    }

    public String getCommand0Code() {
        return commands[0].getProntoCode();
    }

    public void setCommand0Code(String code) {
        commands[0].setProntoCode(code);
    }

    public void command0() {
        sendCommandNumber(0);
    }

    public String getCommand1Name() {
        return commands[1].getCommandName();
    }

    public void setCommand1Name(String commandName) {
        commands[1].setCommandName(commandName);
    }

    public String getCommand1Code() {
        return commands[1].getProntoCode();
    }

    public void setCommand1Code(String code) {
        commands[1].setProntoCode(code);
    }

    public void command1() {
        sendCommandNumber(1);
    }

    public String getCommand2Name() {
        return commands[2].getCommandName();
    }

    public void setCommand2Name(String commandName) {
        commands[2].setCommandName(commandName);
    }

    public String getCommand2Code() {
        return commands[2].getProntoCode();
    }

    public void setCommand2Code(String code) {
        commands[2].setProntoCode(code);
    }

    public void command2() {
        sendCommandNumber(2);
    }

    public String getCommand3Name() {
        return commands[3].getCommandName();
    }

    public void setCommand3Name(String commandName) {
        commands[3].setCommandName(commandName);
    }

    public String getCommand3Code() {
        return commands[3].getProntoCode();
    }

    public void setCommand3Code(String code) {
        commands[3].setProntoCode(code);
    }

    public void command3() {
        sendCommandNumber(3);
    }

    public String getCommand4Name() {
        return commands[4].getCommandName();
    }

    public void setCommand4Name(String commandName) {
        commands[4].setCommandName(commandName);
    }

    public String getCommand4Code() {
        return commands[4].getProntoCode();
    }

    public void setCommand4Code(String code) {
        commands[4].setProntoCode(code);
    }

    public void command4() {
        sendCommandNumber(4);
    }

    public String getCommand5Name() {
        return commands[5].getCommandName();
    }

    public void setCommand5Name(String commandName) {
        commands[5].setCommandName(commandName);
    }

    public String getCommand5Code() {
        return commands[5].getProntoCode();
    }

    public void setCommand5Code(String code) {
        commands[5].setProntoCode(code);
    }

    public void command5() {
        sendCommandNumber(5);
    }

    public String getCommand6Name() {
        return commands[6].getCommandName();
    }

    public void setCommand6Name(String commandName) {
        commands[6].setCommandName(commandName);
    }

    public String getCommand6Code() {
        return commands[6].getProntoCode();
    }

    public void setCommand6Code(String code) {
        commands[6].setProntoCode(code);
    }

    public void command6() {
        sendCommandNumber(6);
    }

    public String getCommand7Name() {
        return commands[7].getCommandName();
    }

    public void setCommand7Name(String commandName) {
        commands[7].setCommandName(commandName);
    }

    public String getCommand7Code() {
        return commands[7].getProntoCode();
    }

    public void setCommand7Code(String code) {
        commands[7].setProntoCode(code);
    }

    public void command7() {
        sendCommandNumber(7);
    }

    public String getCommand8Name() {
        return commands[8].getCommandName();
    }

    public void setCommand8Name(String commandName) {
        commands[8].setCommandName(commandName);
    }

    public String getCommand8Code() {
        return commands[8].getProntoCode();
    }

    public void setCommand8Code(String code) {
        commands[8].setProntoCode(code);
    }

    public void command8() {
        sendCommandNumber(8);
    }

    public String getCommand9Name() {
        return commands[9].getCommandName();
    }

    public void setCommand9Name(String commandName) {
        commands[9].setCommandName(commandName);
    }

    public String getCommand9Code() {
        return commands[9].getProntoCode();
    }

    public void setCommand9Code(String code) {
        commands[9].setProntoCode(code);
    }

    public void command9() {
        sendCommandNumber(9);
    }
}
