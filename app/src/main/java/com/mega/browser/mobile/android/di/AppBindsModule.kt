package com.mega.browser.mobile.android.di

import com.mega.browser.mobile.android.adblock.allowlist.AllowListModel
import com.mega.browser.mobile.android.adblock.allowlist.SessionAllowListModel
import com.mega.browser.mobile.android.adblock.source.AssetsHostsDataSource
import com.mega.browser.mobile.android.adblock.source.HostsDataSource
import com.mega.browser.mobile.android.adblock.source.HostsDataSourceProvider
import com.mega.browser.mobile.android.adblock.source.PreferencesHostsDataSourceProvider
import com.mega.browser.mobile.android.database.adblock.HostsDatabase
import com.mega.browser.mobile.android.database.adblock.HostsRepository
import com.mega.browser.mobile.android.database.allowlist.AdBlockAllowListDatabase
import com.mega.browser.mobile.android.database.allowlist.AdBlockAllowListRepository
import com.mega.browser.mobile.android.database.bookmark.BookmarkDatabase
import com.mega.browser.mobile.android.database.bookmark.BookmarkRepository
import com.mega.browser.mobile.android.database.downloads.DownloadsDatabase
import com.mega.browser.mobile.android.database.downloads.DownloadsRepository
import com.mega.browser.mobile.android.database.history.HistoryDatabase
import com.mega.browser.mobile.android.database.history.HistoryRepository
import com.mega.browser.mobile.android.database.javascript.JavaScriptDatabase
import com.mega.browser.mobile.android.database.javascript.JavaScriptRepository
import com.mega.browser.mobile.android.ssl.SessionSslWarningPreferences
import com.mega.browser.mobile.android.ssl.SslWarningPreferences
import dagger.Binds
import dagger.Module

/**
 * Dependency injection module used to bind implementations to interfaces.
 */
@Module
abstract class AppBindsModule {

    @Binds
    abstract fun provideBookmarkModel(bookmarkDatabase: BookmarkDatabase): BookmarkRepository

    @Binds
    abstract fun provideDownloadsModel(downloadsDatabase: DownloadsDatabase): DownloadsRepository

    @Binds
    abstract fun providesHistoryModel(historyDatabase: HistoryDatabase): HistoryRepository

    @Binds
    abstract fun providesJavaScriptModel(javaScriptDatabase: JavaScriptDatabase): JavaScriptRepository

    @Binds
    abstract fun providesAdBlockAllowListModel(adBlockAllowListDatabase: AdBlockAllowListDatabase): AdBlockAllowListRepository

    @Binds
    abstract fun providesAllowListModel(sessionAllowListModel: SessionAllowListModel): AllowListModel

    @Binds
    abstract fun providesSslWarningPreferences(sessionSslWarningPreferences: SessionSslWarningPreferences): SslWarningPreferences

    @Binds
    abstract fun providesHostsDataSource(assetsHostsDataSource: AssetsHostsDataSource): HostsDataSource

    @Binds
    abstract fun providesHostsRepository(hostsDatabase: HostsDatabase): HostsRepository

    @Binds
    abstract fun providesHostsDataSourceProvider(preferencesHostsDataSourceProvider: PreferencesHostsDataSourceProvider): HostsDataSourceProvider
}
