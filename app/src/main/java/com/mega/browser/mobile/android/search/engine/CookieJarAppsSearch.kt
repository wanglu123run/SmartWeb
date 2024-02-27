package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * The CookieJarApps searx instance search engine.
 *
 */
class CookieJarAppsSearch : BaseSearchEngine(
    "file:///android_asset/megabrowser.webp",
    "https://searx.cookiejarapps.com/?q=",
    R.string.search_engine_searx_cookiejarapps
)
