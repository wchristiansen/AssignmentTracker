package com.wchristiansen.assignmenttracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.wchristiansen.assignmenttracker.BaseApplication;
import com.wchristiansen.assignmenttracker.constants.Permissions;

import java.io.File;

/**
 * @author will
 * @version 9/27/17
 */
public class ImportDatabaseFragment extends DialogFragment {

    private static final String TITLE = "args_title";

    public interface OnListDialogItemSelect {
        void onListItemSelected(String selection);
    }

    private ArrayAdapter<String> adapter;
    private OnListDialogItemSelect listener;

    public static ImportDatabaseFragment newInstance(OnListDialogItemSelect listener) {
        ImportDatabaseFragment fragment = new ImportDatabaseFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, "Import Database");
        fragment.setArguments(args);
        fragment.setOnListDialogItemSelect(listener);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int permission = ContextCompat.checkSelfPermission(getActivity(), Permissions.ReadStorage.name);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            loadAvailableImports();
        } else {
            requestPermissions(
                    new String[]{ Permissions.ReadStorage.name },
                    Permissions.ReadStorage.requestCode
            );
        }

        String title = getArguments().getString(TITLE);
        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setCancelable(false)
            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int index) {
                    if(adapter != null && adapter.getCount() >= index) {
                        String selectedOption = adapter.getItem(index);
                        if (listener != null && selectedOption != null) {
                            listener.onListItemSelected(getFullBackupPath() + selectedOption);
                        }
                        getDialog().dismiss();
                        ImportDatabaseFragment.this.dismiss();
                    }
                }
            }).create();
    }

    private void loadAvailableImports() {
        adapter.clear();
        File sdDirectory = Environment.getExternalStorageDirectory();
        if (sdDirectory.canRead()) {
            File backupDB = new File(sdDirectory, BaseApplication.getDatabaseBackupPath());
            adapter.addAll(backupDB.list());
        }
        adapter.notifyDataSetChanged();
    }

    private void setOnListDialogItemSelect(OnListDialogItemSelect listener) {
        this.listener = listener;
    }

    private String getFullBackupPath() {
        File sdDirectory = Environment.getExternalStorageDirectory();
        return new File(sdDirectory, BaseApplication.getDatabaseBackupPath()).getPath() + "/";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Permissions.ReadStorage.requestCode) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAvailableImports();
            } else {
                if(getDialog() != null) {
                    getDialog().dismiss();
                }
                ImportDatabaseFragment.this.dismiss();
                Toast.makeText(getActivity(), "Unable to read backup data", Toast.LENGTH_LONG).show();
            }
        }
    }
}