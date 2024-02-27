package com.mega.browser.mobile.android.js

import com.anthonycr.mezzanine.FileStream


@FileStream("app/src/main/js/DarkMode.js")
interface DarkMode {

    fun provideJs(): String

}