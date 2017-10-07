package com.wchristiansen.assignmenttracker.adapters;

/**
 * @author will
 * @version 9/27/17
 */
public enum OptionsMenuItem {

    EDIT(0, "Edit"),
    DELETE(1, "Delete"),
    MARK_COMPLETE(2, "Mark as In/Complete");

    public int id;
    public String title;

    OptionsMenuItem(int id, String title) {
        this.id = id;
        this.title = title;
    }
}
