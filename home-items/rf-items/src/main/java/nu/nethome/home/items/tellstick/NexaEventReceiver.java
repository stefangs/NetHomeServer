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

package nu.nethome.home.items.tellstick;

import nu.nethome.coders.decoders.NexaDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;

public class NexaEventReceiver implements TellstickEventReceiver {

    NexaDecoder decoder = new NexaDecoder();

    public NexaEventReceiver(ProtocolDecoderSink sink) {
        decoder.setTarget(sink);
    }

    @Override
    public void processEvent(TellstickEvent event) {
        long tellstickData = event.getData();
        long tellstickBitmask = 1;
        int nethomeData = 0;
        int nethomeBitmask = 2;
        for (int i = 0; i < 12; i++) {
            if ((tellstickData & tellstickBitmask) != 0) {
                nethomeData |= nethomeBitmask;
            }
            tellstickBitmask <<= 1;
            nethomeBitmask <<= 2;
        }
        decoder.decodeMessage(nethomeData);
    }

    @Override
    public String getEventType() {
        return "protocol:arctech;model:codeswitch";
    }
}
