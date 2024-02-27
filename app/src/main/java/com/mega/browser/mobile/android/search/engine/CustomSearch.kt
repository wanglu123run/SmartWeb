package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * A custom search engine.
 */
class CustomSearch(queryUrl: String) : BaseSearchEngine(
    "file:///android_asset/megabrowser.webp",
    queryUrl,
    R.string.search_engine_custom
)
