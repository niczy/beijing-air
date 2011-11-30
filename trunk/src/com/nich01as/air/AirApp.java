/**
 * @(#)AirApp.java, 2011-10-24. 
 * 
 * Copyright 2011 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nich01as.air;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *
 * @author Nicholas
 *
 */
public class AirApp extends Application {
    
    private static final String PREFERENCE_UPDATE_TIME = "update_time";
    
    private static final String PREFERENCE_AQI_LEVEL = "aqi_level";
    
    private static final String PREFERENCE_AQI_VALUE = "aqi_value";
    
    private SharedPreferences mPreferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    public void updateCheckTime() {
        mPreferences.edit().putLong(PREFERENCE_UPDATE_TIME, System.currentTimeMillis()).commit();
    }
    
    public long getLastUpdateTime() {
        return mPreferences.getLong(PREFERENCE_UPDATE_TIME, 0);
    }
    
    private void setAQILevel(int level) {
        mPreferences.edit().putInt(PREFERENCE_AQI_LEVEL, level).commit();
    }
    
    public int getAQILevel() {
        return mPreferences.getInt(PREFERENCE_AQI_LEVEL, 0);
    }
    
    public void setAQIValue(int value) {
        mPreferences.edit().putInt(PREFERENCE_AQI_VALUE, value).commit();
        setAQILevel(getAQILevel(value));
    }
    
    public int getAQLValue() {
        return mPreferences.getInt(PREFERENCE_AQI_VALUE, 0);
    }
    
    private int getAQILevel(int aqiValue) {
        if (aqiValue <= 50) {
            return 0;
        } else if (aqiValue <= 100) {
            return 1;
        } else if (aqiValue <= 150) {
            return 2;
        } else if (aqiValue <= 200) {
            return 3;
        } else if (aqiValue <= 300) {
            return 4;
        } else {
            return 5;
        }
    }
    
}
