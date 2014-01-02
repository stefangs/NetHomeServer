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

package nu.nethome.home.items.rfbitbanger;

import nu.nethome.coders.encoders.Encoders;
import nu.nethome.coders.encoders.ShortBeepEncoder;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.BadMessageException;
import nu.nethome.util.ps.Message;
import nu.nethome.util.ps.ProtocolEncoder;
import nu.nethome.util.ps.impl.RFBitBangerPort;

import java.util.logging.Logger;

@Plugin
@HomeItemType("Hardware")
public class RFBitBangerTransmitter extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"RFBitBangerTransmitter\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\"  Default=\"true\" />"
            + "  <Attribute Name=\"DeviceName\" Type=\"String\" Get=\"getDeviceName\" 	Set=\"setDeviceName\" />"
            + "  <Attribute Name=\"SendCount\" Type=\"String\" Get=\"getSendCount\"  />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"PlayTestBeep\"		Method=\"playTestBeep\" />"
            + "  <Attribute Name=\"TestBeepFrequency\" Type=\"String\" Get=\"getTestBeepFrequency\" 	Set=\"setTestBeepFrequency\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(RFBitBangerTransmitter.class.getName());

    // Public attributes
    private int testBeepFrequency = 2000;
    String deviceName = RFBitBangerPort.DEFAULT_DEVICE_NAME;
    RFBitBangerPort port;
    private long sendCount = 0;
    private EncoderFactory factory;
    private boolean connected = false;


    public RFBitBangerTransmitter() {
        factory = new EncoderFactory(Encoders.getAllTypes());
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
      */
    public boolean receiveEvent(Event event) {
        if (!event.getAttribute("Direction").equals("Out") || port == null) {
            return false;
        }
        ProtocolEncoder foundEncoder = factory.getEncoder(event);
        if (foundEncoder != null) {
            try {
                Message parameters = factory.extractMessage(event);
                int repeat = calculateRepeat(event, foundEncoder);
                connected = port.playMessage(foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED), repeat, 0);
                sendCount += connected ? 1 : 0;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            }
            return true;
        }
        return false;
    }

    private int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }

    public String getModel() {
        return MODEL;
    }

    public void activate(HomeService server) {
        super.activate(server);
        factory.addEncoderTypes(server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class));
        reconnect();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        port = new RFBitBangerPort(deviceName);
        connected = port.playMessage(new int[0], 0, 0);
    }

    /**
     * Play a short test beep. This is for test purposes
     */
    public void playTestBeep() {
        ShortBeepEncoder beep = new ShortBeepEncoder();
        beep.setFrequency(testBeepFrequency);
        beep.setDuration(0.05F);
        port.playMessage(beep.encode(), 10, 0);
        sendCount++;
    }

    /**
     * @return Returns the m_TestBeepFrequency.
     */
    public String getTestBeepFrequency() {
        return Integer.toString(testBeepFrequency);
    }

    /**
     * @param TestBeepFrequency The m_TestBeepFrequency to set.
     */
    public void setTestBeepFrequency(String TestBeepFrequency) {
        testBeepFrequency = Integer.parseInt(TestBeepFrequency);
    }

    public String getSendCount() {
        return Long.toString(sendCount);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getState() {
        return connected ? "Connected" : "Disconnected";
    }
}
