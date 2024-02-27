package com.mega.browser.mobile.android.settings.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mega.browser.mobile.android.R


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preferences_headers)
    }
}