package com.mega.browser.mobile.android.browser

import com.mega.browser.mobile.android.preference.IntEnum

/**
 * An enum representing what detail level should be displayed in the search box.
 */
enum class SearchBoxDisplayChoice(override val value: Int) : IntEnum {
    URL(0),
    DOMAIN(1),
    TITLE(2)
}
