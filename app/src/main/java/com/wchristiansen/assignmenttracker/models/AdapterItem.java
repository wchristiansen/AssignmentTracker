package com.wchristiansen.assignmenttracker.models;

/**
 * @author will
 * @version 9/27/17
 */
public interface AdapterItem {

    String getTitle();
    boolean isHidden();
    void setIsHidden(boolean isHidden);

}
