package com.mega.browser.mobile.android.browser

import com.mega.browser.mobile.android.preference.IntEnum

/**
 * The available proxy choices.
 */
enum class PasswordChoice(override val value: Int) : IntEnum {
    NONE(0),
    CUSTOM(1)
}
