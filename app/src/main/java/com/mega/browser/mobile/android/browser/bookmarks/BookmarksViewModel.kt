package com.mega.browser.mobile.android.browser.bookmarks

import com.mega.browser.mobile.android.database.Bookmark
import android.graphics.Bitmap

/**
 * The data model representing a [Bookmark] in a list.
 *
 * @param bookmark The bookmark backing this view model, either an entry or a folder.
 * @param icon The icon for this bookmark.
 */
data class BookmarksViewModel(
    val bookmark: Bookmark,
    var icon: Bitmap? = null
)
