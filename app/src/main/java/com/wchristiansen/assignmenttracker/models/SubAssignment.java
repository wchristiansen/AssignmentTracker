package com.wchristiansen.assignmenttracker.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author will
 * @version 9/27/17
 */
public class SubAssignment extends Assignment implements AdapterItem, Parcelable {

    private boolean isEditing;
    private Assignment parentAssignment;

    public SubAssignment(Assignment parentAssignment) {
        this(-1, parentAssignment, null, false, true);
    }

    public SubAssignment(long id, Assignment parentAssignment, String title, boolean isCompleted, boolean isEditing) {
        super(id, parentAssignment.getCourseId(), title, isCompleted, parentAssignment.isHidden(), parentAssignment.getDueDate());
        this.parentAssignment = parentAssignment;
        this.isEditing = isEditing;
    }

    private SubAssignment(Parcel in) {
        super(in);
        this.parentAssignment = in.readParcelable(Assignment.class.getClassLoader());
        this.isEditing = in.readByte() == 1;
    }

    public Assignment getParentAssignment() {
        return parentAssignment;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    public boolean isBeingAdded() {
        return isEditing || getCourseId() != -1 && parentAssignment != null && getTitle() == null;
    }

    @Override
    public boolean hasSubAssignments() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(parentAssignment, flags);
        out.writeByte(isEditing ? (byte)1 : 0);
    }

    public static final Parcelable.Creator<SubAssignment> CREATOR = new Parcelable.Creator<SubAssignment>() {
        public SubAssignment createFromParcel(Parcel in) {
            return new SubAssignment(in);
        }

        public SubAssignment[] newArray(int size) {
            return new SubAssignment[size];
        }
    };
}
