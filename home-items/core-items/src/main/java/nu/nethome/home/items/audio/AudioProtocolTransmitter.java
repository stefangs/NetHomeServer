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

package nu.nethome.home.items.audio;

import nu.nethome.coders.encoders.Encoders;
import nu.nethome.coders.encoders.ShortBeepEncoder;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.BadMessageException;
import nu.nethome.util.ps.Message;
import nu.nethome.util.ps.ProtocolEncoder;
import nu.nethome.util.ps.impl.AudioPulsePlayer;

import javax.sound.sampled.Mixer.Info;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * 
 * AudioProtocolTransmitter acts as a transmitter port for pulse protocol events.
 * It transmits the events via a sound port and requires external hardware for
 * converting the audio data to RF or IR signals. 
 * 
 * @author Stefan
 */
@Plugin
public class AudioProtocolTransmitter extends HomeItemAdapter implements HomeItem {

    private final String m_Model_1 = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"AudioProtocolTransmitter\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"SourceNumber\" Type=\"String\" Init=\"setSourceName\" />"
            + "  <Attribute Name=\"TargetName\" Type=\"StringList\" Get=\"getSourceName\" 	Set=\"setSourceName\" >");

    private final String m_Model_2 = ("</Attribute>"
            + "  <Attribute Name=\"ActiveTarget\" Type=\"String\" Get=\"getActiveTarget\" />"
            + "  <Attribute Name=\"FlankSwing\" Type=\"String\" Get=\"getFlankSwing\" 	Set=\"setFlankSwing\" />"
            + "  <Action Name=\"PlayTestBeep\"		Method=\"playTestBeep\" />"
            + "  <Attribute Name=\"TestBeepFrequency\" Type=\"String\" Get=\"getTestBeepFrequency\" 	Set=\"setTestBeepFrequency\" />"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\"  Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(AudioProtocolTransmitter.class.getName());
    protected Timer m_Timer = new Timer("AudioProtocolTransmitterRestarter");
    protected static long RESTART_INTERVAL = 1000 * 60 * 60 * 6; // 6 hours
    private EncoderFactory factory;


    // Public attributes
    protected int m_TestBeepFrequency = 2000;
    protected String m_Attribute5 = "5";
    protected AudioPulsePlayer m_PulsePlayer = new AudioPulsePlayer();
    protected String m_SourceName = "";


    public AudioProtocolTransmitter() {
        factory = new EncoderFactory(Encoders.getAllTypes());
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
      */
    public boolean receiveEvent(Event event) {
        if (!event.getAttribute("Direction").equals("Out")) {
            return false;
        }
        ProtocolEncoder foundEncoder = factory.getEncoder(event);
        if (foundEncoder != null) {
            try {
                Message parameters = factory.extractMessage(event);
                int repeat = factory.calculateRepeat(event, foundEncoder);
                m_PulsePlayer.playMessage(foundEncoder.encode(parameters, ProtocolEncoder.Phase.FIRST),
                        foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED), repeat);
                return true;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            }
        }
        return false;
    }

    public String getModel() {
        StringBuilder result = new StringBuilder(m_Model_1);
        result.append("<item>");
        result.append(getSourceName());
        result.append("</item>");
        for (Info source : m_PulsePlayer.getSourceList()) {
            result.append("<item>");
            result.append(source.getName().replaceAll(" ", "&#032;"));
            result.append("</item>");
        }
        result.append(m_Model_2);
        return result.toString();
    }

    /* Activate the instance
      * @see ssg.home.HomeItem#activate()
      */
    public void activate() {
        m_PulsePlayer.openLine();

        // Start the restart timer. On the Windows platform it seems that after a while
        // the sending line stops to work. The only way I have found
        // to handle this is to simply restart the line on even intervals as this seems
        // to remove the problem.
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            m_Timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (m_PulsePlayer.isOpen()) {
                                m_PulsePlayer.closeLine();
                                m_PulsePlayer.openLine();
                            }
                            logger.fine("Restarting audio sampling");
                        }
                    }, RESTART_INTERVAL, RESTART_INTERVAL);
        }
        factory.addEncoderTypes(server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class));
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        // Stop the timer
        m_Timer.cancel();

        // Stop the sampler
        if (m_PulsePlayer.isOpen()) {
            m_PulsePlayer.closeLine();
        }
    }

    /**
     * Play a short test beep to the audio output line. This is used for testing
     * with a speaker or phones attached to the audio output.
     */
    public void playTestBeep() {
        ShortBeepEncoder beep = new ShortBeepEncoder();
        beep.setFrequency(m_TestBeepFrequency);
        beep.setDuration(0.2F);
        m_PulsePlayer.playMessage(beep.encode());
    }

    /**
     * @return Returns the SourceNumber.
     */
    public String getSourceNumber() {
        return Integer.toString(m_PulsePlayer.getSource());
    }

    /**
     * @param SourceNumber The SourceNumber to set.
     */
    public void setSourceNumber(String SourceNumber) {
        m_PulsePlayer.setSource(Integer.parseInt(SourceNumber));
        if (m_PulsePlayer.isOpen()) {
            m_PulsePlayer.closeLine();
            m_PulsePlayer.openLine();
        }
    }

    /**
     * Specify audio source. This can be specified in two ways, either the
     * source number or by the name of the source. Even if the number is
     * specified, the source name is stored and used.
     *
     * @param name source name or source number
     */
    public void setSourceName(String name) {
        Info sources[] = m_PulsePlayer.getSourceList();
        if ((name.length() == 1) &&
                (name.charAt(0) >= '0') &&
                (name.charAt(0) <= '9')) {
            m_PulsePlayer.setSource(Integer.parseInt(name));
        } else {
            for (int i = 0; i < sources.length; i++) {
                if (sources[i].getName().replaceAll("\\s+", " ").equalsIgnoreCase(name)) {
                    m_PulsePlayer.setSource(i);
                }
            }
        }
        m_SourceName = sources[m_PulsePlayer.getSource()].getName();
        if (m_PulsePlayer.isOpen()) {
            m_PulsePlayer.closeLine();
            m_PulsePlayer.openLine();
        }
    }

    public String getSourceName() {
        return m_SourceName;
    }


    /**
     * @return Returns the FlankSwing.
     */
    public String getFlankSwing() {
        return Integer.toString(m_PulsePlayer.getSwing());
    }

    /**
     * @param FlankSwing The FlankSwing to set.
     */
    public void setFlankSwing(String FlankSwing) {
        m_PulsePlayer.setSwing(Integer.parseInt(FlankSwing));
    }

    /**
     * @return Returns the Sources.
     */
    public String getSources() {
        String result = "";
        for (int i = 0; i < m_PulsePlayer.getSourceList().length; i++) {
            result += Integer.toString(i) + "=" +
                    m_PulsePlayer.getSourceList()[i].getName() + ",";
        }
        return result;
    }

    public String getSource(int nr) {
        if (nr >= m_PulsePlayer.getSourceList().length) {
            return "";
        }
        String selected = "";
        if (nr == m_PulsePlayer.getSource()) {
            selected = "->";
        }
        return selected + m_PulsePlayer.getSourceList()[nr].getName();
    }

    public String getActiveTarget() {
        if (m_PulsePlayer.isOpen()) {
            int sourceNumber = m_PulsePlayer.getSource();
            return "[" + sourceNumber + "] " + m_PulsePlayer.getSourceList()[sourceNumber].getName();
        }
        return "[No active target]";
    }

    public String getSource1() {
        return getSource(1);
    }

    public String getSource2() {
        return getSource(2);
    }

    public String getSource3() {
        return getSource(3);
    }

    public String getSource4() {
        return getSource(4);
    }

    public String getSource5() {
        return getSource(5);
    }

    /**
     * @return Returns the TestBeepFrequency.
     */
    public String getTestBeepFrequency() {
        return Integer.toString(m_TestBeepFrequency);
    }

    /**
     * @param TestBeepFrequency The TestBeepFrequency to set.
     */
    public void setTestBeepFrequency(String TestBeepFrequency) {
        m_TestBeepFrequency = Integer.parseInt(TestBeepFrequency);
    }

    public String getState() {
        return m_PulsePlayer.isOpen() ? "Connected" : "Not Connected";
    }
}
