package com.mega.browser.mobile.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.mega.browser.mobile.android.database.bookmark.BookmarkExporter
import com.mega.browser.mobile.android.database.bookmark.BookmarkRepository
import com.mega.browser.mobile.android.device.BuildInfo
import com.mega.browser.mobile.android.device.BuildType
import com.mega.browser.mobile.android.di.AppComponent
import com.mega.browser.mobile.android.di.DaggerAppComponent
import com.mega.browser.mobile.android.di.DatabaseScheduler
import com.mega.browser.mobile.android.di.injector
import com.mega.browser.mobile.android.log.Logger
import com.mega.browser.mobile.android.preference.DeveloperPreferences
import com.mega.browser.mobile.android.utils.FileUtils
import com.mega.browser.mobile.android.utils.MemoryLeakUtils
import com.mega.browser.mobile.android.utils.installMultiDex
import android.os.StrictMode
import com.mega.browser.mobile.android.BuildConfig
import com.google.android.material.color.DynamicColors
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject
import kotlin.system.exitProcess

class BrowserApp : Application() {

    @Inject internal lateinit var developerPreferences: DeveloperPreferences
    @Inject internal lateinit var bookmarkModel: BookmarkRepository
    @Inject @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler
    @Inject internal lateinit var logger: Logger
    @Inject internal lateinit var buildInfo: BuildInfo

    lateinit var applicationComponent: AppComponent

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT < 21) {
            installMultiDex(context = base)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        if (Build.VERSION.SDK_INT >= 28) {
            if (getProcessName() == "$packageName:incognito") {
                WebView.setDataDirectorySuffix("incognito")
            }
        }

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            if (BuildConfig.DEBUG) {
                com.mega.browser.mobile.android.utils.FileUtils.writeCrashToStorage(ex)
            }

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex)
            } else {
                exitProcess(2)
            }
        }

        RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
            if (BuildConfig.DEBUG && throwable != null) {
                com.mega.browser.mobile.android.utils.FileUtils.writeCrashToStorage(throwable)
                throw throwable
            }
        }

        applicationComponent = DaggerAppComponent.builder()
                .application(this)
                .buildInfo(createBuildInfo())
                .build()
        injector.inject(this)

        Single.fromCallable(bookmarkModel::count)
                .filter { it == 0L }
                .flatMapCompletable {
                    val assetsBookmarks = BookmarkExporter.importBookmarksFromAssets(this@BrowserApp)
                    bookmarkModel.addBookmarkList(assetsBookmarks)
                }
                .subscribeOn(databaseScheduler)
                .subscribe()
        if (buildInfo.buildType == BuildType.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        registerActivityLifecycleCallbacks(object : com.mega.browser.mobile.android.utils.MemoryLeakUtils.LifecycleAdapter() {
            override fun onActivityDestroyed(activity: Activity) {
                logger.log(TAG, "Cleaning up after the Android framework")
                com.mega.browser.mobile.android.utils.MemoryLeakUtils.clearNextServedView(activity, this@BrowserApp)
            }
        })

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    /**
     * Create the [BuildType] from the [BuildConfig].
     */
    private fun createBuildInfo() = BuildInfo(when {
        BuildConfig.DEBUG -> BuildType.DEBUG
        else -> BuildType.RELEASE
    })

    companion object {
        private const val TAG = "BrowserApp"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        }
    }

}