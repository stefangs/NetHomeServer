package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreationEventCache {

    List<ItemEvent> itemEvents = new ArrayList<ItemEvent>();

    public void newEvent(Event event, boolean wasHandled) {
        if (isCreationEvent(event)) {
            String content = ItemEvent.extractContent(event);
            boolean updated = false;
            for (ItemEvent itemEvent : itemEvents) {
                if (itemEvent.getContent().equals(content)) {
                    itemEvent.updateEvent(event);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                itemEvents.add(new ItemEvent(event, wasHandled));
            }
        }
    }

    public List<ItemEvent> getItemEvents() {
        return Collections.unmodifiableList(itemEvents);
    }

    public ItemEvent getItemEvent(long id) {
        for (ItemEvent event : itemEvents) {
            if (event.getId() == id) {
                return event;
            }
        }
        return null;
    }

    private boolean isCreationEvent(Event event) {
        return isInbound(event) && isMessageType(event);
    }

    private boolean isMessageType(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).endsWith("_Message");
    }

    private boolean isInbound(Event event) {
        return event.getAttribute("Direction").equals("In");
    }
}
