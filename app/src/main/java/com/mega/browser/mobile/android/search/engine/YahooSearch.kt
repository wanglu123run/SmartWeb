package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * The Yahoo search engine.
 *
 * See http://upload.wikimedia.org/wikipedia/commons/thumb/2/24/Yahoo%21_logo.svg/799px-Yahoo%21_logo.svg.png
 * for the icon.
 */
class YahooSearch : BaseSearchEngine(
    "file:///android_asset/yahoo.webp",
    "https://search.yahoo.com/search?p=",
    R.string.search_engine_yahoo
)
