package com.mega.browser.mobile.android.html.homepage

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import android.webkit.URLUtil
import com.mega.browser.mobile.android.AppTheme
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.browser.HomepageTypeChoice
import com.mega.browser.mobile.android.constant.FILE
import com.mega.browser.mobile.android.constant.UTF8
import com.mega.browser.mobile.android.database.history.HistoryRepository
import com.mega.browser.mobile.android.html.HtmlPageFactory
import com.mega.browser.mobile.android.html.ListPageReader
import com.mega.browser.mobile.android.html.jsoup.*
import com.mega.browser.mobile.android.preference.UserPreferences
import com.mega.browser.mobile.android.search.SearchEngineProvider
import com.mega.browser.mobile.android.utils.DrawableUtils
import com.mega.browser.mobile.android.html.jsoup.andBuild
import com.mega.browser.mobile.android.html.jsoup.body
import com.mega.browser.mobile.android.html.jsoup.charset
import com.mega.browser.mobile.android.html.jsoup.idMy
import com.mega.browser.mobile.android.html.jsoup.parse
import com.mega.browser.mobile.android.html.jsoup.tag
import com.mega.browser.mobile.android.html.jsoup.title
import dagger.Reusable
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.net.URI
import javax.inject.Inject


/**
 * A factory for the home page.
 */
@Reusable
class HomePageFactory @Inject constructor(
    private val application: Application,
    private val searchEngineProvider: SearchEngineProvider,
    private val homePageReader: HomePageReader,
    private var userPreferences: UserPreferences,
    private var resources: Resources,
    private val historyRepository: HistoryRepository,
    private val listPageReader: ListPageReader
) : HtmlPageFactory {

    private val title = application.getString(R.string.home)

    override fun buildPage(): Single<String> = Single
        .just(searchEngineProvider.provideSearchEngine())
        .map { (iconUrl, queryUrl, _) ->
            parse(homePageReader.provideHtml()) andBuild {
                title { title }
                charset { UTF8 }
                body {
                    // Add background image
                    if(userPreferences.imageUrlString != ""){ tag("body") { attr("style", "background: url('" + userPreferences.imageUrlString + "') no-repeat scroll;") } }

                    // Set search engine icon
                    idMy("search_input") { attr("style", "background: url('" + iconUrl + "') no-repeat scroll 7px 7px;background-size: 22px 22px;") }

                    // Fill params in scripts
                    tag("script") {
                        html(
                                if(userPreferences.homepageType == HomepageTypeChoice.INFORMATIVE){
                                    html()
                                            .replace("\${ENDPOINT}", userPreferences.newsEndpoint)
                                            .replace("\${BASE_URL}", queryUrl)
                                            .replace("&", "\\u0026")
                                }
                                else{
                                    html()
                                            .replace("\${BASE_URL}", queryUrl)
                                            .replace("&", "\\u0026")
                                }
                        )
                    }

                    if(userPreferences.homepageType == HomepageTypeChoice.FOCUSED){
                        idMy("image_url")?.remove()
                    }

                    // Shortcuts
                    if(userPreferences.showShortcuts){
                        val shortcuts = arrayListOf(userPreferences.link1, userPreferences.link2, userPreferences.link3, userPreferences.link4)

                        idMy("edit_shortcuts"){ text(resources.getString(R.string.edit_shortcuts)) }
                        idMy("apply"){ text(resources.getString(R.string.apply)) }
                        idMy("link1click"){ attr("href", shortcuts[0])}
                        idMy("link2click"){ attr("href", shortcuts[1])}
                        idMy("link3click"){ attr("href", shortcuts[2])}
                        idMy("link4click"){ attr("href", shortcuts[3])}

                        shortcuts.forEachIndexed { index, element ->
                            if(!URLUtil.isValidUrl(element)){
                                val icon = createIconByName('?')
                                val encoded = bitmapToBase64(icon)

                                idMy("link" + (index + 1)){ attr("src",
                                        "data:image/png;base64,$encoded"
                                )}

                                return@forEachIndexed
                            }

                            val url = URI(element.replaceFirst("www.", ""))
                            val icon = createIconByName(url.host.first().toUpperCase())
                            val encoded = bitmapToBase64(icon)
                            idMy("link" + (index + 1)){ attr("src", "https://${URI(element).host}/favicon.ico")}
                            idMy("link" + (index + 1)){ attr("onerror", "this.src = 'data:image/png;base64,$encoded';")}

                        }

                        idMy("search_input"){ attr("placeholder", resources.getString(R.string.search_homepage))}
                    }
                    else{
                        idMy("shortcuts"){ attr("style", "display: none;")}
                    }

                }
            }
        }
        .map { content -> Pair(createHomePage(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use {
                if(userPreferences.startPageThemeEnabled && userPreferences.useTheme == AppTheme.LIGHT){
                    it.write(content)
                }
                else if(userPreferences.startPageThemeEnabled && userPreferences.useTheme == AppTheme.BLACK){
                    it.write(content + "<style>body {\n" +
                            "    background-color: #000000;\n" +
                            "} .text, .edit{" +
                            "color: #ffffff;" +
                            "fill: #ffffff;" +
                            "}</style>")
                }
                else if(userPreferences.startPageThemeEnabled && userPreferences.useTheme == AppTheme.DARK){
                    it.write(content + "<style>body {\n" +
                            "    background-color: #2a2a2a;\n" +
                            "} .text, .edit{" +
                            "color: #ffffff;" +
                            "fill: #ffffff;" +
                            "}</style>")
                }
                else{
                    it.write(content)
                }
            }
        }
        .map { (page, _) -> "$FILE$page" }

    /**
     * Create the home page file.
     */
    fun createHomePage() = File(application.filesDir, FILENAME)

    fun createIconByName(name: Char): Bitmap{
        val icon = com.mega.browser.mobile.android.utils.DrawableUtils.createRoundedLetterImage(
                name,
                64,
                64,
                Color.GRAY
        )
        return icon
    }

    fun bitmapToBase64(bitmap: Bitmap): String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val encoded: String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return encoded
    }

    companion object {

        const val FILENAME = "homepage.html"

    }

}
