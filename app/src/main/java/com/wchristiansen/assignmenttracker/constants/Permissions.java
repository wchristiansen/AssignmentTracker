package com.wchristiansen.assignmenttracker.constants;

import android.Manifest;

/**
 * @author will
 * @version 10/7/17
 */
public enum Permissions {

    ReadStorage(1, Manifest.permission.READ_EXTERNAL_STORAGE);

    public int requestCode;
    public String name;

    Permissions(int requestCode, String permissionName) {
        this.requestCode = requestCode;
        this.name = permissionName;
    }
}
