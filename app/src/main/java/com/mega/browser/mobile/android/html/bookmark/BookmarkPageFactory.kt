package com.mega.browser.mobile.android.html.bookmark

import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.constant.FILE
import com.mega.browser.mobile.android.database.Bookmark
import com.mega.browser.mobile.android.database.bookmark.BookmarkRepository
import com.mega.browser.mobile.android.di.DatabaseScheduler
import com.mega.browser.mobile.android.di.DiskScheduler
import com.mega.browser.mobile.android.extensions.safeUse
import com.mega.browser.mobile.android.favicon.FaviconModel
import com.mega.browser.mobile.android.favicon.toValidUri
import com.mega.browser.mobile.android.html.HtmlPageFactory
import com.mega.browser.mobile.android.html.jsoup.*
import com.mega.browser.mobile.android.utils.ThemeUtils
import android.app.Application
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.mega.browser.mobile.android.html.jsoup.andBuild
import com.mega.browser.mobile.android.html.jsoup.body
import com.mega.browser.mobile.android.html.jsoup.clone
import com.mega.browser.mobile.android.html.jsoup.idMy
import com.mega.browser.mobile.android.html.jsoup.parse
import com.mega.browser.mobile.android.html.jsoup.removeElement
import com.mega.browser.mobile.android.html.jsoup.tag
import com.mega.browser.mobile.android.html.jsoup.title
import dagger.Reusable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import javax.inject.Inject

/**
 * Created by anthonycr on 9/23/18.
 */
@Reusable
class BookmarkPageFactory @Inject constructor(
    private val application: Application,
    private val bookmarkModel: BookmarkRepository,
    private val faviconModel: FaviconModel,
    @DatabaseScheduler private val databaseScheduler: Scheduler,
    @DiskScheduler private val diskScheduler: Scheduler,
    private val bookmarkPageReader: BookmarkPageReader
) : HtmlPageFactory {

    private val title = application.getString(R.string.action_bookmarks)
    private val folderIconFile by lazy { File(application.cacheDir, FOLDER_ICON) }
    private val defaultIconFile by lazy { File(application.cacheDir, DEFAULT_ICON) }

    override fun buildPage(): Single<String> = bookmarkModel
            .getAllBookmarksSorted()
            .flattenAsObservable { it }
            .groupBy<Bookmark.Folder, Bookmark>(Bookmark.Entry::folder) { it }
            .flatMapSingle { bookmarksInFolder ->
                val folder = bookmarksInFolder.key
                return@flatMapSingle bookmarksInFolder
                        .toList()
                        .concatWith(
                                if (folder == Bookmark.Folder.Root) {
                                    bookmarkModel.getFoldersSorted().map { it.filterIsInstance<Bookmark.Folder.Entry>() }
                                } else {
                                    Single.just(emptyList())
                                }
                        )
                        .toList()
                        .map { bookmarksAndFolders ->
                            Pair(folder, bookmarksAndFolders.flatten().map { it.asViewModel() })
                        }
            }
            .map { (folder, viewModels) -> Pair(folder, construct(viewModels)) }
            .subscribeOn(databaseScheduler)
            .observeOn(diskScheduler)
            .doOnNext { (folder, content) ->
                FileWriter(createBookmarkPage(folder), false).use {
                    it.write(content)
                }
            }
            .ignoreElements()
            .toSingle {
                cacheIcon(com.mega.browser.mobile.android.utils.ThemeUtils.createThemedBitmap(application, R.drawable.ic_folder, false), folderIconFile)
                cacheIcon(faviconModel.createDefaultBitmapForTitle(null), defaultIconFile)

                "$FILE${createBookmarkPage(null)}"
            }

    private fun cacheIcon(icon: Bitmap, file: File) = FileOutputStream(file).safeUse {
        icon.compress(Bitmap.CompressFormat.PNG, 100, it)
        icon.recycle()
    }

    private fun construct(list: List<BookmarkViewModel>): String {
        return parse(bookmarkPageReader.provideHtml()) andBuild {
            title { title }
            body {
                val repeatableElement = idMy("repeated")?.removeElement()
                idMy("content") {
                    list.forEach {
                        appendChild(repeatableElement?.clone {
                            tag("a") { attr("href", it.url) }
                            tag("img") { attr("src", it.iconUrl) }
                            idMy("title") { appendText(it.title) }
                        })
                    }
                }
            }
        }
    }

    private fun Bookmark.asViewModel(): BookmarkViewModel = when (this) {
        is Bookmark.Folder -> createViewModelForFolder(this)
        is Bookmark.Entry -> createViewModelForBookmark(this)
    }

    private fun createViewModelForFolder(folder: Bookmark.Folder): BookmarkViewModel {
        val folderPage = createBookmarkPage(folder)
        val url = "$FILE$folderPage"

        return BookmarkViewModel(
                title = folder.title,
                url = url,
                iconUrl = folderIconFile.toString()
        )
    }

    private fun createViewModelForBookmark(entry: Bookmark.Entry): BookmarkViewModel {
        val bookmarkUri = entry.url.toUri().toValidUri()

        val iconUrl = if (bookmarkUri != null) {
            val faviconFile = FaviconModel.getFaviconCacheFile(application, bookmarkUri)
            if (!faviconFile.exists()) {
                val defaultFavicon = faviconModel.createDefaultBitmapForTitle(entry.title)
                faviconModel.cacheFaviconForUrl(defaultFavicon, entry.url)
                        .subscribeOn(diskScheduler)
                        .subscribe()
            }

            faviconFile
        } else {
            defaultIconFile
        }

        if(bookmarkUri == null){
            return BookmarkViewModel(
                    title = "entry.title",
                    url = "entry.url",
                    iconUrl = "iconUrl.toString()"
            )
        }

        return BookmarkViewModel(
                title = entry.title,
                url = entry.url,
                iconUrl = iconUrl.toString()
        )
    }

    /**
     * Create the bookmark page file.
     */
    fun createBookmarkPage(folder: Bookmark.Folder?): File {
        val prefix = if (folder?.title?.isNotBlank() == true) {
            "${folder.title}-"
        } else {
            ""
        }
        return File(application.filesDir, prefix + FILENAME)
    }

    companion object {

        const val FILENAME = "bookmark.html"

        private const val FOLDER_ICON = "folder.png"
        private const val DEFAULT_ICON = "default.png"

    }
}