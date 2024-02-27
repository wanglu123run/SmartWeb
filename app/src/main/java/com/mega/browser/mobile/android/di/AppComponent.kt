package com.mega.browser.mobile.android.di

import com.mega.browser.mobile.android.BrowserApp
import com.mega.browser.mobile.android.adblock.BloomFilterAdBlocker
import com.mega.browser.mobile.android.adblock.NoOpAdBlocker
import com.mega.browser.mobile.android.browser.SearchBoxModel
import com.mega.browser.mobile.android.browser.activity.BrowserActivity
import com.mega.browser.mobile.android.browser.activity.ThemableBrowserActivity
import com.mega.browser.mobile.android.browser.bookmarks.BookmarksDrawerView
import com.mega.browser.mobile.android.device.BuildInfo
import com.mega.browser.mobile.android.dialog.LightningDialogBuilder
import com.mega.browser.mobile.android.download.LightningDownloadListener
import com.mega.browser.mobile.android.reading.activity.ReadingActivity
import com.mega.browser.mobile.android.search.SuggestionsAdapter
import com.mega.browser.mobile.android.settings.activity.SettingsActivity
import com.mega.browser.mobile.android.settings.activity.ThemableSettingsActivity
import com.mega.browser.mobile.android.view.MegaChromeClient
import com.mega.browser.mobile.android.view.MegaCookieView
import com.mega.browser.mobile.android.view.MegaWebClient
import android.app.Application
import com.mega.browser.mobile.android.download.DownloadActivity
import com.mega.browser.mobile.android.history.HistoryActivity
import com.mega.browser.mobile.android.onboarding.NavbarChoiceFragment
import com.mega.browser.mobile.android.popup.PopUpClass
import com.mega.browser.mobile.android.onboarding.Onboarding
import com.mega.browser.mobile.android.onboarding.PermsFragment
import com.mega.browser.mobile.android.onboarding.SearchEngineFragment
import com.mega.browser.mobile.android.onboarding.ThemeChoiceFragment
import com.mega.browser.mobile.android.settings.fragment.AdBlockSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.AdvancedSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.DebugSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.DisplaySettingsFragment
import com.mega.browser.mobile.android.settings.fragment.DrawerOffsetFragment
import com.mega.browser.mobile.android.settings.fragment.DrawerSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.ExportSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.ExtensionsSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.GeneralSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.HomepageSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.ParentalControlSettingsFragment
import com.mega.browser.mobile.android.settings.fragment.PrivacySettingsFragment
import com.mega.browser.mobile.android.settings.fragment.ThemeSettingsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (AppBindsModule::class)])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun buildInfo(buildInfo: BuildInfo): Builder

        fun build(): AppComponent
    }

    fun inject(activity: BrowserActivity)

    fun inject(activity: DownloadActivity)

    fun inject(activity: HistoryActivity)

    fun inject(fragment: ExportSettingsFragment)

    fun inject(builder: LightningDialogBuilder)

    fun inject(megaCookieView: MegaCookieView)

    fun inject(activity: ThemableBrowserActivity)

    fun inject(advancedSettingsFragment: AdvancedSettingsFragment)

    fun inject(app: BrowserApp)

    fun inject(activity: ReadingActivity)

    fun inject(webClient: MegaWebClient)

    fun inject(activity: SettingsActivity)

    fun inject(activity: ThemableSettingsActivity)

    fun inject(listener: LightningDownloadListener)

    fun inject(fragment: PrivacySettingsFragment)

    fun inject(fragment: DebugSettingsFragment)

    fun inject(fragment: ExtensionsSettingsFragment)

    fun inject(suggestionsAdapter: SuggestionsAdapter)

    fun inject(chromeClient: MegaChromeClient)

    fun inject(searchBoxModel: SearchBoxModel)

    fun inject(generalSettingsFragment: GeneralSettingsFragment)

    fun inject(displaySettingsFragment: DisplaySettingsFragment)

    fun inject(adBlockSettingsFragment: AdBlockSettingsFragment)

    fun inject(drawerSettingsFragment: DrawerSettingsFragment)

    fun inject(homepageSettingsFragment: HomepageSettingsFragment)

    fun inject(themeSettingsFragment: ThemeSettingsFragment)

    fun inject(drawerOffsetFragment: DrawerOffsetFragment)

    fun inject(parentalSettingsFragment: ParentalControlSettingsFragment)

    fun inject(bookmarksView: BookmarksDrawerView)

    fun provideBloomFilterAdBlocker(): BloomFilterAdBlocker

    fun provideNoOpAdBlocker(): NoOpAdBlocker

    fun inject(popUpClass: PopUpClass)

    fun inject(searchEngineFragment: SearchEngineFragment)

    fun inject(themeChoiceFragment: ThemeChoiceFragment)

    fun inject(navbarChoiceFragment: NavbarChoiceFragment)

    fun inject(permsFragmenst: PermsFragment)

    fun inject(onboarding: Onboarding)
}
