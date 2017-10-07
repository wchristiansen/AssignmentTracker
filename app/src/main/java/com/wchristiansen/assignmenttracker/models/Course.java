package com.wchristiansen.assignmenttracker.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author will
 * @version 9/27/17
 */
public class Course implements AdapterItem, Parcelable {

    private long id = -1;
    private String name;
    private String nickname;
    private boolean isHidden;
    private List<Assignment> assignmentList;

    public Course(String name) {
        this(name, new ArrayList<Assignment>());
    }


    private Course(String name, List<Assignment> assignmentList) {
        this.name = name;
        this.assignmentList = assignmentList;
    }

    public Course(long id, String name, String nickname, boolean isHidden) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.isHidden = isHidden;
        this.assignmentList = new ArrayList<>();
    }

    private Course(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.nickname = in.readString();
        this.isHidden = in.readByte() == 1;
        this.assignmentList = new ArrayList<>();
        in.readTypedList(assignmentList, Assignment.CREATOR);
    }

    public Course copyWithoutAssignmentList() {
        return new Course(id, name, nickname, isHidden);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        if(!TextUtils.isEmpty(nickname)) {
            return nickname;
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<Assignment> getAssignmentList() {
        return assignmentList;
    }

    public void setAssignmentList(List<Assignment> assignmentList) {
        this.assignmentList = new ArrayList<>();
        for(Assignment a : assignmentList) {
            a.setCourseId(id);
            this.assignmentList.add(a);
        }
    }

    public int getTotalAssignmentCount() {
        int count = 0;
        for(Assignment a : getAssignmentList()) {
            count += a.getSubAssignmentList().size() + 1;
        }
        return count;
    }

    public boolean addAssignment(Assignment assignment) {
        return assignmentList.add(assignment);
    }

    public boolean removeAssignment(Assignment assignment) {
        return assignmentList.remove(assignment);
    }

    public boolean replaceAssignment(Assignment toReplace, Assignment replacement) {
        int index = assignmentList.indexOf(toReplace);
        if(index >= 0 && index < assignmentList.size()) {
            assignmentList.remove(index);
            assignmentList.add(index, replacement);
            return true;
        }
        return false;
    }

    public List<Assignment> getAssignmentsForDate(@NonNull Date dueDate) {
        List<Assignment> dateList = new ArrayList<>();
        for(Assignment a : assignmentList) {
            if(dueDate.equals(a.getDueDate())) {
                dateList.add(a);
            }
        }
        return dateList;
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Course && id == ((Course) o).getId();
    }

    @Override
    public String toString() {
        return getName() + (TextUtils.isEmpty(getNickname()) ? "" : " (" + getNickname() + ")");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeString(nickname);
        out.writeByte(isHidden ? (byte)1 : 0);
        out.writeTypedList(assignmentList);
    }

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
