package com.mega.browser.mobile.android.js

import com.anthonycr.mezzanine.FileStream


@FileStream("app/src/main/js/CookieBlock.js")
interface CookieBlock {

    fun provideJs(): String

}