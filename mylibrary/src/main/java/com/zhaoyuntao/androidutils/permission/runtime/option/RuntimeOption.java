package com.zhaoyuntao.androidutils.permission.runtime.option;

import androidx.annotation.NonNull;

import com.zhaoyuntao.androidutils.permission.runtime.PermissionDef;
import com.zhaoyuntao.androidutils.permission.runtime.PermissionRequest;
import com.zhaoyuntao.androidutils.permission.runtime.setting.SettingRequest;

/**
 */
public interface RuntimeOption {

    /**
     * One or more permissions.
     */
    PermissionRequest permission(@NonNull @PermissionDef String... permissions);

    /**
     * One or more permission groups.
     *
     * @param groups use constants in {@link Permission.Group}.
     */
    PermissionRequest permission(@NonNull String[]... groups);

    /**
     * Permission settings.
     */
    SettingRequest setting();
}