package com.mega.browser.mobile.android.browser.tabs

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mega.browser.mobile.android.R
import com.mega.browser.mobile.android.browser.TabsView
import com.mega.browser.mobile.android.controller.UIController
import com.mega.browser.mobile.android.extensions.inflater
import com.mega.browser.mobile.android.list.HorizontalItemAnimator
import com.mega.browser.mobile.android.preference.UserPreferences
import com.mega.browser.mobile.android.utils.ThemeUtils
import com.mega.browser.mobile.android.utils.Utils
import com.mega.browser.mobile.android.view.SmartCookieView


/**
 * A view which displays browser tabs in a horizontal [RecyclerView].
 */
class TabsDesktopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    userPreferences: UserPreferences
) : ConstraintLayout(context, attrs, defStyleAttr), TabsView {

    private val uiController = context as UIController
    private val tabsAdapter: TabsDesktopAdapter
    private val tabList: RecyclerView

    init {
        setBackgroundColor(Utils.mixTwoColors(com.mega.browser.mobile.android.utils.ThemeUtils.getColorBackground(uiController as Context), Color.BLACK, 0.85f))
        context.inflater.inflate(R.layout.tab_strip, this, true)
        findViewById<ImageView>(R.id.new_tab_button).apply {
            setColorFilter(com.mega.browser.mobile.android.utils.ThemeUtils.getTextColor(uiController as Context))
            setOnClickListener {
                uiController.newTabButtonClicked()
            }
            setOnLongClickListener {
                uiController.newTabButtonLongClicked()
                true
            }
        }

        val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        val animator = com.mega.browser.mobile.android.list.HorizontalItemAnimator().apply {
            supportsChangeAnimations = false
            addDuration = 200
            changeDuration = 0
            removeDuration = 200
            moveDuration = 200
        }

        tabsAdapter = TabsDesktopAdapter(context, context.resources, uiController = uiController, userPreferences = userPreferences)

        tabList = findViewById<RecyclerView>(R.id.tabs_list).apply {
            setLayerType(View.LAYER_TYPE_NONE, null)
            itemAnimator = animator
            this.layoutManager = layoutManager
            adapter = tabsAdapter
            setHasFixedSize(true)
        }

        val backgroundColor = Utils.mixTwoColors(com.mega.browser.mobile.android.utils.ThemeUtils.getColorBackground(uiController as Context), Color.BLACK, 0.85f)
        tabList.setBackgroundColor(backgroundColor)
    }

    override fun tabAdded() {
        displayTabs()
        tabList.postDelayed({ tabList.smoothScrollToPosition(tabsAdapter.itemCount - 1) }, 500)
    }

    override fun tabRemoved(position: Int) {
        displayTabs()
    }

    override fun tabChanged(position: Int) {
        displayTabs()
    }

    private fun displayTabs() {
        tabsAdapter.showTabs(uiController.getTabModel().allTabs.map(SmartCookieView::asTabViewState))
    }

    override fun tabsInitialized() {
        tabsAdapter.notifyDataSetChanged()
    }

    override fun setGoBackEnabled(isEnabled: Boolean) = Unit

    override fun setGoForwardEnabled(isEnabled: Boolean) = Unit

}
