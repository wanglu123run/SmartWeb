package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * The CookieJarApps searx instance search engine.
 *
 */
class CookieJarAppsSearch : BaseSearchEngine(
    "file:///android_asset/smartcookieweb.webp",
    "https://searx.cookiejarapps.com/?q=",
    R.string.search_engine_searx_cookiejarapps
)
