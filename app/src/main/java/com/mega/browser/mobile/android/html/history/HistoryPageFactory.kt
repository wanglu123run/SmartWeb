package com.mega.browser.mobile.android.html.history

import android.app.Application
import android.content.res.Resources
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.constant.FILE
import com.mega.browser.mobile.android.database.history.HistoryRepository
import com.mega.browser.mobile.android.html.HtmlPageFactory
import com.mega.browser.mobile.android.html.ListPageReader
import com.mega.browser.mobile.android.html.jsoup.andBuild
import com.mega.browser.mobile.android.html.jsoup.body
import com.mega.browser.mobile.android.html.jsoup.clone
import com.mega.browser.mobile.android.html.jsoup.idMy
import com.mega.browser.mobile.android.html.jsoup.parse
import com.mega.browser.mobile.android.html.jsoup.removeElement
import com.mega.browser.mobile.android.html.jsoup.tag
import com.mega.browser.mobile.android.html.jsoup.title
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import java.text.DateFormat.getDateTimeInstance
import java.util.*
import javax.inject.Inject

/**
 * Factory for the history page.
 */
@Reusable
class HistoryPageFactory @Inject constructor(
    private val listPageReader: ListPageReader,
    private val application: Application,
    private val historyRepository: HistoryRepository,
    private var resources: Resources
) : HtmlPageFactory {

    private val title = application.getString(R.string.action_history)

    override fun buildPage(): Single<String> = historyRepository
        .lastHundredVisitedHistoryEntries()
        .map { list ->
            parse(listPageReader.provideHtml()) andBuild {
                title { title }
                body {
                    val repeatedElement = idMy("repeated")?.removeElement()
                    idMy("content") {
                        list.forEach {
                            appendChild(repeatedElement?.clone {
                                tag("a") { attr("href", it.url) }
                                idMy("title") { text(it.title) }
                                idMy("url") { text(it.url) }
                                idMy("date") { text(resources.getString(R.string.last_loaded) + " " + getDateTime(it.lastTimeVisited)) }
                            })
                        }
                    }
                }
            }
        }
        .map { content -> Pair(createHistoryPage(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use { it.write(content) }
        }
        .map { (page, _) -> "$FILE$page" }

    /**
     * Use this observable to immediately delete the history page. This will clear the cached
     * history page that was stored on file.
     *
     * @return a completable that deletes the history page when subscribed to.
     */
    fun deleteHistoryPage(): Completable = Completable.fromAction {
        with(createHistoryPage()) {
            if (exists()) {
                delete()
            }
        }
    }

    private fun createHistoryPage() = File(application.filesDir, FILENAME)

    private fun getDateTime(s: Long): String? {
        try {
            val sdf = getDateTimeInstance()
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    companion object {
        const val FILENAME = "history.html"
    }

}
