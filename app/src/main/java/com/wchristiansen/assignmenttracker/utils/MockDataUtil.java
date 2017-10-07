package com.wchristiansen.assignmenttracker.utils;

import android.support.annotation.Nullable;

import com.wchristiansen.assignmenttracker.helpers.DatabaseHelper;
import com.wchristiansen.assignmenttracker.models.Assignment;
import com.wchristiansen.assignmenttracker.models.Course;
import com.wchristiansen.assignmenttracker.models.SubAssignment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author will
 * @version 9/27/17
 */
public class MockDataUtil {

    public static List<Course> generateMockCourses() {
        return generateMockCourses(null);
    }

    /**
     * Generates mock data that can be used without a database
     * @param databaseHelper database helper that will save the mock data to the SQLite database if
     *                       the helper is not null. Useful when starting a new database with data
     * @return list of mock Course data
     */
    public static List<Course> generateMockCourses(@Nullable DatabaseHelper databaseHelper) {
        List<Course> courseList = new ArrayList<>();

        Course c1 = new Course("Project Management");
        c1.setAssignmentList(getMockAssignments(c1, false));
        c1.setNickname("PM");
        if(databaseHelper != null) {
            c1 = databaseHelper.insertOrUpdateCourse(c1);
        }
        courseList.add(c1);

        Course c2 = new Course("Software Architecture");
        c2.setAssignmentList(getMockAssignments(c2, true));
        if(databaseHelper != null) {
            c2 = databaseHelper.insertOrUpdateCourse(c2);
        }
        courseList.add(c2);

        Course c3 = new Course("Seminar");
        c3.setAssignmentList(getMockAssignments(c3, false));
        if(databaseHelper != null) {
            c3 = databaseHelper.insertOrUpdateCourse(c3);
        }
        courseList.add(c3);

        return courseList;
    }

    private static List<Assignment> getMockAssignments(Course course, boolean all) {
        List<Assignment> mockAssignments = new ArrayList<>();

        final Calendar c = Calendar.getInstance();
        if(all) {
            c.set(2017, 9, 4);

            Assignment a1 = new Assignment(course, "Assignment 1", false, c.getTime());
            a1.addSubAssignment(new SubAssignment(-1, a1, "Sub Assignment 1", true, false));
            a1.addSubAssignment(new SubAssignment(-1, a1, "Sub Assignment 2", true, false));
            a1.addSubAssignment(new SubAssignment(-1, a1, "Sub Assignment 3", false, false));
            a1.addSubAssignment(new SubAssignment(-1, a1, "Sub Assignment 4", false, false));

            mockAssignments.add(a1);

            c.set(2017, 9, 11);
            Assignment a2 = new Assignment(course, "Assignment 2", false, c.getTime());
            a2.addSubAssignment(new SubAssignment(-1, a2, "Sub Assignment 1", true, false));
            a2.addSubAssignment(new SubAssignment(-1, a2, "Sub Assignment 2", false, false));
            a2.addSubAssignment(new SubAssignment(-1, a2, "Sub Assignment 3", true, false));
            a2.addSubAssignment(new SubAssignment(-1, a2, "Sub Assignment 4", false, false));
            a2.addSubAssignment(new SubAssignment(-1, a2, "Sub Assignment 5", true, false));

            mockAssignments.add(a2);
        }

        c.set(2017, 9, 11);
        Assignment a3 = new Assignment(course, "Assignment 3", false, c.getTime());
        a3.addSubAssignment(new SubAssignment(-1, a3, "Sub Assignment 1", true, false));
        a3.addSubAssignment(new SubAssignment(-1, a3, "Sub Assignment 2", false, false));

        mockAssignments.add(a3);

        return mockAssignments;
    }
}
