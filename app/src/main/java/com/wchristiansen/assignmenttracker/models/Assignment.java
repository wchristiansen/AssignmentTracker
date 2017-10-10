package com.wchristiansen.assignmenttracker.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author will
 * @version 9/27/17
 */
public class Assignment implements AdapterItem, Parcelable {

    private long id = -1;
    private long courseId = -1;
    private String title;
    private boolean isComplete;
    private boolean isHidden;
    private Date dueDate;
    private List<SubAssignment> subAssignmentList;

    public Assignment(Course course, String title, boolean isComplete, Date dueDate) {
        this.courseId = course.getId();
        this.title = title;
        this.isComplete = isComplete;
        this.dueDate = dueDate;
        this.subAssignmentList = new ArrayList<>();
    }

    public Assignment(long id, long courseId, String title, boolean isComplete, boolean isHidden, Date dueDate) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.isHidden = isHidden;
        this.isComplete = isComplete;
        this.dueDate = dueDate;
        this.subAssignmentList = new ArrayList<>();
    }

    Assignment(Parcel in) {
        this.id = in.readLong();
        this.courseId = in.readLong();
        this.title = in.readString();
        this.isComplete = in.readByte() == 1;
        this.dueDate = (Date)in.readSerializable();
        this.subAssignmentList = new ArrayList<>();
        in.readTypedList(subAssignmentList, SubAssignment.CREATOR);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
        if(isComplete && hasSubAssignments()) {
            for(SubAssignment s : subAssignmentList) {
                s.setIsComplete(true);
            }
        }
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<SubAssignment> getSubAssignmentList() {
        return subAssignmentList;
    }

    public void setSubAssignmentList(List<SubAssignment> subAssignmentList) {
        this.subAssignmentList = new ArrayList<>(subAssignmentList);
    }

    public void addSubAssignment(SubAssignment a) {
        if(subAssignmentList == null) {
            subAssignmentList = new ArrayList<>();
        }
        subAssignmentList.add(a);
    }

    public boolean allSubAssignmentsComplete() {
        boolean allCompleted = true;
        if(subAssignmentList != null && hasSubAssignments()) {
            for (SubAssignment a : subAssignmentList) {
                if(!a.isComplete()) {
                    allCompleted = false;
                }
            }
        }
        return allCompleted;
    }

    public boolean hasSubAssignments() {
        return true;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int hashCode() {
        int result = 31 * (int)id;
        result += 31 * courseId;
        result += 31 * ((title == null) ? 0 : title .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Assignment
                && id == ((Assignment) o).getId()
                && courseId == ((Assignment)o).getCourseId()
                && TextUtils.equals(title, ((Assignment)o).getTitle());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeLong(courseId);
        out.writeString(title);
        out.writeByte(isComplete ? (byte)1 : 0);
        out.writeByte(isHidden ? (byte)1 : 0);
        out.writeSerializable(dueDate);
        out.writeTypedList(subAssignmentList);
    }

    public static final Parcelable.Creator<Assignment> CREATOR = new Parcelable.Creator<Assignment>() {
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };
}
