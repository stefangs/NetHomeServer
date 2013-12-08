package nu.nethome.home.items.web.servergui;

import javax.servlet.http.HttpServletRequest;

/**
* Created by Stefan 2013-12-08
*/
class EditItemArguments extends HomeGUIArguments {

    private final String className;
    private final String delete;
    private final String move;
    private final String room;
    private final String locationString;
    private final String newName;
    private final String saveType;

    public EditItemArguments(HttpServletRequest req) {
        super(req);
        newName = PortletPage.fromURL(req.getParameter("new_name"));
        className = PortletPage.fromURL(req.getParameter("class_name"));
        delete = req.getParameter("delete");
        move = req.getParameter("move");
        room = PortletPage.fromURL(req.getParameter("room"));
        locationString = req.getParameter("new_location");
        saveType = req.getParameter("save_type");
    }

    public boolean hasClassName() {
        return className != null;
    }

    public String getRoom() {
        return room;
    }

    public String getClassName() {
        return className;
    }

    public boolean isItemDelete() {
        return delete != null;
    }

    public boolean isItemMove() {
        return move != null;
    }

    public String getNewLocation() {
        return locationString;
    }

    public boolean hasNewLocation() {
        return locationString != null;
    }

    public String getNewName() {
        return newName;
    }

    public boolean isSaveTypeCancel() {
        return (saveType != null) && saveType.equals(EditItemPage.CANCEL_BUTTON_TEXT);
    }

    public boolean isSaveTypeThatReturns() {
        return (saveType != null) && (saveType.equals(EditItemPage.SAVE_BUTTON_TEXT) || isSaveTypeCancel());
    }

    public boolean hasRoom() {
        return room != null;
    }
}
