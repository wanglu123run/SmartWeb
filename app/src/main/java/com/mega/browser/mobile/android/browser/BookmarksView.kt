package com.mega.browser.mobile.android.browser

import com.mega.browser.mobile.android.database.Bookmark

interface BookmarksView {

    fun navigateBack()

    fun handleUpdatedUrl(url: String)

    fun handleBookmarkDeleted(bookmark: Bookmark)

}
