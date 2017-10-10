package com.wchristiansen.assignmenttracker.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.wchristiansen.assignmenttracker.BaseApplication;
import com.wchristiansen.assignmenttracker.R;
import com.wchristiansen.assignmenttracker.helpers.DatabaseHelper;
import com.wchristiansen.assignmenttracker.models.AdapterItem;
import com.wchristiansen.assignmenttracker.models.Assignment;
import com.wchristiansen.assignmenttracker.models.Course;
import com.wchristiansen.assignmenttracker.models.SubAssignment;
import com.wchristiansen.assignmenttracker.utils.KeyboardUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author will
 * @version 9/27/17
 */
public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentViewHolder>
        implements AssignmentViewHolder.InteractionListener {

    private static final int VIEW_TYPE_SECTION_HEADER = 0;
    private static final int VIEW_TYPE_ASSIGNMENT = 1;
    private static final int VIEW_TYPE_SUB_ASSIGNMENT = 2;
    private static final int VIEW_TYPE_ADD_SUB_ASSIGNMENT = 3;

    private Context context;
    private DatabaseHelper databaseHelper;
    private List<AdapterItem> adapterModelList;

    private int addAssignmentAtPosition = -1;

    public AssignmentAdapter(Context context, @NonNull List<Course> courseList) {
        this.context = context;
        this.databaseHelper = BaseApplication.getDatabaseHelper();
        this.adapterModelList = new ArrayList<>();

        // Not super efficient but saves in the long term do to dealing with a flat list instead of
        // a list of Courses containing of list of Assignments containing a list of SubAssignments.
        // I came to the conclusion that it was better to take the performance hit upfront since the
        // adapter will be iterating through the data list more often than it's created
        for(Course c : courseList) {
            adapterModelList.add(c);
            for(Assignment a : c.getAssignmentList()) {
                adapterModelList.add(a);
                if(a.getSubAssignmentList() != null) {
                    adapterModelList.addAll(a.getSubAssignmentList());
                }
            }
        }
    }

    public List<Course> getCourseList() {
        List<Course> courseList = new ArrayList<>();
        for(AdapterItem item : adapterModelList) {
            if(item instanceof Course) {
                courseList.add((Course)item);
            }
        }
        return courseList;
    }

    public boolean isEditingAssignment() {
        if(addAssignmentAtPosition != -1) {
            removeEditingAssignment();
            return true;
        }
        return false;
    }

    public void addCourse(Course course) {
        course = databaseHelper.insertOrUpdateCourse(course);
        addItem(course);
    }

    public void addAssignment(Assignment assignment) {
        int index = -1;
        AdapterItem item;
        long courseId = assignment.getCourseId();
        for(int i = 0; i < adapterModelList.size(); i++) {
            item = adapterModelList.get(i);
            if(item instanceof Course && courseId == ((Course) item).getId()) {
                index = i;
                break;
            }
        }

        if(index >= 0 && index < adapterModelList.size()) {
            Course course = (Course)adapterModelList.get(index);
            for(Assignment a : course.getAssignmentList()) {
                // Count the assignment (+1) and the sub-assignments so the new assignment is added
                // to the bottom of the list
                index += a.getSubAssignmentList().size() + 1;
            }

            if(index < adapterModelList.size()) {
                course.addAssignment(assignment);
                assignment = databaseHelper.insertOrUpdateAssignment(assignment);
                addItem(assignment, index + 1);
            }
        }
    }

    public void removeAssignment(int position) {
        AdapterItem item = adapterModelList.get(position);
        if(item instanceof Assignment) {
            Assignment assignment = (Assignment)item;
            boolean result = databaseHelper.removeAssignment(assignment);
            if(result) {
                List<Assignment> toRemove = new ArrayList<>();
                toRemove.add(assignment);

                if(assignment.getSubAssignmentList() != null) {
                    toRemove.addAll(assignment.getSubAssignmentList());
                }

                adapterModelList.removeAll(toRemove);

                if(assignment.hasSubAssignments() && assignment.getSubAssignmentList().size() > 0) {
                    // Notify the system of the number of items that were removed
                    int totalRemoved = toRemove.size();
                    notifyItemRangeRemoved(position, totalRemoved);
                } else {
                    notifyItemRemoved(position);
                }
            } else {
                Toast.makeText(context, "Well shit.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addItem(AdapterItem item) {
        int index = adapterModelList.size() == 0 ? 0 : adapterModelList.size() - 1;
        addItem(item, index);
    }

    private void addItem(AdapterItem item, int index) {
        adapterModelList.add(index, item);
        notifyItemInserted(index);
    }

    @Override
    public int getItemViewType(int position) {
        AdapterItem item = adapterModelList.get(position);
        if(item instanceof Course) {
            return VIEW_TYPE_SECTION_HEADER;
        }
        else if(item instanceof SubAssignment) {
            SubAssignment s = (SubAssignment)item;
            if(s.isBeingAdded()) {
                return VIEW_TYPE_ADD_SUB_ASSIGNMENT;
            } else {
                return VIEW_TYPE_SUB_ASSIGNMENT;
            }
        }
        return VIEW_TYPE_ASSIGNMENT;
    }

    @Override
    public AssignmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewId;
        switch (viewType) {
            case VIEW_TYPE_SECTION_HEADER:
                viewId = R.layout.list_item_section_header;
                break;
            case VIEW_TYPE_ADD_SUB_ASSIGNMENT:
                viewId = R.layout.list_item_add_sub_assignment;
                break;
            case VIEW_TYPE_SUB_ASSIGNMENT:
                viewId = R.layout.list_item_sub_assignment;
                break;
            case VIEW_TYPE_ASSIGNMENT:
            default:
                viewId = R.layout.list_item_assignment;
                break;
        }
        View item = LayoutInflater.from(context).inflate(viewId, parent, false);
        return new AssignmentViewHolder(
                item,
                this,
                viewId == R.layout.list_item_assignment || viewId == R.layout.list_item_sub_assignment,
                viewId == R.layout.list_item_assignment
        );
    }

    @Override
    public void onBindViewHolder(final AssignmentViewHolder holder, int position) {
        AdapterItem item = adapterModelList.get(position);
        if(item != null) {
            String title = item.getTitle();
            if(item instanceof Assignment && ((Assignment) item).hasSubAssignments()) {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                title += " (Due " + df.format(((Assignment) item).getDueDate()) + ")";
            }
            if(item instanceof Assignment && item.isHidden()) {
                holder.setVisibility(false);
            } else {
                holder.setVisibility(true);
                if(holder.title != null) {
                    holder.title.setText(title);
                    if (holder.title instanceof CheckBox && item instanceof Assignment) {
                        ((CheckBox) holder.title).setChecked(((Assignment)item).isComplete());
                    }
                }

                if(holder.editText != null) {
                    holder.editText.setText(!TextUtils.isEmpty(title) ? title : "");
                    holder.editText.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.editText.requestFocus();
                            KeyboardUtil.showSoftKeyboard(holder.editText);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return adapterModelList.size();
    }

    @Override
    public void onViewChecked(View v, int position) {
        AdapterItem item = adapterModelList.get(position);
        if(item instanceof Assignment) {
            Assignment assignment = (Assignment)item;
            assignment.setIsComplete(!assignment.isComplete());
            notifyItemChanged(position);
            databaseHelper.insertOrUpdateAssignment((Assignment)item);

            // Verify that the parent is checked appropriately if all sub-assignments are selected
            if(item instanceof SubAssignment) {
                Assignment parent = ((SubAssignment) item).getParentAssignment();
                if(parent != null) {
                    if (!assignment.isComplete()) {
                        // Uncheck if the item was a sub-assignment in a completed parent assignment
                        parent.setIsComplete(false);
                        notifyItemChanged(adapterModelList.indexOf(parent));
                        databaseHelper.insertOrUpdateAssignment(parent);
                    }
                    else if (parent.allSubAssignmentsComplete()) {
                        // Check the parent if all of it's sub-assignments are selected
                        parent.setIsComplete(true);
                        notifyItemChanged(adapterModelList.indexOf(parent));
                        databaseHelper.insertOrUpdateAssignment(parent);
                    }
                }
            } else {
                int endPosition = position + ((Assignment) item).getSubAssignmentList().size();
                notifyItemRangeChanged(position, endPosition);

                List<AdapterItem> updateItemList = adapterModelList.subList(position, endPosition);
                databaseHelper.batchItemUpdate(updateItemList);
            }
        }
    }

    @Override
    public void onAddSubAssignment(View v, int position, boolean isNew) {
        AdapterItem item = adapterModelList.get(position);
        if(item instanceof Assignment) {
            int offset = 0;
            Assignment a = (Assignment)item;
            if (addAssignmentAtPosition != -1) {
                if(position > addAssignmentAtPosition) {
                    offset = -1;
                }

                removeEditingAssignment();
            }

            if (!(a instanceof SubAssignment)) {
                addAssignmentAtPosition = position + a.getSubAssignmentList().size() + 1 + offset;
                addItem(new SubAssignment(a), addAssignmentAtPosition);
            }else {
                addAssignmentAtPosition = position;
                ((SubAssignment)a).setIsEditing(true);

                adapterModelList.set(addAssignmentAtPosition, a);
                notifyItemChanged(addAssignmentAtPosition);
            }
        }
    }

    @Override
    public void onSaveSubAssignment(View view, String title, int position) {
        AdapterItem item = adapterModelList.get(position);
        if(item instanceof SubAssignment) {
            SubAssignment newSubAssignment = (SubAssignment) item;
            newSubAssignment.setTitle(title);
            newSubAssignment.setIsEditing(false);

            // Add the new sub-assignment to the SQLite Database
            newSubAssignment = databaseHelper.insertOrUpdateSubAssignment(newSubAssignment);
            newSubAssignment.getParentAssignment().addSubAssignment(newSubAssignment);

            adapterModelList.set(addAssignmentAtPosition, newSubAssignment);
            notifyItemChanged(addAssignmentAtPosition);

            addAssignmentAtPosition = -1;
        }
    }

    private void removeEditingAssignment() {
        AdapterItem item = adapterModelList.get(addAssignmentAtPosition);
        if(item instanceof SubAssignment) {
            // If the sub-assignment already has an item assume it was being edited and not added
            if(((SubAssignment) item).getId() != -1) {
                ((SubAssignment) item).setIsEditing(false);
                notifyItemChanged(addAssignmentAtPosition);
            } else {
                adapterModelList.remove(addAssignmentAtPosition);
                notifyItemRemoved(addAssignmentAtPosition);
            }
        }

        addAssignmentAtPosition = -1;
    }

    @Override
    public void onSectionHeaderClicked(View v, int position) {
        collapseOrExpandCourse(v, position);
    }

    @SuppressWarnings("unused")
    private void collapseOrExpandCourse(View v, int position) {
        int endIndex = position;

        AdapterItem item = adapterModelList.get(position);
        boolean isHidden = item.isHidden();
        item.setIsHidden(!isHidden);

        // TODO Determine how to animate only the one row rather than all collapsible rows
        //animateDropDownArrow(v, !isHidden);

        if(position + 1 < adapterModelList.size() - 1) {
            item = adapterModelList.get(position + 1);
            while (item instanceof Assignment) {
                item.setIsHidden(!isHidden);
                notifyItemChanged(endIndex);
                if (endIndex + 1 >= adapterModelList.size()) {
                    break;
                }
                item = adapterModelList.get(++endIndex);
            }
        }
        List<AdapterItem> updateItemList = adapterModelList.subList(position, endIndex);
        databaseHelper.batchItemUpdate(updateItemList);
    }

    @SuppressWarnings("unused")
    private void animateDropDownArrow(View v, boolean reverse) {
        if(v instanceof TextView) {
            int MAX_LEVEL = 10000;

            Drawable[] myTextViewCompoundDrawables = ((TextView)v).getCompoundDrawables();
            for (Drawable drawable : myTextViewCompoundDrawables) {
                if (drawable == null) {
                    continue;
                }

                ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "level", reverse ? MAX_LEVEL : 0, reverse ? 0 : MAX_LEVEL);
                anim.start();
            }
        }
    }
}
