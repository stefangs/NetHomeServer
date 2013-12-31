package nu.nethome.home.items.web.servergui;

import nu.nethome.home.impl.HomeItemClassInfo;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;


public class CreationEventCacheTest {
    class NoAnnotation extends HomeItemAdapter {
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

    @HomeItemType(value = "Controls", creationEvents = "NexaL_Message")
    class EventsItem2 extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    CreationEventCache cache;
    Event nexaEvent;
    Event randomEvent;
    HomeItemInfo nexaInfo1;
    HomeItemInfo nexaInfo2;

    @Before
    public void setUp() throws Exception {
        nexaInfo1 = (HomeItemInfo)new HomeItemClassInfo(EventsItem.class);
        nexaInfo2 = (HomeItemInfo)new HomeItemClassInfo(EventsItem2.class);
        cache = new CreationEventCache();
        cache.addItemInfo(Arrays.asList(
                nexaInfo1,
                nexaInfo2,
                (HomeItemInfo) new HomeItemClassInfo(PortsItem.class),
                (HomeItemInfo) new HomeItemClassInfo(NoAnnotation.class)));
        nexaEvent = new InternalEvent("NexaL_Message");
        nexaEvent.setAttribute("Direction", "In");
        randomEvent = new InternalEvent("Random_Message");
        randomEvent.setAttribute("Direction", "In");
    }

    @Test
    public void unmappedEventGivesNoItemsTypes() {
        assertThat(cache.getItemsCreatableByEvent(randomEvent).size(), is(0));
    }

    @Test
    public void mappedEventGivesItemsTypes() {
        assertThat(cache.getItemsCreatableByEvent(nexaEvent).size(), is(2));
        assertThat(cache.getItemsCreatableByEvent(nexaEvent), hasItems(nexaInfo1, nexaInfo2));
    }

    @Test
    public void unmappedEventGivesNoItemEvent() {
        cache.newEvent(randomEvent, true);
        assertThat(cache.getItemEvents().size(), is(0));
    }

    @Test
    public void mappedEventGivesNewItemEvent() {
        cache.newEvent(nexaEvent, true);
        assertThat(cache.getItemEvents().size(), Matchers.is(1));
        assertThat(cache.getItemEvents().get(0).getEvent(), is(nexaEvent));
    }

    @Test
    public void mappedOutboundEventGivesNoItemEvent() {
        nexaEvent = new InternalEvent("NexaL_Message");
        nexaEvent.setAttribute("Direction", "Out");
        cache.newEvent(nexaEvent, true);
        assertThat(cache.getItemEvents().size(), Matchers.is(0));
    }
}
