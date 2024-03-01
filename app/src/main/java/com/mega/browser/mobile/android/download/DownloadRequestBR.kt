package com.mega.browser.mobile.android.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.huxq17.download.Pump
import com.huxq17.download.core.DownloadListener
import com.huxq17.download.utils.LogUtil
import com.mega.browser.mobile.android.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadRequestBR : BroadcastReceiver() {

    companion object {

        const val TAG = "DownloadRqeuestBR"

        fun send(
            context: Context, downloadDirectory: String, url: String, userAgent: String,
            contentDisposition: String?, mimeType: String, contentSize: String
        ) {
            val intent = Intent()
            intent.putExtra("downloadDirectory", downloadDirectory)
            intent.putExtra("url", url)
            intent.putExtra("userAgent", userAgent)
            intent.putExtra("contentDisposition", contentDisposition)
            intent.putExtra("mimeType", mimeType)
            intent.putExtra("contentSize", contentSize)
            intent.component = ComponentName(context, DownloadRequestBR::class.java)
            context.sendBroadcast(intent)
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        val downloadDirectory = intent.getStringExtra("downloadDirectory")
        val url = intent.getStringExtra("url")
        val userAgent = intent.getStringExtra("userAgent")
        val contentDisposition = intent.getStringExtra("contentDisposition")
        val mimeType = intent.getStringExtra("mimeType")
        val contentSize = intent.getStringExtra("contentSize")
        onDownloadStartNoStream(context, downloadDirectory!!, url!!, userAgent!!, contentDisposition, mimeType!!, contentSize!!)
    }


    fun onDownloadStartNoStream(context: Context, downloadDirectory: String, url: String, userAgent: String,
                                contentDisposition: String?, mimeType: String, contentSize: String) {
        Log.e(TAG, "DOWNLOAD: Trying to download from URL: $url")
        Log.e(TAG, "DOWNLOAD: Content disposition: $contentDisposition")
        Log.e(TAG, "DOWNLOAD: MimeType: $mimeType")
        Log.e(TAG, "DOWNLOAD: User agent: $userAgent")

        var location = downloadDirectory
        location = com.mega.browser.mobile.android.utils.FileUtils.addNecessarySlashes(location)
        val downloadFolder = Uri.parse(location)

        if(url.toUri().scheme == "data"){
            Toast.makeText(context, R.string.data_scheme, Toast.LENGTH_LONG).show()
            return
        }

        val now = Date()
        val uniqid = SimpleDateFormat("ddHHmmss", Locale.US).format(now).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.download_channel)
            val description = context.getString(R.string.download_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("com.mega.browser.mobile.android.downloads", name, importance)
            channel.description = description
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val fileName = DownloadHandler.getFileNameFromURL(url, contentDisposition, mimeType)
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, "com.mega.browser.mobile.android.downloads")
        Log.d(TAG, fileName)

        builder.setContentTitle(context.getString(R.string.action_download))
            .setContentText(fileName)
            .setSmallIcon(R.drawable.ic_file_download_black)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)

        // Issue the initial notification with zero progress
        val PROGRESS_MAX = 100
        val PROGRESS_CURRENT = 0
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
        notificationManager.notify(uniqid, builder.build())

        // Open DownloadActivity
        val intent = Intent(context, DownloadActivity::class.java)
        intent.putExtra("is_incognito", true)

        val rpIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        Pump.newRequest(url, "$downloadFolder/$fileName") //Set id,optionally
            .listener(object : DownloadListener() {
                override fun onSuccess() {
                    notificationManager.cancel(uniqid)
                    builder.setContentTitle(context.getString(R.string.download_successful))
                        .setContentText(URLUtil.guessFileName(url, contentDisposition, mimeType))
                        .setSmallIcon(R.drawable.ic_file_download_black)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(rpIntent)
                        .setOnlyAlertOnce(true)
                    builder.setProgress(0, 0, false);
                    notificationManager.notify(uniqid + 1, builder.build())

                    val file = downloadInfo.filePath

                    MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null
                    ) { path, uri ->
                        Log.i("ExternalStorage", "Scanned $path:")
                        Log.i("ExternalStorage", "-> uri=$uri")
                    }

                }
                override fun onFailed() {
                    notificationManager.cancel(uniqid)
                    LogUtil.e("onFailed code=" + downloadInfo.errorCode)
                }
                override fun onProgress(progress: Int) {
                    if (progress.toString().contains("0")){
                        builder.setProgress(100, progress, false)
                        notificationManager.notify(uniqid, builder.build())
                    }

                }
            })
            .threadNum(1)
            .submit()

    }

}