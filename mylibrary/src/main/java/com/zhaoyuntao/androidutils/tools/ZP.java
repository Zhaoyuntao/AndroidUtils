package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import com.zhaoyuntao.androidutils.permission.Action;
import com.zhaoyuntao.androidutils.permission.PermissionSettings;
import com.zhaoyuntao.androidutils.permission.Rationale;
import com.zhaoyuntao.androidutils.permission.RequestExecutor;
import com.zhaoyuntao.androidutils.permission.ZPermission;
import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.permission.runtime.PermissionDef;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * created by zhaoyuntao
 * on 2019-11-06
 * description:
 */
public class ZP {

    /**
     * request the specified permission if app don't has it
     *
     * @param context
     * @param requestResult
     * @param permissions
     */
    public static void p(final Context context, final PermissionSettings permissionSettings, @NonNull final RequestResult requestResult, final @NonNull @PermissionDef String... permissions) {

        if (context == null) {
            requestResult.onDenied(Arrays.asList(permissions));
            return;
        }
        if (hasPermission(context, permissions)) {
            for (String permission : permissions) {
                ZPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
            }
            requestResult.onGranted(Arrays.asList(permissions));
            return;
        }

        ZPermission.with(context)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        for (String permission : permissions) {
                            if (hasPermission(context, permission)) {
                                ZPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
                            } else {
                                ZPermission.setAlwaysDeniedPermissionBefore(context, permission, true);
                            }
                        }
                        requestResult.onGranted(permissions);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissionsList) {
                        PermissionSettings finalPermissionSettings = permissionSettings;
                        if (finalPermissionSettings == null) {
                            finalPermissionSettings = new PermissionSettings() {
                                @Override
                                public void whenDeny() {

                                }

                                @Override
                                public void whenContinue() {

                                }
                            };
                        }
                        finalPermissionSettings.setPermission(permissions);
                        if (ZPermission.hasAlwaysDeniedPermission(context, permissionsList)) {
                            finalPermissionSettings.setPermission(permissions);
                            finalPermissionSettings.setAlwaysDeny(true);
                            for (String permission : permissionsList) {
                                if (hasPermission(context, permission)) {
                                    ZPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
                                } else {
                                    ZPermission.setAlwaysDeniedPermissionBefore(context, permission, true);
                                }
                            }
                            requestResult.onDeniedNotAsk(finalPermissionSettings);
                        } else {
                            finalPermissionSettings.setAlwaysDeny(false);
                            requestResult.onDenied(permissionsList);
                        }
                    }
                }).start();

    }

    /**
     * request the apk install permission if app don't has it
     *
     * @param context
     * @param requestResult
     */
    public static void requestInstallPermission(final Context context, final File file, @NonNull final InstallRequestResult requestResult) {
        if (context == null) {
            requestResult.onDenied();
            return;
        }

        ZPermission.with(context).install().file(file).rationale(new Rationale<File>() {
            @Override
            public void showRationale(Context context, File data, RequestExecutor executor) {
                requestResult.showRationale(context, data, executor);
            }
        }).onGranted(new Action<File>() {
            @Override
            public void onAction(File data) {
                requestResult.onGranted();
            }
        }).onDenied(new Action<File>() {
            @Override
            public void onAction(File data) {
                requestResult.onDenied();
            }
        }).start();
    }

    public static void p(final Context context, @NonNull final RequestResult requestResult, final @NonNull @PermissionDef String... permissions) {
        p(context, null, requestResult, permissions);
    }

    public interface InstallRequestResult {
        void onGranted();

        void onDenied();

        void showRationale(Context context, File data, RequestExecutor executor);
    }

    /**
     * whether the app has the specified permission
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasPermission(final Context context, @NonNull @PermissionDef String... permissions) {
        if (context == null) {
            return false;
        }
        return ZPermission.hasPermissions(context, permissions);
    }

    /**
     * request a specified permission with a notice for why you want this permission
     *
     * @param context
     * @param requestResultWithNotice
     * @param permissions
     */
    public static void requestPermissionWithNotice(final Context context, @NonNull final RequestResultWithNotice requestResultWithNotice, @NonNull @PermissionDef final String... permissions) {
        if (hasPermission(context, permissions)) {
            for (String permission : permissions) {
                ZPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
            }
            requestResultWithNotice.onGranted(Arrays.asList(permissions));
        } else {
            PermissionSettings permissionSettings = new PermissionSettings() {
                @Override
                public void whenDeny() {

                }

                @Override
                public void whenContinue() {
                    p(context, requestResultWithNotice, permissions);
                }
            };
            permissionSettings.setPermission(permissions);
            permissionSettings.setAlwaysDeny(false);
            for (String permission : permissions) {
                if (!hasPermission(context, permission)) {
                    if (ZPermission.hasAlwaysDeniedPermission(context, permissions) && ZPermission.alwaysDeniedPermissionBefore(context, permission)) {
                        permissionSettings.setAlwaysDeny(true);
                    }
                }
            }
            requestResultWithNotice.onShowNotice(permissionSettings);
        }
    }

    /**
     * request camera permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestCameraPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.CAMERA);
    }

    /**
     * request camera permission with a notice
     *
     * @param context
     * @param requestResultWithNotice
     */
    public static void requestCameraPermissionWithNotice(final Context context, @NonNull final RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA);
    }

    /**
     * request record audio permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestAudioPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.RECORD_AUDIO);
    }

    /**
     * request record audio permission with notice
     *
     * @param context
     * @param requestResultWithNotice
     */
    public static void requestAudioPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.RECORD_AUDIO);
    }

    /**
     * if has permission of RECORD_AUDIO
     *
     * @param context
     * @return
     */
    public static boolean hasAudioPermission(Context context) {
        return hasPermission(context, Permission.RECORD_AUDIO);
    }

    /**
     * request write and read file permissions
     *
     * @param context
     * @param requestResult
     */
    public static void requestExternalPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestExternalPermissionWithNotice(final Context context, final RequestResultWithNotice requestResult) {
        requestPermissionWithNotice(context, requestResult, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * request read phone number permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestPhoneNumberPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.READ_PHONE_STATE);
    }

    /**
     * request read phone number permission with a notice
     *
     * @param context
     * @param requestResultWithNotice
     */
    public static void requestPhoneNumberPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.READ_PHONE_STATE);
    }

    /**
     * request read contacts permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestContactsPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.READ_CONTACTS);
    }

    /**
     * request location permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestLocationPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.ACCESS_FINE_LOCATION);
    }

    /**
     * request calendar permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestCalendarPermission(final Context context, final RequestResult requestResult) {
        p(context, requestResult, Permission.READ_CALENDAR, Permission.WRITE_CALENDAR);
    }

    public static void requestCalendarPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.READ_CALENDAR, Permission.WRITE_CALENDAR);
    }

    public static boolean hasContactsPermission(Context context) {
        return hasPermission(context, Permission.READ_CONTACTS);
    }

    public static void requestContactsPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.READ_CONTACTS);
    }

    public static boolean hasFineLocationPermission(Context context) {
        return hasPermission(context, Permission.ACCESS_FINE_LOCATION);
    }

    public static void requestFineLocationPermission(Context context, RequestResult requestResult) {
        p(context, requestResult, Permission.ACCESS_FINE_LOCATION);
    }

    public static void requestFineLocationPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Permission.CAMERA);
    }

    public static boolean hasExternalPermission(Context context) {
        return hasPermission(context, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestCameraAndAudioPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA, Permission.RECORD_AUDIO);
    }

    public static void requestCameraAndAudioAndWriteExternalPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA, Permission.RECORD_AUDIO, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }

    public static void requestBatteryWhitePermission(Context context, @NonNull BatteryRequestResult requestResult) {
        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm == null) {
                return;
            }
            boolean isInWhiteList = pm.isIgnoringBatteryOptimizations(context.getPackageName());

            if (isInWhiteList) {
                requestResult.isInWhiteList();
            } else {
                requestResult.isNotInWhiteList();
            }
        }
    }

    public interface BatteryRequestResult {
        void isInWhiteList();

        void isNotInWhiteList();
    }

    public interface RequestResult {
        void onGranted(List<String> permissions);

        void onDenied(List<String> permissions);

        void onDeniedNotAsk(PermissionSettings permissionSettings);
    }

    public interface RequestResultWithNotice extends RequestResult {

        void onShowNotice(PermissionSettings permissionSettings);
    }

}
