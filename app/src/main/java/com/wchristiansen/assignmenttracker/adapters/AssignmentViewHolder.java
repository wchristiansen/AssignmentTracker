package com.wchristiansen.assignmenttracker.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wchristiansen.assignmenttracker.R;
import com.wchristiansen.assignmenttracker.utils.KeyboardUtil;

/**
 * @author will
 * @version 9/27/17
 */
class AssignmentViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnCreateContextMenuListener {

    private static final String ENABLE_LONG_PRESS_MENU = "enable_long_press_menu";

    private final InteractionListener listener;

    TextView title;
    EditText editText;

    interface InteractionListener {
        void onViewChecked(View v, int position);
        void onAddSubAssignment(View v, int position, boolean isNew);
        void onSaveSubAssignment(View v, String title, int position);
        void onSectionHeaderClicked(View v, int position);
    }

    AssignmentViewHolder(final View itemView,
                         final InteractionListener listener,
                         final boolean allowLongPressActions) {
        super(itemView);
        this.listener = listener;

        itemView.setTag(allowLongPressActions ? ENABLE_LONG_PRESS_MENU : null);

        if(allowLongPressActions) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    view.setOnCreateContextMenuListener(AssignmentViewHolder.this);
                    return false;
                }
            });
        }

        title = itemView.findViewById(R.id.title);
        if(title != null) {
            title.setOnClickListener(this);
        }

        editText = itemView.findViewById(R.id.edit_sub_assignment_title);
        if(editText != null) {
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSubAssignmentSaved(v);
                        return true;
                    }
                    return false;
                }
            });
        }


        ImageView addSubAssignment = itemView.findViewById(R.id.btn_add_sub_assignment);
        if(addSubAssignment != null) {
            addSubAssignment.setOnClickListener(this);
        }

        ImageView saveSubAssignmentButton = itemView.findViewById(R.id.btn_save_sub_assignment);
        if(saveSubAssignmentButton != null) {
            saveSubAssignmentButton.setOnClickListener(this);
        }
    }

    void setVisibility(boolean isVisible) {
        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)itemView.getLayoutParams();
        if (isVisible){
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            itemView.setVisibility(View.VISIBLE);
        } else {
            itemView.setVisibility(View.GONE);
            param.height = 0;
            param.width = 0;
        }
        itemView.setLayoutParams(param);
    }

    private void onSubAssignmentSaved(View v) {
        if(listener != null && editText != null) {
            listener.onSaveSubAssignment(v, String.valueOf(editText.getText()), getAdapterPosition());
            editText.setText("");
            editText.clearFocus();
            KeyboardUtil.hideSoftKeyboard(v);
        }
    }

    @Override
    public void onClick(View view) {
        if(listener != null) {
            if(view.getId() == R.id.btn_add_sub_assignment) {
                listener.onAddSubAssignment(view, getAdapterPosition(), true);
            }
            else if(view.getId() == R.id.btn_save_sub_assignment) {
                onSubAssignmentSaved(view);
            }
            else if(!(view instanceof CheckBox) && view instanceof TextView) {
                listener.onSectionHeaderClicked(view, getAdapterPosition());
            }
            else {
                listener.onViewChecked(view, getAdapterPosition());
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu,
                                    View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.add(getAdapterPosition(), OptionsMenuItem.EDIT.id, 0, OptionsMenuItem.EDIT.title);
        contextMenu.add(getAdapterPosition(), OptionsMenuItem.DELETE.id, 0, OptionsMenuItem.DELETE.title);
        contextMenu.add(getAdapterPosition(), OptionsMenuItem.MARK_COMPLETE.id, 0, OptionsMenuItem.MARK_COMPLETE.title);
    }
}
