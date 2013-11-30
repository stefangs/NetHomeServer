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

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * Created by Stefan 2013-11-21
 */
public class SendQueueTest {

    SendQueue queue;
    RawMessage message1;
    RawMessage message2;
    RawMessage message3;

    @Before
    public void setUp() throws Exception {
        queue = new SendQueue(100);
        message1 = new RawMessage(null, 1);
        message2 = new RawMessage(null, 2);
        message3 = new RawMessage(null, 3);
    }

    @Test
    public void sendsIfNoQueue() {
        assertThat(queue.newMessage(message1), is(message1));
    }

    @Test
    public void doesNotSendIfQueue() {
        queue.newMessage(message1);
        assertThat(queue.newMessage(message2),  is(nullValue()));
    }

    @Test
    public void sendIfQueueAndMessageTimedOut() throws InterruptedException {
        queue.newMessage(message1);
        Thread.sleep(200);
        assertThat(queue.newMessage(message2), is(message2));
    }

    @Test
    public void ackReturnsNullIfNoMessages() {
        assertThat(queue.messageAcknowledge(), is(nullValue()));
    }

    @Test
    public void ackReturnsNullIfOneMessages() {
        queue.newMessage(message1);
        assertThat(queue.messageAcknowledge(), is(nullValue()));
    }

    @Test
    public void ackReturnsMessageInQueue() {
        queue.newMessage(message1);
        queue.newMessage(message2);
        assertThat(queue.messageAcknowledge(), is(message2));
    }

    @Test
    public void ackReturnsMessagesInQueue() {
        queue.newMessage(message1);
        queue.newMessage(message2);
        queue.newMessage(message3);
        assertThat(queue.messageAcknowledge(), is(message2));
        assertThat(queue.messageAcknowledge(), is(message3));
    }

    @Test
    public void flushClearsQueue() {
        queue.newMessage(message1);
        queue.newMessage(message2);
        queue.newMessage(message3);
        queue.flush();
        assertThat(queue.messageAcknowledge(), is(nullValue()));
    }

    @Test
    public void discardsOldMEssages() {
        RawMessage oldMessage = new RawMessage(new int[0], 4, new Date(System.currentTimeMillis() - 10000L));
        queue.newMessage(message1);
        queue.newMessage(oldMessage);
        queue.newMessage(message3);
        assertThat(queue.messageAcknowledge(), is(message3));
        assertThat(queue.messageAcknowledge(), is(nullValue()));
    }

}