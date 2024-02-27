package com.mega.browser.mobile.android.search.engine

import com.mega.browser.mobile.android.R

/**
 * The Baidu search engine.
 *
 * See http://www.baidu.com/img/bdlogo.gif for the icon.
 */
class BaiduSearch : BaseSearchEngine(
    "file:///android_asset/baidu.webp",
    "https://www.baidu.com/s?wd=",
    R.string.search_engine_baidu
)
