package com.revonline.pastebin;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Marco on 17/01/14.
 */
public class Settings extends PreferenceActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }


}