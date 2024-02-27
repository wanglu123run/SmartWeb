package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * The Searx search engine.
 *
 */
class SearxSearch : BaseSearchEngine(
    "file:///android_asset/searx.webp",
    "https://www.searx.be/?q=",
    R.string.search_engine_searx
)
