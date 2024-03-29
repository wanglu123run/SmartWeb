package com.mega.browser.mobile.android.browser.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.iterator
import com.mega.browser.mobile.android.AppTheme
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.di.injector
import com.mega.browser.mobile.android.preference.UserPreferences
import javax.inject.Inject

abstract class ThemableBrowserActivity : AppCompatActivity() {

    // TODO reduce protected visibility
    @Inject protected lateinit var userPreferences: UserPreferences

    private var themeId: AppTheme = AppTheme.LIGHT
    private var showTabsInDrawer: Boolean = false
    private var isBottomBarShow: Boolean = false
    private var shouldRunOnResumeActions = false

    /**
     * Override this to provide an alternate theme that should be set for every instance of this
     * activity regardless of the user's preference.
     */
    @StyleRes
    protected open fun provideThemeOverride(): Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        themeId = userPreferences.useTheme
        showTabsInDrawer = userPreferences.showTabsInDrawer
        isBottomBarShow = userPreferences.bottomBar
        // set the theme
        setTheme(provideThemeOverride() ?: when (userPreferences.useTheme) {
            AppTheme.LIGHT -> R.style.Theme_LightTheme
            AppTheme.DARK -> R.style.Theme_DarkTheme
            AppTheme.BLACK -> R.style.Theme_BlackTheme
        })
        super.onCreate(savedInstanceState)

        resetPreferences()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        withStyledAttributes(attrs = intArrayOf(R.attr.iconColorState)) {
            val iconTintList = getColorStateList(0)
            menu.iterator().forEach { menuItem ->
                menuItem.icon?.let { DrawableCompat.setTintList(DrawableCompat.wrap(it), iconTintList) }
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun resetPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (userPreferences.useBlackStatusBar || !userPreferences.showTabsInDrawer) {
                window.statusBarColor = Color.BLACK
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && shouldRunOnResumeActions) {
            shouldRunOnResumeActions = false
            onWindowVisibleToUserAfterResume()
        }
    }

    /**
     * Called after the activity is resumed
     * and the UI becomes visible to the user.
     * Called by onWindowFocusChanged only if
     * onResume has been called.
     */
    protected open fun onWindowVisibleToUserAfterResume() = Unit

    override fun onResume() {
        super.onResume()
        resetPreferences()
        shouldRunOnResumeActions = true
        val drawerTabs = userPreferences.showTabsInDrawer
        if (themeId != userPreferences.useTheme
            || showTabsInDrawer != drawerTabs
            || isBottomBarShow != userPreferences.bottomBar) {
            restart()
        }
    }

    protected fun restart() {
        finish()
        startActivity(Intent(this, javaClass))
    }
}
