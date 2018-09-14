package com.zhaoyuntao.androidutils.tools;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SP {
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private SharedPreferences getSharedPreferences(Context context) {
        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences("swdk", Context.MODE_PRIVATE);
        }
        return mPreferences;
    }

    @SuppressLint("CommitPrefEdits")
    private SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        getSharedPreferences(context);
        if (mEditor == null) {
            mEditor = mPreferences.edit();
        }
        return mEditor;
    }

    public void write(String key, int num, Context context) {
        getSharedPreferencesEditor(context).putInt(key, num).commit();
    }

    public void write(String key, long num, Context context) {
        getSharedPreferencesEditor(context).putLong(key, num).commit();
    }

    public void write(String key, float num, Context context) {
        getSharedPreferencesEditor(context).putFloat(key, num).commit();
    }

    public void write(String key, String num, Context context) {
        getSharedPreferencesEditor(context).putString(key, num).commit();
    }

    public void write(String key, boolean num, Context context) {
        getSharedPreferencesEditor(context).putBoolean(key, num).commit();
    }

    public String getString(Context context, String key, String defaultvalue) {
        if (getSharedPreferences(context).contains(key)) {
            return getSharedPreferences(context).getString(key, defaultvalue);
        }
        return defaultvalue;
    }

    public int getInt(Context context, String key, int defaultvalue) {
        if (getSharedPreferences(context).contains(key)) {
            return getSharedPreferences(context).getInt(key, defaultvalue);
        } else {
            SS.e("not contains:" + key );
        }
        return defaultvalue;
    }

    public long getLong(Context context, String key, long defaultvalue) {
        if (getSharedPreferences(context).contains(key)) {
            return getSharedPreferences(context).getLong(key, defaultvalue);
        }
        return defaultvalue;
    }

    public float getFloat(Context context, String key, float defaultvalue) {
        if (getSharedPreferences(context).contains(key)) {
            return getSharedPreferences(context).getFloat(key, defaultvalue);
        }
        return defaultvalue;
    }

    public boolean getBoolean(Context context, String key, boolean defaultvalue) {
        if (getSharedPreferences(context).contains(key)) {
            return getSharedPreferences(context).getBoolean(key, defaultvalue);
        }
        return defaultvalue;
    }

    public boolean contains(Context context, String key) {
        return getSharedPreferences(context).contains(key);
    }

    public void delete(Context context, String... para) {
        for (String str : para) {
            getSharedPreferences(context).edit().remove(str).commit();
        }

    }
}
