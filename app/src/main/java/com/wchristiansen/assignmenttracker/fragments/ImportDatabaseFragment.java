package com.wchristiansen.assignmenttracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;

import com.wchristiansen.assignmenttracker.BaseApplication;

import java.io.File;

/**
 * @author will
 * @version 9/27/17
 */
public class ImportDatabaseFragment extends DialogFragment {

    private static final String TITLE = "args_title";
    private static final String FILE_OPTIONS = "args_file_options";

    public interface OnListDialogItemSelect {
        void onListItemSelected(String selection);
    }

    private OnListDialogItemSelect listener;

    public static ImportDatabaseFragment newInstance(OnListDialogItemSelect listener) {
        ImportDatabaseFragment fragment = new ImportDatabaseFragment();

        String[] availableImports = new String[]{};
        File sdDirectory = Environment.getExternalStorageDirectory();
        if(sdDirectory.canRead()) {
            File backupDB = new File(sdDirectory, BaseApplication.getDatabaseBackupPath());
            availableImports = backupDB.list();
        }

        Bundle args = new Bundle();
        args.putString(TITLE, "Import Database");
        args.putStringArray(FILE_OPTIONS, availableImports);
        fragment.setArguments(args);
        fragment.setOnListDialogItemSelect(listener);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        final String[] options = getArguments().getStringArray(FILE_OPTIONS);
        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setCancelable(false)
            .setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    if(listener != null && options != null && options.length < index) {
                        listener.onListItemSelected(getFullBackupPath() + options[index]);
                    }
                    getDialog().dismiss();
                    ImportDatabaseFragment.this.dismiss();
                }
            }).create();
    }

    private void setOnListDialogItemSelect(OnListDialogItemSelect listener) {
        this.listener = listener;
    }

    private String getFullBackupPath() {
        File sdDirectory = Environment.getExternalStorageDirectory();
        return new File(sdDirectory, BaseApplication.getDatabaseBackupPath()).getPath() + "/";
    }
}