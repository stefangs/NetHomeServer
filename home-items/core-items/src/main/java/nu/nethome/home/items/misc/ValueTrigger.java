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

package nu.nethome.home.items.misc;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;


/**
 * ClassName
 *
 * @author Stefan
 */
@Plugin
public class ValueTrigger extends HomeItemAdapter implements HomeItem {

    private final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ValueTrigger\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" Type=\"StringList\" Get=\"getState\" Init=\"setState\" 	Default=\"true\" >"
            + "   <item>activated</item><item>deactivated</item></Attribute>"
            + "  <Attribute Name=\"LatestValue\" Type=\"String\" Get=\"getLatestValue\"  />"
            + "  <Attribute Name=\"ValueAction\" Type=\"String\" Get=\"getValueAction\" 	Set=\"setValueAction\" />"
            + "  <Attribute Name=\"Max\" Type=\"String\" Get=\"getMaxLimit\" 	Set=\"setMaxLimit\" />"
            + "  <Attribute Name=\"Min\" Type=\"String\" Get=\"getMinLimit\" 	Set=\"setMinLimit\" />"
            + "  <Attribute Name=\"ActionOnExceedingMax\" Type=\"Command\" Get=\"getPassingMaxAction\" 	Set=\"setPassingMaxAction\" />"
            + "  <Attribute Name=\"ActionOnDeceedingMin\" Type=\"Command\" Get=\"getPassingMinAction\" 	Set=\"setPassingMinAction\" />"
            + "  <Attribute Name=\"ActionWhileOverMax\" Type=\"Command\" Get=\"getActionWhileOverMax\" 	Set=\"setActionWhileOverMax\" />"
            + "  <Attribute Name=\"ActionWhileUnderMin\" Type=\"Command\" Get=\"getActionWhileUnderMin\" 	Set=\"setActionWhileUnderMin\" />"
            + "  <Action Name=\"EnableTrigger\" 	Method=\"enableTrigger\" />"
            + "  <Action Name=\"DisableTrigger\" 	Method=\"disableTrigger\" />"
            + "  <Action Name=\"CheckNow\" 	Method=\"checkValueAndTakeAction\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(ValueTrigger.class.getName());
    protected CommandLineExecutor executor;
    protected boolean activated = false;

    protected enum State {
        deactivated, normal, overMax, underMin;
    }

    // Public attributes
    protected State state = State.normal;
    protected String latestValue = "<Not read>";
    protected String valueAction = "get,OutThermometer,Temperature";
    protected Double maxLimit = 0.0;
    protected Double minLimit = 0.0;
    protected String passingMaxAction = "";
    protected String passingMinAction = "";
    protected String actionWhileOverMax = "";
    protected String actionWhileUnderMin = "";

    public ValueTrigger() {
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE) && (state != State.deactivated)
                && activated) {
            checkValueAndTakeAction();
            return true;
        }
        return false;
    }

    public void checkValueAndTakeAction() {
        Double value;
        try {
            value = getCurrentValue();
        } catch (IllegalValueException e) {
            return;
        }
        latestValue = Double.toString(value);

        switch (state) {
            case normal: {
                if (value > maxLimit) {
                    state = State.overMax;
                    performAction(passingMaxAction);
                }
                if (value < minLimit) {
                    state = State.underMin;
                    performAction(passingMinAction);
                }
                break;
            }
            case overMax: {
                if (value <= maxLimit) {
                    state = State.normal;
                }
                if (value < minLimit) {
                    state = State.underMin;
                    performAction(passingMinAction);
                }
                break;
            }
            case underMin: {
                if (value >= minLimit) {
                    state = State.normal;
                }
                if (value > maxLimit) {
                    state = State.overMax;
                    performAction(passingMaxAction);
                }
                break;
            }
        }
        if (state == State.overMax) {
            performAction(actionWhileOverMax);
        }
        if (state == State.underMin) {
            performAction(actionWhileUnderMin);
        }
    }

    private void performAction(String action) {
        String result = executor.executeCommandLine(action);
        if (!result.startsWith("ok")) {
            logger.warning(result);
        }
    }

    private Double getCurrentValue() throws IllegalValueException {
        Double resultValue;
        String result = executor.executeCommandLine(getValueAction());
        String results[] = result.split(",");
        if (results.length != 3 || !results[0].equalsIgnoreCase("ok") || results[2].length() == 0) {
            throw new IllegalValueException("Could not get value", getValueAction());
        }
        try {
            resultValue = Double.parseDouble(results[2].replace("%2C", "."));
        } catch (NumberFormatException e) {
            throw new IllegalValueException("Bad value", getValueAction());
        }
        return resultValue;
    }

    public void enableTrigger() {
        state = State.normal;
        checkValueAndTakeAction();
    }

    public void disableTrigger() {
        state = State.deactivated;
    }

    public String getModel() {
        return m_Model;
    }

    public void activate(HomeService server) {
        super.activate(server);
        activated = true;
        executor = new CommandLineExecutor(server, true);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    public String getState() {
        return state.toString();
    }

    public void setState(String state) {
        if (state.equalsIgnoreCase("deactivated")) {
            this.state = State.deactivated;
        } else {
            this.state = State.normal;
        }
    }

    public String getValueAction() {
        return valueAction;
    }

    public void setValueAction(String valueAction) {
        this.valueAction = valueAction;
    }

    public String getMaxLimit() {
        return Double.toString(maxLimit);
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = Double.parseDouble(maxLimit);
    }

    public String getMinLimit() {
        return Double.toString(minLimit);
    }

    public void setMinLimit(String minLimit) {
        this.minLimit = Double.parseDouble(minLimit);
    }

    public String getPassingMaxAction() {
        return passingMaxAction;
    }

    public void setPassingMaxAction(String passingMaxAction) {
        this.passingMaxAction = passingMaxAction;
    }

    public String getPassingMinAction() {
        return passingMinAction;
    }

    public void setPassingMinAction(String passingMinAction) {
        this.passingMinAction = passingMinAction;
    }

    public String getLatestValue() {
        return latestValue;
    }

    public String getActionWhileOverMax() {
        return actionWhileOverMax;
    }

    public void setActionWhileOverMax(String actionWhileOverMax) {
        this.actionWhileOverMax = actionWhileOverMax;
    }

    public String getActionWhileUnderMin() {
        return actionWhileUnderMin;
    }

    public void setActionWhileUnderMin(String actionWhileUnderMin) {
        this.actionWhileUnderMin = actionWhileUnderMin;
    }
}


