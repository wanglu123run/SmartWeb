package com.mega.browser.mobile.android.utils

fun stringContainsItemFromList(inputStr: String, items: Array<String>): Boolean {
    for (i in items.indices) {
        if (inputStr.contains(items[i])) {
            return true
        }
    }
    return false
}