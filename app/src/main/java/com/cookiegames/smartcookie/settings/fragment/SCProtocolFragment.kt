/*
 * Copyright 2014 A.C.R. Development
 */
package com.cookiegames.smartcookie.settings.fragment

import android.os.Bundle
import com.cookiegames.smartcookie.R


class SCProtocolFragment : AbstractSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_sc_protocol)
    }

}
