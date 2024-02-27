package com.mega.browser.mobile.android.browser

import com.mega.browser.mobile.android.preference.IntEnum

/**
 * The available proxy choices.
 */
enum class SiteBlockChoice(override val value: Int) : IntEnum {
    NONE(0),
    WHITELIST(1),
    BLACKLIST(2)
}
