package com.mega.browser.mobile.android.js

import com.anthonycr.mezzanine.FileStream


@FileStream("app/src/main/js/AmpBlock.js")
interface BlockAMP {

    fun provideJs(): String

}