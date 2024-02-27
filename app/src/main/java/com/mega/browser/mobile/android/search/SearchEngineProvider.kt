package com.mega.browser.mobile.android.search

import android.app.Application
import com.mega.browser.mobile.android.di.SuggestionsClient
import com.mega.browser.mobile.android.log.Logger
import com.mega.browser.mobile.android.preference.UserPreferences
import com.mega.browser.mobile.android.search.engine.AskSearch
import com.mega.browser.mobile.android.search.engine.BaiduSearch
import com.mega.browser.mobile.android.search.engine.BaseSearchEngine
import com.mega.browser.mobile.android.search.engine.BingSearch
import com.mega.browser.mobile.android.search.engine.CustomSearch
import com.mega.browser.mobile.android.search.engine.DuckLiteSearch
import com.mega.browser.mobile.android.search.engine.DuckSearch
import com.mega.browser.mobile.android.search.engine.EcosiaSearch
import com.mega.browser.mobile.android.search.engine.GoogleSearch
import com.mega.browser.mobile.android.search.engine.NaverSearch
import com.mega.browser.mobile.android.search.engine.StartPageMobileSearch
import com.mega.browser.mobile.android.search.engine.StartPageSearch
import com.mega.browser.mobile.android.search.engine.YahooSearch
import com.mega.browser.mobile.android.search.engine.YandexSearch
import com.mega.browser.mobile.android.search.suggestions.BaiduSuggestionsModel
import com.mega.browser.mobile.android.search.suggestions.DuckSuggestionsModel
import com.mega.browser.mobile.android.search.suggestions.GoogleSuggestionsModel
import com.mega.browser.mobile.android.search.suggestions.NaverSuggestionsModel
import com.mega.browser.mobile.android.search.suggestions.NoOpSuggestionsRepository
import com.mega.browser.mobile.android.search.suggestions.RequestFactory
import com.mega.browser.mobile.android.search.suggestions.MegaWebSuggestionsModel
import com.mega.browser.mobile.android.search.suggestions.SuggestionsRepository
import dagger.Reusable
import io.reactivex.Single
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * The model that provides the search engine based
 * on the user's preference.
 */
@Reusable
class  SearchEngineProvider @Inject constructor(
    private val userPreferences: UserPreferences,
    @SuggestionsClient private val okHttpClient: Single<OkHttpClient>,
    private val requestFactory: RequestFactory,
    private val application: Application,
    private val logger: Logger
) {

    /**
     * Provide the [SuggestionsRepository] that maps to the user's current preference.
     */
    fun provideSearchSuggestions(): SuggestionsRepository =
        when (userPreferences.searchSuggestionChoice) {
            0 -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger)
            1 -> DuckSuggestionsModel(okHttpClient, requestFactory, application, logger)
            2 -> BaiduSuggestionsModel(okHttpClient, requestFactory, application, logger)
            3 -> NaverSuggestionsModel(okHttpClient, requestFactory, application, logger)
            4 -> MegaWebSuggestionsModel(okHttpClient, requestFactory, application, logger)
            5 -> NoOpSuggestionsRepository()
            else -> GoogleSuggestionsModel(okHttpClient, requestFactory, application, logger)
        }

    /**
     * Provide the [BaseSearchEngine] that maps to the user's current preference.
     */
    fun provideSearchEngine(): BaseSearchEngine =
        when (userPreferences.searchChoice) {
            0 -> CustomSearch(userPreferences.searchUrl)
            1 -> GoogleSearch()
            2 -> AskSearch()
            3 -> BingSearch()
            4 -> YahooSearch()
            5 -> StartPageSearch()
            6 -> StartPageMobileSearch()
            7 -> DuckSearch()
            8 -> DuckLiteSearch()
            9 -> BaiduSearch()
            10 -> YandexSearch()
            11 -> NaverSearch()
            12 -> EcosiaSearch()
//            13 -> EkoruSearch()
//            14 -> CookieJarAppsSearch()
//            15 -> SearxSearch()
            else -> GoogleSearch()
        }

    /**
     * Return the serializable index of of the provided [BaseSearchEngine].
     */
    fun mapSearchEngineToPreferenceIndex(searchEngine: BaseSearchEngine): Int =
        when (searchEngine) {
            is CustomSearch -> 0
            is GoogleSearch -> 1
            is AskSearch -> 2
            is BingSearch -> 3
            is YahooSearch -> 4
            is StartPageSearch -> 5
            is StartPageMobileSearch -> 6
            is DuckSearch -> 7
            is DuckLiteSearch -> 8
            is BaiduSearch -> 9
            is YandexSearch -> 10
            is NaverSearch -> 11
            is EcosiaSearch -> 12
//            is EkoruSearch -> 13
//            is CookieJarAppsSearch -> 14
//            is SearxSearch -> 15
            else -> throw UnsupportedOperationException("Unknown search engine provided: " + searchEngine.javaClass)
        }

    /**
     * Provide a list of all supported search engines.
     */
    fun provideAllSearchEngines(): List<BaseSearchEngine> = listOf(
        CustomSearch(userPreferences.searchUrl),
        GoogleSearch(),
        AskSearch(),
        BingSearch(),
        YahooSearch(),
        StartPageSearch(),
        StartPageMobileSearch(),
        DuckSearch(),
        DuckLiteSearch(),
        BaiduSearch(),
        YandexSearch(),
        NaverSearch(),
        EcosiaSearch(),
//        EkoruSearch(),
//        CookieJarAppsSearch(),
//        SearxSearch()
    )

}
