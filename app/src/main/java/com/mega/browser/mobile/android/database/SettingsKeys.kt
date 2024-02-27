package com.mega.browser.mobile.android.database

sealed class SettingsKeys(
        open val key: String,
        open val value: String
)