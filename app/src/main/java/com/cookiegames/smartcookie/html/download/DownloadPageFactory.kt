package com.cookiegames.smartcookie.html.download

import com.cookiegames.smartcookie.R
import com.cookiegames.smartcookie.constant.FILE
import com.cookiegames.smartcookie.database.downloads.DownloadEntry
import com.cookiegames.smartcookie.database.downloads.DownloadsRepository
import com.cookiegames.smartcookie.html.HtmlPageFactory
import com.cookiegames.smartcookie.html.ListPageReader
import com.cookiegames.smartcookie.html.jsoup.*
import com.cookiegames.smartcookie.preference.UserPreferences
import android.app.Application
import dagger.Reusable
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/** 下载记录
 * The factory for the downloads page.
 */
@Reusable
class DownloadPageFactory @Inject constructor(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val manager: DownloadsRepository,
    private val listPageReader: ListPageReader
) : HtmlPageFactory {

    override fun buildPage(): Single<String> = manager
        .getAllDownloads()
        .map { list ->
            parse(listPageReader.provideHtml()) andBuild {
                title { application.getString(R.string.action_downloads) }
                body {
                    val repeatableElement = idMy("repeated")?.removeElement()
                    idMy("content") {
                        list.forEach {
                            val haha = repeatableElement?.clone {
                                tag("a") { attr("href", createFileUrl(it.title)) }
                                idMy("title") { text(createFileTitle(it)) }
                                idMy("url") { text(it.url) }
                            }
                            appendChild(haha)
                        }
                    }
                }
            }
        }
        .map { content ->
            Pair(createDownloadsPageFile(), content)
        }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use { it.write(content) }
        }
        .map { (page, _) -> "$FILE$page" }


    private fun createDownloadsPageFile(): File = File(application.filesDir, FILENAME)

    private fun createFileUrl(fileName: String): String = "$FILE${userPreferences.downloadDirectory}/$fileName"

    private fun createFileTitle(downloadItem: DownloadEntry): String {
        val contentSize = if (downloadItem.contentSize.isNotBlank()) {
            "[${downloadItem.contentSize}]"
        } else {
            ""
        }

        return "${downloadItem.title} $contentSize"
    }

    companion object {

        const val FILENAME = "downloads.html"

    }

}
