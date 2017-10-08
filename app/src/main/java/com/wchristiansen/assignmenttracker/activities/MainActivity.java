package com.wchristiansen.assignmenttracker.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wchristiansen.assignmenttracker.BaseApplication;
import com.wchristiansen.assignmenttracker.R;
import com.wchristiansen.assignmenttracker.adapters.AssignmentAdapter;
import com.wchristiansen.assignmenttracker.adapters.HidingScrollListener;
import com.wchristiansen.assignmenttracker.adapters.OptionsMenuItem;
import com.wchristiansen.assignmenttracker.fragments.AddNewItemFragment;
import com.wchristiansen.assignmenttracker.fragments.ImportDatabaseFragment;
import com.wchristiansen.assignmenttracker.models.Assignment;
import com.wchristiansen.assignmenttracker.models.Course;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author will
 * @version 9/27/17
 */
public class MainActivity extends AppCompatActivity implements AddNewItemFragment.AddItemListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AssignmentAdapter assignmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Course> courseList = BaseApplication.getDatabaseHelper().getCourses();
        assignmentAdapter = new AssignmentAdapter(this, courseList);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View noCoursesView = findViewById(R.id.message_no_courses_added);
        if(noCoursesView != null) {
            noCoursesView.setVisibility(courseList.size() == 0 ? View.VISIBLE : View.GONE);
        }

        final FloatingActionButton menuFab = findViewById(R.id.btn_add_menu);
        if(menuFab != null) {
            menuFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleFabMenuOptions(true);
                }
            });
        }

        FloatingActionButton addCourseButton = findViewById(R.id.btn_add_course);
        if(addCourseButton != null) {
            addCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddItemDialogFragment(true);
                }
            });
        }

        FloatingActionButton addAssignmentButton = findViewById(R.id.btn_add_assignment);
        if(addAssignmentButton != null) {
            addAssignmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddItemDialogFragment(false);
                }
            });
        }

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if(recyclerView != null) {
            recyclerView.setAdapter(assignmentAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addOnScrollListener(new HidingScrollListener() {
                @Override
                public void onHide() {
                    hideMenuViews();
                }

                @Override
                public void onShow() {
                    showMenuViews();
                }
            });
            registerForContextMenu(recyclerView);

            // TODO Should there be a swipe option for certain view types?
            /*SwipeToDeleteHelper swipeToDeleteHelper = new SwipeToDeleteHelper(this, 0,
                    ItemTouchHelper.LEFT, R.color.bg_delete_item, R.drawable.icv_delete_white_24dp,
                    new SwipeToDeleteHelper.Callback() {
                        @Override
                        public void onItemRemoved(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                        }
                    });

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteHelper);
            itemTouchHelper.attachToRecyclerView(recyclerView);*/
        }

        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The window will contain the coordinates of the view area that is still visible
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                // Get screen height and calculate the difference with the usable area from the rect
                int height = rootView.getContext().getResources().getDisplayMetrics().heightPixels;

                // Determine if the keyboard is showing by inspecting the entire ViewTree and if it
                // is showing then hide the FAB menu buttons since room is limited
                if (height - r.bottom != 0) {
                    hideMenuViews();
                } else {
                    showMenuViews();
                }
            }
        });

        toggleFabMenuOptions(false);
    }

    @Override
    public void onBackPressed() {
        if(assignmentAdapter.isEditingAssignment()) {
            // Nothing to do here since the adapter handled it
            return;
        }
        super.onBackPressed();
    }

    //<editor-fold desc="Methods to handle Activity menu item creation and events">

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings: Nothing yet ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_backup_database) {
            backupDatabase();
            return true;
        }
        else if (id == R.id.action_import_database) {
            ImportDatabaseFragment fragment = ImportDatabaseFragment.newInstance(
                    new ImportDatabaseFragment.OnListDialogItemSelect() {
                        @Override
                        public void onListItemSelected(String selection) {
                            importBackup(selection);
                        }
                    });
            fragment.show(getSupportFragmentManager(), ImportDatabaseFragment.class.getSimpleName());
            return true;
        }
        else if (id == R.id.action_reset_database) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Database Reset?")
                    .setMessage("Are you sure you want to reset the database? This action cannot be undone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetDatabase();
                        }
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int index = item.getGroupId();
        if (OptionsMenuItem.EDIT.id == item.getItemId()) {
            assignmentAdapter.onAddSubAssignment(null, index, false);
        } else if (OptionsMenuItem.DELETE.id == item.getItemId()) {
            assignmentAdapter.removeAssignment(index);
        } else if (OptionsMenuItem.MARK_COMPLETE.id == item.getItemId()) {
            assignmentAdapter.onViewChecked(null, index);
        }

        return false;
    }

    //</editor-fold>

    //<editor-fold desc="Methods to handle dialog fragments and their callbacks">

    @Override
    public void onCourseAdded(Course course) {
        boolean showAddAssignment = false;
        if(assignmentAdapter.getCourseList().size() == 0) {
            showAddAssignment = true;
        }

        assignmentAdapter.addCourse(course);

        // Show the fancy new option here if it should be available
        if(showAddAssignment && assignmentAdapter.getCourseList().size() > 0) {
            View addAssignmentLabel = findViewById(R.id.label_add_assignment);
            FloatingActionButton addAssignmentButton = findViewById(R.id.btn_add_assignment);
            showMiniFabAndLabel(addAssignmentButton, addAssignmentLabel, true, null, true);

            View noCoursesView = findViewById(R.id.message_no_courses_added);
            if(noCoursesView != null) {
                noCoursesView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAssignmentAdded(Assignment assignment) {
        assignmentAdapter.addAssignment(assignment);
    }

    private void showAddItemDialogFragment(boolean addNewCourse) {
        AddNewItemFragment fragment = AddNewItemFragment.newInstance(
                addNewCourse ? R.layout.fragment_add_course : R.layout.fragment_add_assignment,
                "Add New " + (addNewCourse ? "Course" : "Assignment"),
                addNewCourse ? null : assignmentAdapter.getCourseList()
        );
        fragment.show(getFragmentManager(), AddNewItemFragment.TAG);
    }

    //</editor-fold>

    //<editor-fold desc="Methods to handle database modification and settings">

    private void backupDatabase() {
        try {
            File backupFile = BaseApplication.getDatabaseHelper().backupDatabase();
            Log.d(TAG, "Database backed up: " + (backupFile != null ? backupFile.getPath() : null));
            Toast.makeText(this, "Database backed up successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Database backup failed", Toast.LENGTH_LONG).show();
        }
    }

    private void importBackup(String importFileName) {
        Log.d(TAG, "Selected: " + importFileName);
        try {
            BaseApplication.getDatabaseHelper().importDatabase(importFileName);

            // Reload the database and display the newly imported data in the RecyclerView
            updateAdapterDataFromDatabase();

            Toast.makeText(this, "Database import was successful", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Database import failed", Toast.LENGTH_LONG).show();
        }
    }

    private void resetDatabase() {
        boolean successful = BaseApplication.getDatabaseHelper().resetDatabase();
        if(successful) {
            // Reload the database and display the reset database in the RecyclerView
            updateAdapterDataFromDatabase();

            Toast.makeText(this, "Database reset successfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Database reset failed", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "Database reset: " + successful);
    }

    private void updateAdapterDataFromDatabase() {
        List<Course> courseList = BaseApplication.getDatabaseHelper().getCourses();
        assignmentAdapter = new AssignmentAdapter(this, courseList);

        View noCoursesView = findViewById(R.id.message_no_courses_added);
        if(noCoursesView != null) {
            noCoursesView.setVisibility(courseList.size() == 0 ? View.VISIBLE : View.GONE);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if(recyclerView != null) {
            recyclerView.setAdapter(assignmentAdapter);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Methods to handle showing or hiding FloatingActionButton menu options">

    private void toggleFabMenuOptions(boolean animate) {
        final View addAssignmentContainer = findViewById(R.id.container_add_assignment);
        final boolean show = addAssignmentContainer.getVisibility() == View.GONE;
        toggleFabMenuOptions(show, animate);
    }

    private void toggleFabMenuOptions(final boolean show, final boolean animate) {
        final View addCourseContainer = findViewById(R.id.container_add_course);
        final View addAssignmentContainer = findViewById(R.id.container_add_assignment);

        if(show) {
            addAssignmentContainer.setVisibility(View.VISIBLE);
            addCourseContainer.setVisibility(View.VISIBLE);
        }

        AnimatorListenerAdapter animationEndListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(!show) {
                    addAssignmentContainer.setVisibility(View.GONE);
                    addCourseContainer.setVisibility(View.GONE);
                }
            }
        };

        boolean showAddAssignment = show;
        if(assignmentAdapter.getCourseList().size() == 0) {
            showAddAssignment = false;
        }

        View addAssignmentLabel = findViewById(R.id.label_add_assignment);
        FloatingActionButton addAssignmentButton = findViewById(R.id.btn_add_assignment);
        showMiniFabAndLabel(addAssignmentButton, addAssignmentLabel, showAddAssignment, animationEndListener, animate);

        View addCourseLabel = findViewById(R.id.label_add_course);
        FloatingActionButton addCourseButton = findViewById(R.id.btn_add_course);
        showMiniFabAndLabel(addCourseButton, addCourseLabel, show, animationEndListener, animate);

        FloatingActionButton parentFab = findViewById(R.id.btn_add_menu);
        ViewCompat.animate(parentFab)
                .rotation(show ? 135f : 0f)
                .withLayer()
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void showMiniFabAndLabel(final FloatingActionButton fab,
                                     final View label,
                                     final boolean show,
                                     final AnimatorListenerAdapter animatorListener,
                                     final boolean animate) {
        int viewWidth = label.getMeasuredWidth();
        label.animate()
                .setDuration(animate ? 200 : 0)
                .alpha(show ? 1f : 0f)
                .scaleX(show ? 1f : 0f)
                .scaleY(show ? 1f : 0f)
                .x(show ? 0 : viewWidth)
                .translationX(show ? 0 : viewWidth)
                .setListener(animatorListener);

        if(show) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void hideMenuViews() {
        LinearLayout container = findViewById(R.id.menu_button_container);
        FloatingActionButton menuFab = findViewById(R.id.btn_add_menu);

        Interpolator interpolator = new AccelerateInterpolator(2);
        int containerPadding = container.getPaddingBottom();

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) menuFab.getLayoutParams();
        toggleMenuFab(menuFab, lp.bottomMargin + containerPadding, interpolator, false);

        View addCourseContainer = findViewById(R.id.container_add_course);
        toggleSubMenuFab(addCourseContainer, container.getPaddingEnd(), interpolator, false);

        View addAssignmentContainer = findViewById(R.id.container_add_assignment);
        toggleSubMenuFab(addAssignmentContainer, container.getPaddingEnd(), interpolator, false);
    }

    private void showMenuViews() {
        Interpolator interpolator = new DecelerateInterpolator(2);

        FloatingActionButton menuFab = findViewById(R.id.btn_add_menu);
        toggleMenuFab(menuFab, 0, interpolator, true);

        View addCourseContainer = findViewById(R.id.container_add_course);
        toggleSubMenuFab(addCourseContainer, 0, interpolator, true);

        View addAssignmentContainer = findViewById(R.id.container_add_assignment);
        toggleSubMenuFab(addAssignmentContainer, 0, interpolator, true);
    }

    private void toggleMenuFab(View v, int padding, Interpolator interpolator, boolean show) {
        if(v != null) {
            v.animate()
                    .translationY(show ? 0 : v.getHeight() + padding)
                    .setInterpolator(interpolator)
                    .start();
        }
    }

    private void toggleSubMenuFab(View v, int padding, Interpolator interpolator, boolean show) {
        if(v != null && v.getVisibility() == View.VISIBLE) {
            int offset = show ? 0 : v.getWidth() + padding;
            v.animate()
                    .scaleX(show ? 1 : 0)
                    .scaleY(show ? 1 : 0)
                    .translationX(offset)
                    .setInterpolator(interpolator)
                    .start();
        }
    }

    //</editor-fold>
}
