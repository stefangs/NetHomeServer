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

package nu.nethome.home.util;

import nu.nethome.home.system.Event;
import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.Message;
import nu.nethome.util.ps.ProtocolEncoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EncoderFactory {

    private static final String SUFFIX = "_Message";
    private Map<String, ProtocolEncoder> encoders = new HashMap<String, ProtocolEncoder>();

    public EncoderFactory() {
    }

    public EncoderFactory(Collection<Class<? extends ProtocolEncoder>> encoderTypes) {
        addEncoderTypes(encoderTypes);
    }

    public void addEncoderTypes(Collection<Class<? extends ProtocolEncoder>> encoderTypes) {
        for (Class<? extends ProtocolEncoder> encoderType : encoderTypes) {
            try {
                addEncoder(encoderType.newInstance());
            } catch (InstantiationException e) {
                // Nothing to do...
            } catch (IllegalAccessException e) {
                // Nothing to do...
            }
        }
    }

    public void addEncoder(ProtocolEncoder encoder) {
        encoders.put(encoder.getInfo().getName(), encoder);
    }

    public ProtocolEncoder getEncoder(Event protocolEvent) {
        String protocolName = extractProtocolName(protocolEvent.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
        return findEncoder(protocolName);
    }

    private ProtocolEncoder findEncoder(String protocolName) {
        return encoders.get(protocolName);
    }

    private String extractProtocolName(String attribute) {
        if (attribute.endsWith(SUFFIX) && attribute.length() > SUFFIX.length()) {
            return attribute.substring(0, attribute.length() - SUFFIX.length());
        }
        return null;
    }

    public Message extractMessage(Event protocolEvent) {
        EncoderMessage result = new EncoderMessage();
        String protocolName = extractProtocolName(protocolEvent.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
        if (protocolName != null) {
            protocolName += ".";
            for (String attributeName : protocolEvent.getAttributeNames()) {
                if (attributeName.startsWith(protocolName) && attributeName.length() > protocolName.length()) {
                    result.addValue(new FieldValue(
                            attributeName.substring(protocolName.length()),
                            protocolEvent.getAttribute(attributeName)));
                }
            }
        }
        return result;
    }

    public int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }
}
