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

package nu.nethome.home.items;

import nu.nethome.home.item.*;
import nu.nethome.util.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Stefan 2013-11-17
 */
@Plugin
@HomeItemType("Controls")
public class LampGroup extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"LampGroup\" Category=\"Controls\" >"
            + "  <Attribute Name=\"Lamps\" 	Type=\"Items\" Get=\"getLamps\" 	Set=\"setLamps\" />"
            + "  <Attribute Name=\"Delay\" 	Type=\"StringList\" Get=\"getDelay\" 	Set=\"setDelay\">"
            + "     <item>0</item> <item>100</item> <item>200</item> <item>300</item> <item>500</item> <item>1000</item></Attribute>"
            + "  <Action Name=\"on\" 	Method=\"performOn\" />"
            + "  <Action Name=\"off\" 	Method=\"performOff\" />"
            + "  <Action Name=\"recall\" 	Method=\"performRecall\" />"
            + "  <Action Name=\"dim1\" 	Method=\"performDim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"performDim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"performDim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"performDim4\" />"
            + "</HomeItem> ");

    private static final int MAX_QUEUE_SIZE = 10;
    public static final String QUIT_COMMAND = "quit";
    private enum Command {
        quit, on, off, recall, dim1, dim2, dim3, dim4;
    }

    private String lamps = "";
    private String lampsOn = "";
    private LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>(MAX_QUEUE_SIZE);
    private long delay = 300;


    public void commandDistributorTask() {
        while (true) {
            try {
                // Take the next event from the queue, will wait if no events yet
                Command command = commandQueue.take();
                // Check if it was the quit event, quit in that case
                switch (command) {
                    case quit:
                        return;
                    case on:
                        on();
                        break;
                    case off:
                        off();
                        break;
                    case recall:
                        recall();
                        break;
                    case dim1:
                        dim1();
                        break;
                    case dim2:
                        dim3();
                        break;
                    case dim3:
                        dim3();
                        break;
                    case dim4:
                        dim4();
                        break;
                }
            } catch (InterruptedException e) {
                // Do Dinada
            }
        }
    }

    private void dim1() {
        tryPerformActionOnItems(lamps, "dim1", "on");
    }

    private void dim2() {
        tryPerformActionOnItems(lamps, "dim2", "on");
    }

    private void dim3() {
        tryPerformActionOnItems(lamps, "dim3", "on");
    }

    private void dim4() {
        tryPerformActionOnItems(lamps, "dim4", "on");
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        Thread eventThread = new Thread("CommandDistributor") {
            @Override
            public void run() {
                commandDistributorTask();
            }
        };
        eventThread.start();
    }

    @Override
    public void stop() {
        commandQueue.add(Command.quit);
        super.stop();
    }

    public String getLamps() {
        return lamps;
    }

    public void setLamps(String lamps) {
        this.lamps = lamps;
    }

    public String getLampsOn() {
        return lampsOn;
    }

    public void setLampsOn(String lampsOn) {
        this.lampsOn = lampsOn;
    }

    public String getDelay() {
        return Long.toString(delay);
    }

    public void setDelay(String delay) {
        this.delay = Long.parseLong(delay);
    }

    public void performOn() {
        commandQueue.add(Command.on);
    }

    public void performOff() {
        commandQueue.add(Command.off);
    }

    public void performRecall() {
        commandQueue.add(Command.recall);
    }

    public void performDim1() {
        commandQueue.add(Command.dim1);
    }

    public void performDim2() {
        commandQueue.add(Command.dim2);
    }

    public void performDim3() {
        commandQueue.add(Command.dim3);
    }

    public void performDim4() {
        commandQueue.add(Command.dim4);
    }

    private void on() {
        performActionOnItems(lamps, "on");
    }

    private void off() {
        StringBuilder result = new StringBuilder();
        String separator = "";
        for (HomeItemProxy item : getAsItems(lamps)) {
            if (item.getAttributeValue("State").equalsIgnoreCase("on")) {
                result.append(separator).append(item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
                separator = ",";
            }
            try {
                item.callAction("off");
                Thread.sleep(delay);
            } catch (ExecutionFailure executionFailure) {
                // fail silently
            } catch (InterruptedException e) {
                // fail silently
            }
        }
        lampsOn = result.toString();
    }

    private void recall() {
        performActionOnItems(lampsOn, "on");
    }

    private void performActionOnItems(String items, String action) {
        for (HomeItemProxy item : getAsItems(items)) {
            try {
                item.callAction(action);
                Thread.sleep(delay);
            } catch (ExecutionFailure executionFailure) {
                // fail silently
            } catch (InterruptedException e) {
                // fail silently
            }
        }
    }

    private void tryPerformActionOnItems(String items, String action, String fallbackAction) {
        for (HomeItemProxy item : getAsItems(items)) {
            try {
                if (item.getModel().hasAction(action)) {
                    item.callAction(action);
                } else {
                    item.callAction(fallbackAction);
                }
                Thread.sleep(delay);
            } catch (ExecutionFailure executionFailure) {
                // fail silently
            } catch (InterruptedException e) {
                // fail silently
            }
        }
    }

    private List<HomeItemProxy> getAsItems(String items) {
        String[] itemNames = items.split(",");
        List<HomeItemProxy> result = new ArrayList<HomeItemProxy>(itemNames.length);
        for (String name : itemNames) {
            HomeItemProxy item = server.openInstance(name);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}
