/*
 * Copyright 2014 A.C.R. Development
 */
package com.mega.browser.mobile.android.settings.fragment

import android.os.Bundle
import com.mega.browser.mobile.android.R


class SCProtocolFragment : AbstractSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_sc_protocol)
    }

}
