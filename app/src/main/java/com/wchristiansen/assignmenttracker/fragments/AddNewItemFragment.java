package com.wchristiansen.assignmenttracker.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wchristiansen.assignmenttracker.R;
import com.wchristiansen.assignmenttracker.models.Assignment;
import com.wchristiansen.assignmenttracker.models.Course;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author will
 * @version 9/27/17
 */
public class AddNewItemFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String TAG = AddNewItemFragment.class.getSimpleName();
    private static final String LAYOUT_ID = "args_layout_id";
    private static final String DIALOG_TITLE = "args_dialog_title";
    private static final String COURSE_LIST = "args_course_list";

    public interface AddItemListener {
        void onCourseAdded(Course course);
        void onAssignmentAdded(Assignment assignment);
    }

    private Date dueDate;
    private Course selectedCourse;
    private AddItemListener listener;

    public static AddNewItemFragment newInstance(@LayoutRes int layoutId,
                                                 @NonNull String dialogTitle,
                                                 @Nullable List<Course> courseList) {
        AddNewItemFragment fragment = new AddNewItemFragment();

        Bundle args = new Bundle();
        args.putInt(LAYOUT_ID, layoutId);
        args.putString(DIALOG_TITLE, dialogTitle);
        if(courseList != null) {
            args.putParcelableArrayList(COURSE_LIST, new ArrayList<>(courseList));
        }
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TODO Restore the fragment's state here
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO Save fragment state here
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AddItemListener)activity;
        } catch(ClassCastException e) {
            Log.e(TAG, "Activity must implement AddItemListener in order to receive callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getArguments().getString(DIALOG_TITLE));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        DatePickerFragment fragment =
                (DatePickerFragment)getFragmentManager().findFragmentByTag(DatePickerFragment.TAG);
        if(fragment != null) {
            fragment.setOnDateSetListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        int layoutId = getArguments().getInt(LAYOUT_ID);
        final View baseLayout = inflater.inflate(layoutId, container, false);
        if(baseLayout != null) {
            final List<Course> courseList = getArguments().getParcelableArrayList(COURSE_LIST);
            final Spinner coursePicker = baseLayout.findViewById(R.id.picker_course);
            if(coursePicker != null && courseList != null) {
                coursePicker.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, courseList));
                coursePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedCourse = (Course) adapterView.getItemAtPosition(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });
            }

            final TextView datePickerButton = baseLayout.findViewById(R.id.btn_date_picker);
            if(datePickerButton != null) {
                datePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogFragment newFragment = DatePickerFragment.newInstance(AddNewItemFragment.this);
                        newFragment.show(getFragmentManager(), DatePickerFragment.TAG);
                    }
                });
            }

            final View addCourseButton = baseLayout.findViewById(R.id.btn_add_course);
            if(addCourseButton != null) {
                addCourseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String courseName = getTextTextValue(R.id.input_course_name);
                        String courseNickname = getTextTextValue(R.id.input_course_nickname);

                        if(!TextUtils.isEmpty(courseName)) {
                            addNewCourse(courseName, courseNickname);
                        } else {
                            Toast.makeText(getActivity(), "Please enter a Course Name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            final View addAssignmentButton = baseLayout.findViewById(R.id.btn_add_assignment);
            if(addAssignmentButton != null) {
                addAssignmentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String assignmentName = getTextTextValue(R.id.input_assignment_name);
                        String errorMessage = validateAssignmentData(assignmentName, dueDate);

                        if(errorMessage == null) {
                            addNewAssignment(assignmentName, selectedCourse, dueDate);
                        } else {
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        return baseLayout;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);

        dueDate = c.getTime();

        String formattedDueDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(dueDate);
        if(getView() != null) {
            final TextView datePickerButton = getView().findViewById(R.id.btn_date_picker);
            if (datePickerButton != null) {
                datePickerButton.setText(Html.fromHtml("Due on <b>" + formattedDueDate + "</b>"));
            }
        }
    }

    private String getTextTextValue(@IdRes int viewId) {
        if(getView() != null) {
            View view = getView().findViewById(viewId);
            if(view instanceof TextView) {
                return String.valueOf(((TextView)view).getText());
            }
        }
        return null;
    }

    private String validateAssignmentData(String name, Date dueDate) {
        if(TextUtils.isEmpty(name)) {
            return "Please enter an assignment name";
        }
        else if(dueDate == null) {
            return "Please select a due date";
        }
        return null;
    }

    private void addNewCourse(String name, String nickname) {
        Course course = new Course(-1, name, nickname, false);

        if(listener != null) {
            listener.onCourseAdded(course);
        }
        dismiss();
    }

    private void addNewAssignment(String name, Course course, Date dueDate) {
        Assignment assignment = new Assignment(course, name, false, dueDate);

        if(listener != null) {
            listener.onAssignmentAdded(assignment);
        }
        dismiss();
    }
}
