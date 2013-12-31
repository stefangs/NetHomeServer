package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.system.Event;

import java.util.*;

public class CreationEventCache {

    List<ItemEvent> itemEvents = new ArrayList<ItemEvent>();
    Map<String, List<HomeItemInfo>> itemsFromEvents = new HashMap<String, List<HomeItemInfo>>();

    public void addItemInfo(List<HomeItemInfo> itemInfos) {
        for (HomeItemInfo info : itemInfos) {
            for (String eventType : info.getCreationEventTypes()) {
                List<HomeItemInfo> mappedInfo = itemsFromEvents.get(eventType);
                if (mappedInfo == null) {
                    mappedInfo = new ArrayList<HomeItemInfo>();
                    itemsFromEvents.put(eventType, mappedInfo);
                }
                mappedInfo.add(info);
            }
        }
    }

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

    public List<HomeItemInfo> getItemsCreatableByEvent(Event event) {
        List<HomeItemInfo> result = this.itemsFromEvents.get(event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
        if (result != null) {
            return Collections.unmodifiableList(result);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isCreationEvent(Event event) {
        return isInbound(event) && getItemsCreatableByEvent(event).size() > 0;
    }

    private boolean isInbound(Event event) {
        return event.getAttribute("Direction").equals("In");
    }
}
