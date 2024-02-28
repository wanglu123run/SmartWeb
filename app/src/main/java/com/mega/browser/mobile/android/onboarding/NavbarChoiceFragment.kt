/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Created by CookieJarApps 10/01/2020 */

package com.mega.browser.mobile.android.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mega.browser.mobile.android.AppTheme
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.di.injector
import com.mega.browser.mobile.android.preference.UserPreferences
import com.mega.browser.mobile.android.search.SearchEngineProvider
import javax.inject.Inject


class NavbarChoiceFragment : Fragment() {
    @Inject
    lateinit var searchEngineProvider: SearchEngineProvider

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var checkBox: CheckBox

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.navbar_choice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var col: Int
        var textCol: Int

        when (userPreferences.useTheme) {
            AppTheme.LIGHT ->{
                col = Color.WHITE
                textCol = Color.BLACK
            }
            AppTheme.DARK ->{
                textCol = Color.WHITE
                col = Color.BLACK
            }
            AppTheme.BLACK ->{
                textCol = Color.WHITE
                col = Color.BLACK
            }
        }

        requireView().setBackgroundColor(col)
        requireView().findViewById<TextView>(R.id.permissionsTitle).setTextColor(textCol)

        val rGroup = getView()?.findViewById(R.id.radioGroup) as RadioGroup
        rGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.defaultNavbar -> userPreferences.bottomBar = false
                R.id.defaultNavbar2nd -> {
                    userPreferences.bottomBar = false
                    userPreferences.navbar = true
                }
                R.id.bottomNavbar -> userPreferences.bottomBar = true
            }
        }
        val rGroup2 = getView()?.findViewById(R.id.radioGroup2) as RadioGroup
        rGroup2.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.defaultTabs -> userPreferences.showTabsInDrawer = true
                R.id.fullTabs -> userPreferences.showTabsInDrawer = false
            }
        }
        requireView().findViewById<ImageView>(R.id.top_defaultNavbar).setOnClickListener {
            rGroup.check(R.id.defaultNavbar)
        }
        requireView().findViewById<ImageView>(R.id.both_defaultNavbar2nd).setOnClickListener {
            rGroup.check(R.id.defaultNavbar2nd)
        }
        requireView().findViewById<ImageView>(R.id.bottom_bottomNavbar).setOnClickListener {
            rGroup.check(R.id.bottomNavbar)
        }
        requireView().findViewById<ImageView>(R.id.drawer_defaultTabs).setOnClickListener {
            rGroup2.check(R.id.defaultTabs)
        }
        requireView().findViewById<ImageView>(R.id.strip_fullTabs).setOnClickListener {
            rGroup2.check(R.id.fullTabs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    companion object {
        fun newInstance() : NavbarChoiceFragment {
            return NavbarChoiceFragment()
        }
    }
}
