package com.tools.lantransfer.track

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.mega.browser.mobile.android.BrowserApp
import com.mega.browser.mobile.android.track.Channel
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsWrapper private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AnalyticsWrapper()
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var formatter: SimpleDateFormat
    private var clientDate: Date
    private var timeZone: TimeZone

    init {
        BrowserApp.app?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
            firebaseAnalytics.setUserProperty(TrackerParamName.CHANNEL, Channel.channel())
        }
        formatter = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault())
        clientDate = Date()
        timeZone = TimeZone.getDefault()
    }


    @JvmOverloads
    fun logEvent(eventName: String, bundle: Bundle = Bundle()) {
        bundle.putString(TrackerParamName.CLIENT_DATE, getDate())
        bundle.putString(TrackerParamName.CLIENT_TIME_ZONE, timeZone.getDisplayName(false, TimeZone.SHORT))
        bundle.putString(TrackerParamName.CHANNEL, Channel.channel())
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    private fun getDate():String {
        clientDate.time = System.currentTimeMillis()
        return formatter.format(clientDate)
    }


}