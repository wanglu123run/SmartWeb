/*
 * Copyright 2014 A.C.R. Development
 */
package com.cookiegames.smartcookie.download

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.text.format.Formatter
import android.view.View
import android.webkit.DownloadListener
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.cookiegames.smartcookie.R
import com.cookiegames.smartcookie.database.downloads.DownloadsRepository
import com.cookiegames.smartcookie.di.injector
import com.cookiegames.smartcookie.dialog.BrowserDialog.setDialogSize
import com.cookiegames.smartcookie.log.Logger
import com.cookiegames.smartcookie.preference.UserPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import javax.inject.Inject


class LightningDownloadListener(context: Activity) : DownloadListener {
    private val mActivity: Activity

    @JvmField
    @Inject
    var userPreferences: UserPreferences? = null

    @JvmField
    @Inject
    var downloadHandler: DownloadHandler? = null

    @JvmField
    @Inject
    var downloadsRepository: DownloadsRepository? = null

    @JvmField
    @Inject
    var logger: Logger? = null
    override fun onDownloadStart(url: String, userAgent: String,
                                 contentDisposition: String, mimetype: String, contentLength: Long) {

        XXPermissions.with(mActivity)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
//                        toast("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
//                    toast("获取录音和日历权限成功")
                    callM(url, userAgent, contentDisposition, mimetype, contentLength)
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
//                        toast("被永久拒绝授权，请手动授予录音和日历权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(mActivity, permissions)
                    } else {
//                        toast("获取录音和日历权限失败")
                    }
                }
            })
    }

    private fun callM(url: String, userAgent: String,
                      contentDisposition: String, mimetype: String, contentLength: Long) {
        val fileName = DownloadHandler.getFileNameFromURL(url, contentDisposition, mimetype)
        val downloadSize: String = if (contentLength > 0) {
            Formatter.formatFileSize(mActivity, contentLength)
        } else {
            mActivity.getString(R.string.unknown_size)
        }
        val checkBoxView = View.inflate(mActivity, R.layout.download_dialog, null)
        val checkBox = checkBoxView.findViewById<View>(R.id.checkbox) as CheckBox
        checkBox.setOnCheckedChangeListener { buttonView, isChecked -> userPreferences!!.showDownloadConfirmation = !isChecked }
        checkBox.text = mActivity.resources.getString(R.string.dont_ask_again)
        val dialogClickListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> downloadHandler!!.onDownloadStart(mActivity, userPreferences!!, url, userAgent, contentDisposition, mimetype, downloadSize)
                DialogInterface.BUTTON_NEUTRAL -> {
                    val clipboard = getSystemService(mActivity, ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(ClipData.newPlainText("", url))
                    Toast.makeText(mActivity, mActivity.resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }
        if (userPreferences!!.showDownloadConfirmation) {
            val builder = MaterialAlertDialogBuilder(mActivity) // dialog
            val message = mActivity.getString(R.string.dialog_download, downloadSize)
            val dialog: Dialog = builder.setTitle(fileName)
                .setMessage(message)
                .setView(checkBoxView)
                .setPositiveButton(mActivity.resources.getString(R.string.action_download),
                    dialogClickListener)
                .setNeutralButton(R.string.action_copy,
                    dialogClickListener)
                .setNegativeButton(mActivity.resources.getString(R.string.action_cancel),
                    dialogClickListener).show()
            setDialogSize(mActivity, dialog)
            logger!!.log(TAG, "Downloading: $fileName")
        } else {
            Toast.makeText(mActivity, mActivity.resources.getString(R.string.download_pending), Toast.LENGTH_LONG).show()
            downloadHandler!!.onDownloadStart(mActivity, userPreferences!!, url, userAgent, contentDisposition, mimetype, downloadSize)
        }

    }

    companion object {
        private const val TAG = "LightningDownloader"
    }

    init {
        context.injector.inject(this)
        mActivity = context
    }
}