/*
 * Copyright 2014 A.C.R. Development
 */
package com.mega.browser.mobile.android.settings.fragment

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.mega.browser.mobile.android.MainActivity
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.bookmark.LegacyBookmarkImporter
import com.mega.browser.mobile.android.bookmark.NetscapeBookmarkFormatImporter
import com.mega.browser.mobile.android.browser.TabsManager
import com.mega.browser.mobile.android.database.bookmark.BookmarkExporter
import com.mega.browser.mobile.android.database.bookmark.BookmarkRepository
import com.mega.browser.mobile.android.di.DatabaseScheduler
import com.mega.browser.mobile.android.di.MainScheduler
import com.mega.browser.mobile.android.di.injector
import com.mega.browser.mobile.android.dialog.BrowserDialog
import com.mega.browser.mobile.android.dialog.DialogItem
import com.mega.browser.mobile.android.extensions.snackbar
import com.mega.browser.mobile.android.extensions.toast
import com.mega.browser.mobile.android.log.Logger
import com.mega.browser.mobile.android.utils.Preconditions.checkNonNull
import com.mega.browser.mobile.android.utils.Utils
import com.mega.browser.mobile.android.utils.Utils.close
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONObject
import java.io.*
import javax.inject.Inject


class ExportSettingsFragment : AbstractSettingsFragment() {

    @Inject
    internal lateinit var bookmarkRepository: BookmarkRepository

    @Inject
    internal lateinit var application: Application

    @Inject
    internal lateinit var tabModel: TabsManager

    @Inject
    internal lateinit var netscapeBookmarkFormatImporter: NetscapeBookmarkFormatImporter

    @Inject
    internal lateinit var legacyBookmarkImporter: LegacyBookmarkImporter

    @Inject
    @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler

    @Inject
    @field:MainScheduler
    internal lateinit var mainScheduler: Scheduler

    @Inject
    internal lateinit var logger: Logger

    private var importSubscription: Disposable? = null
    private var exportSubscription: Disposable? = null
    private var bookmarksSortSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)

//        PermissionsManager
//            .getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS, null)

        requestPermissions(success = {

        }, fail = {

        })


        clickablePreference(preference = SETTINGS_EXPORT, onClick = this::exportBookmarks)
        clickablePreference(preference = SETTINGS_IMPORT, onClick = this::importBookmarks)
        clickablePreference(
            preference = SETTINGS_DELETE_BOOKMARKS,
            onClick = this::deleteAllBookmarks
        )

        clickablePreference(preference = SETTINGS_TAB_EXPORT, onClick = this::exportTabs)
        clickablePreference(preference = SETTINGS_TAB_IMPORT, onClick = this::importTabs)

        clickablePreference(preference = SETTINGS_SETTINGS_EXPORT, onClick = this::exportSettings)
        clickablePreference(preference = SETTINGS_SETTINGS_IMPORT, onClick = this::importSettings)

        clickablePreference(preference = SETTINGS_DELETE_SETTINGS, onClick = this::clearSettings)
    }


    fun requestPermissions(success: ()->Unit, fail: ()->Unit) {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
//                        toast("获取部分权限成功，但部分权限未正常授予")
                        fail.invoke()
                        return
                    }
                    success.invoke()
//                    toast("获取录音和日历权限成功")
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
//                        toast("被永久拒绝授权，请手动授予录音和日历权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(requireActivity(), permissions)
                        fail.invoke()
                    } else {
//                        toast("获取录音和日历权限失败")
                        fail.invoke()
                    }
                }
            })
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_bookmarks)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        exportSubscription?.dispose()
        importSubscription?.dispose()
        bookmarksSortSubscription?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()

        exportSubscription?.dispose()
        importSubscription?.dispose()
        bookmarksSortSubscription?.dispose()
    }

    private fun importTabs() {

    }

    private fun exportTabs() {
        requestPermissions(success = {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("EXPORT_TABS", true)
            startActivity(intent)
            application.toast(R.string.save_file_success)
        }, fail = {
            val activity = activity
            if (activity != null && !activity.isFinishing && isAdded) {
                Utils.createInformativeDialog(
                    activity,
                    R.string.title_error,
                    R.string.import_bookmark_error
                )
            } else {
                application.toast(R.string.bookmark_export_failure)
            }
        })


//        PermissionsManager.getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS,
//                object : PermissionsResultAction() {
//                    override fun onGranted() {
//                        val intent = Intent(context, MainActivity::class.java)
//                        intent.putExtra("EXPORT_TABS", true)
//                        startActivity(intent)
//                        application.toast(R.string.save_file_success)
//                    }
//
//                    override fun onDenied(permission: String) {
//                        val activity = activity
//                        if (activity != null && !activity.isFinishing && isAdded) {
//                            Utils.createInformativeDialog(
//                                activity,
//                                R.string.title_error,
//                                R.string.import_bookmark_error
//                            )
//                        } else {
//                            application.toast(R.string.bookmark_export_failure)
//                        }
//                    }
//                })

    }

    private fun clearSettings() {
        val builder = MaterialAlertDialogBuilder(activity as Activity)
        builder.setTitle(getString(R.string.confirm))
        builder.setMessage(getString(R.string.clear))


        builder.setPositiveButton(resources.getString(R.string.action_ok)) { dialogInterface, which ->
            Toast.makeText(
                getActivity(),
                R.string.reset_settings, Toast.LENGTH_LONG
            ).show()

            val handler = Handler()
            handler.postDelayed(Runnable {
                (activity?.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData()
            }, 500)
        }
        builder.setNegativeButton(resources.getString(R.string.action_cancel)) { _, _ ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun exportSettings() {
        requestPermissions(success = {
            showExportSettingsDialog()
        }, fail = {
            val activity = activity
            if (activity != null && !activity.isFinishing && isAdded) {
                Utils.createInformativeDialog(
                    activity,
                    R.string.title_error,
                    R.string.action_ok
                )
            } else {
                application.toast(R.string.error)
            }
        })
//        PermissionsManager.getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS,
//                object : PermissionsResultAction() {
//                    override fun onGranted() {
//                        showExportSettingsDialog()
//                    }
//
//                    override fun onDenied(permission: String) {
//                        val activity = activity
//                        if (activity != null && !activity.isFinishing && isAdded) {
//                            Utils.createInformativeDialog(
//                                activity,
//                                R.string.title_error,
//                                R.string.action_ok
//                            )
//                        } else {
//                            application.toast(R.string.error)
//                        }
//                    }
//                })
    }

    private fun showExportSettingsDialog() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"


            putExtra(Intent.EXTRA_TITLE, "SettingsExport.txt")
        }
        settingsExportFilePicker.launch(intent)
    }

    private val settingsExportFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                result.data?.data?.let { uri ->
                    context?.contentResolver?.openOutputStream(uri)?.let { outputStream ->
                        val userPref = application.getSharedPreferences("settings", 0)
                        val allEntries: Map<String, *> = userPref!!.all

                        val exportSettingsToFile = Completable.fromAction {
                            checkNonNull(allEntries)
                            var settingsWriter: BufferedWriter? = null
                            try {
                                settingsWriter =
                                    BufferedWriter(OutputStreamWriter(outputStream))
                                val `object` = JSONObject()
                                for (entry in allEntries) {
                                    `object`.put("key", entry.key)
                                    `object`.put("value", entry.value)
                                    settingsWriter.write(`object`.toString())
                                    settingsWriter.newLine()
                                }
                            } finally {
                                close(settingsWriter)
                            }
                        }

                        exportSubscription?.dispose()
                        exportSubscription = exportSettingsToFile
                            .subscribeBy(
                                onComplete = {
                                    activity?.apply {
                                        snackbar(getString(R.string.action_ok))
                                    }
                                },
                                onError = { throwable ->
                                    logger.log(TAG, "onError: exporting settings", throwable)
                                    val activity = activity
                                    if (activity != null && !activity.isFinishing && isAdded) {
                                        Utils.createInformativeDialog(
                                            activity,
                                            R.string.title_error,
                                            R.string.error
                                        )
                                    } else {
                                        application.toast(R.string.error)
                                    }
                                }
                            )
                    }
                }
            }
        }

    private fun exportBookmarks() {
        requestPermissions(success = {
            showExportBookmarksDialog()
        }, fail = {
            val activity = activity
            if (activity != null && !activity.isFinishing && isAdded) {
                Utils.createInformativeDialog(
                    activity,
                    R.string.title_error,
                    R.string.bookmark_export_failure
                )
            } else {
                application.toast(R.string.bookmark_export_failure)
            }
        })

//        PermissionsManager.getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS,
//                object : PermissionsResultAction() {
//                    override fun onGranted() {
//                        showExportBookmarksDialog()
//                    }
//
//                    override fun onDenied(permission: String) {
//                        val activity = activity
//                        if (activity != null && !activity.isFinishing && isAdded) {
//                            Utils.createInformativeDialog(
//                                activity,
//                                R.string.title_error,
//                                R.string.bookmark_export_failure
//                            )
//                        } else {
//                            application.toast(R.string.bookmark_export_failure)
//                        }
//                    }
//                })
    }

    private fun showExportBookmarksDialog() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"


            putExtra(Intent.EXTRA_TITLE, "BookmarksExport.txt")
        }
        bookmarkExportFilePicker.launch(intent)
    }

    private val bookmarkExportFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    context?.contentResolver?.openOutputStream(uri)?.let { outputStream ->

                        bookmarksSortSubscription = bookmarkRepository.getAllBookmarksSorted()
                            .subscribeOn(databaseScheduler)
                            .subscribe { list ->
                                if (!isAdded) {
                                    return@subscribe
                                }

                                exportSubscription?.dispose()
                                exportSubscription =
                                    BookmarkExporter.exportBookmarksToFile(list, outputStream)
                                        .subscribeOn(databaseScheduler)
                                        .observeOn(mainScheduler)
                                        .subscribeBy(
                                            onComplete = {
                                                activity?.apply {
                                                    snackbar("${getString(R.string.action_ok)}")
                                                }
                                            },
                                            onError = { throwable ->
                                                logger.log(
                                                    TAG,
                                                    "onError: exporting bookmarks",
                                                    throwable
                                                )
                                                val activity = activity
                                                if (activity != null && !activity.isFinishing && isAdded) {
                                                    Utils.createInformativeDialog(
                                                        activity,
                                                        R.string.title_error,
                                                        R.string.bookmark_export_failure
                                                    )
                                                } else {
                                                    application.toast(R.string.bookmark_export_failure)
                                                }
                                            }
                                        )
                            }
                    }
                }
            }
        }

    private fun importBookmarks() {
        requestPermissions(success = {
            showImportBookmarksDialog()
        }, fail = {

        })

//        PermissionsManager.getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS,
//                object : PermissionsResultAction() {
//                    override fun onGranted() {
//                        showImportBookmarksDialog()
//                    }
//
//                    override fun onDenied(permission: String) {
//                        //TODO Show message
//                    }
//                })
    }

    private fun importSettings() {
        requestPermissions(success = {
            showImportSettingsDialog()
        }, fail = {

        })
//        PermissionsManager.getInstance()
//            .requestPermissionsIfNecessaryForResult(activity, REQUIRED_PERMISSIONS,
//                object : PermissionsResultAction() {
//                    override fun onGranted() {
//                        showImportSettingsDialog()
//                    }
//
//                    override fun onDenied(permission: String) {
//                        //TODO Show message
//                    }
//                })
    }

    private fun deleteAllBookmarks() {
        showDeleteBookmarksDialog()
    }

    private fun showDeleteBookmarksDialog() {
        BrowserDialog.showPositiveNegativeDialog(
            activity = activity as Activity,
            title = R.string.action_delete,
            message = R.string.action_delete_all_bookmarks,
            positiveButton = DialogItem(title = R.string.yes) {
                bookmarkRepository
                    .deleteAllBookmarks()
                    .subscribeOn(databaseScheduler)
                    .subscribe()
            },
            negativeButton = DialogItem(title = R.string.no) {},
            onCancel = {}
        )
    }

    private class SortName : Comparator<File> {

        override fun compare(a: File, b: File): Int {
            return if (a.isDirectory && b.isDirectory) {
                a.name.compareTo(b.name)
            } else if (a.isDirectory) {
                -1
            } else if (b.isDirectory) {
                1
            } else if (a.isFile && b.isFile) {
                a.name.compareTo(b.name)
            } else {
                1
            }
        }
    }

    private fun showImportSettingsDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/plain"
                )
            )
        }
        settingsImportFilePicker.launch(intent)
    }

    private val settingsImportFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    context?.contentResolver?.openInputStream(uri).let { inputStream ->
                        val mimeType = context?.contentResolver?.getType(uri)
                        importSubscription?.dispose()
                        importSubscription = Single.just(inputStream)
                            .map {
                                var settingsReader: BufferedReader? = null
                                try {
                                    settingsReader = BufferedReader(InputStreamReader(it))
                                    var line = settingsReader.readLine()
                                    val userPref = application.getSharedPreferences("settings", 0)
                                    userPref.edit().clear().commit()
                                    while (line != null) {
                                        val `object` = JSONObject(line)
                                        val key = `object`.getString("key")
                                        val value = `object`.getString("value")

                                        with (userPref.edit()) {
                                            if (value.matches("-?\\d+".toRegex())) {
                                                putInt(key, value.toInt())
                                            } else if (value.equals("true") || value.equals("false")) {
                                                putBoolean(key, value.toBoolean())
                                            } else {
                                                putString(key, value)
                                            }
                                            apply()
                                        }
                                        line = settingsReader.readLine()
                                    }
                                } finally {
                                    close(settingsReader)
                                }
                            }
                            .subscribeOn(databaseScheduler)
                            .observeOn(mainScheduler)
                            .subscribeBy(
                                onSuccess = { count ->
                                    activity?.apply {
                                        snackbar(getString(R.string.action_ok))
                                    }
                                },
                                onError = {
                                    logger.log(TAG, "onError: importing settings", it)
                                    val activity = activity
                                    if (activity != null && !activity.isFinishing && isAdded) {
                                        Utils.createInformativeDialog(
                                            activity,
                                            R.string.title_error,
                                            R.string.error
                                        )
                                    } else {
                                        application.toast(R.string.error)
                                    }
                                }
                            )
                    }
                }
            }
        }

    private fun showImportBookmarksDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/html",
                    "text/plain"
                )
            )
        }
        bookmarkImportFilePicker.launch(intent)
    }

    private val bookmarkImportFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    context?.contentResolver?.openInputStream(uri).let { inputStream ->
                        val mimeType = context?.contentResolver?.getType(uri)
                        importSubscription?.dispose()
                        importSubscription = Single.just(inputStream)
                            .map {
                                if (mimeType == "text/html") {
                                    netscapeBookmarkFormatImporter.importBookmarks(it)
                                } else {
                                    legacyBookmarkImporter.importBookmarks(it)
                                }
                            }
                            .flatMap {
                                bookmarkRepository.addBookmarkList(it).andThen(Single.just(it.size))
                            }
                            .subscribeOn(databaseScheduler)
                            .observeOn(mainScheduler)
                            .subscribeBy(
                                onSuccess = { count ->
                                    activity?.apply {
                                        snackbar("$count ${getString(R.string.message_import)}")
                                    }
                                },
                                onError = {
                                    logger.log(TAG, "onError: importing bookmarks", it)
                                    val activity = activity
                                    if (activity != null && !activity.isFinishing && isAdded) {
                                        Utils.createInformativeDialog(
                                            activity,
                                            R.string.title_error,
                                            R.string.import_bookmark_error
                                        )
                                    } else {
                                        application.toast(R.string.import_bookmark_error)
                                    }
                                }
                            )
                    }
                }
            }
        }

    companion object {

        private const val TAG = "BookmarkSettingsFrag"

        private const val SETTINGS_EXPORT = "export_bookmark"
        private const val SETTINGS_IMPORT = "import_bookmark"
        private const val SETTINGS_TAB_EXPORT = "export_tab"
        private const val SETTINGS_TAB_IMPORT = "import_tab"
        private const val SETTINGS_DELETE_BOOKMARKS = "delete_bookmarks"
        private const val SETTINGS_SETTINGS_EXPORT = "export_settings"
        private const val SETTINGS_SETTINGS_IMPORT = "import_settings"
        private const val SETTINGS_DELETE_SETTINGS = "clear_settings"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
