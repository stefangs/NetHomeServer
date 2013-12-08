package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.Event;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ItemEvent {
    private Event event;
    private String content;
    private Date received;
    private long id;
    private boolean wasHandled;
    private static Long idCounter = new Long(0);
    private static String ignoredAttributeNames[] = {"Type", "UPM.SequenceNumber", "Direction", "Value", "UPM.Primary", "UPM.Secondary", "UPM.LowBattery"};
    private static Set<String> ignoredAttributes = new HashSet<String>(Arrays.asList(ignoredAttributeNames));

    public ItemEvent(Event event, boolean wasHandled) {
        id = getNewId();
        updateEvent(event);
        content = extractContent(event);
        this.wasHandled = wasHandled;
    }

    public void updateEvent(Event event) {
        this.event = event;
        received = new Date();
    }

    private static long getNewId() {
        synchronized (idCounter) {
            idCounter++;
            return idCounter;
        }
    }

    public static String extractContent(Event event) {
        String divider="";
        StringBuilder result = new StringBuilder();
        result.append(stripProtocolSuffix(event.getAttribute("Type")));
        result.append(":");
        for (String attributeName : event.getAttributeNames()) {
            String value = event.getAttribute(attributeName);
            if (!isAttributeIgnored(attributeName, value)) {
                result.append(divider);
                result.append(stripNamePrefix(attributeName));
                result.append("=");
                result.append(value);
                divider = ",";
            }
        }
        return result.toString();
    }

    private static String stripProtocolSuffix(String type) {
        int index = type.indexOf("_");
        if (index > 0 && index < type.length() - 1) {
            return type.substring(0, index);
        }
        return type;
    }

    private static String stripNamePrefix(String attributeName) {
        int index = attributeName.indexOf(".");
        if (index > 0 && index < attributeName.length() - 1) {
            return attributeName.substring(index + 1, attributeName.length());
        }
        return attributeName;
    }

    private static boolean isAttributeIgnored(String attributeName, String value) {
        return ignoredAttributes.contains(attributeName) || value.length() == 0;
    }

    public Event getEvent() {
        return event;
    }

    public Date getReceived() {
        return received;
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public boolean getWasHandled() {
        return wasHandled;
    }
}
