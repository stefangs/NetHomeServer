/*
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

package nu.nethome.home.impl;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by Stefan 2013-12-25
 */
public class HomeItemClassInfoTest {

    class NoAnnotation extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    @HomeItemType("Ports")
    class PortsItem extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    @HomeItemType(value = "Controls", creationEvents = "NexaL_Message")
    class EventsItem extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    HomeItemInfo noAnno;
    HomeItemInfo portsItem;
    HomeItemInfo eventsItem;

    @Before
    public void setUp() throws Exception {
        noAnno = new HomeItemClassInfo(NoAnnotation.class);
        portsItem = new HomeItemClassInfo(PortsItem.class);
        eventsItem = new HomeItemClassInfo(EventsItem.class);
    }

    @Test
    public void noAnnotationHasClassName() {
        assertThat(noAnno.getClassName(), is("NoAnnotation"));
    }

    @Test
    public void noAnnotationGivesLampCategory() {
        assertThat(noAnno.getCategory(), is("Lamps"));
    }

    @Test
    public void noAnnotationGivesEmptyEventsList() {
        assertThat(noAnno.getCreationEventTypes().length, is(0));
    }

    @Test
    public void withAnnotationHasClassName() {
        assertThat(portsItem.getClassName(), is("PortsItem"));
    }

    @Test
    public void withAnnotationGivesSpecifiedCategory() {
        assertThat(portsItem.getCategory(), is("Ports"));
    }

    @Test
    public void annotationWithoutEventsGivesEmptyEventsList() {
        assertThat(portsItem.getCreationEventTypes().length, is(0));
    }

    @Test
    public void canReadEventsFromAnnotation() {
        assertThat(eventsItem.getCreationEventTypes().length, is(1));
        assertThat(eventsItem.getCreationEventTypes()[0], is("NexaL_Message"));
    }
}
