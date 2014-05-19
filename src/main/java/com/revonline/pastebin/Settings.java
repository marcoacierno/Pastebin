package com.revonline.pastebin;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        addPreferencesFromResource(R.xml.settings);
    }
}