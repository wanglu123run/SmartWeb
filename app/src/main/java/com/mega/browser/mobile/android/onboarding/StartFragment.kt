/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Created by CookieJarApps 10/01/2020 */

package com.mega.browser.mobile.android.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mega.browser.mobile.android.R


class StartFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_splash_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireView().findViewById<TextView>(R.id.service_agreement).setOnClickListener {
            //服务条款
//            WebViewActivity.start(requireContext(), "www.baidu.com", null)
        }

        requireView().findViewById<TextView>(R.id.privacy_agreement).setOnClickListener {
            //隐私协议
            WebViewActivity.start(requireContext(), "https://sites.google.com/view/browser-privacy-policy-ww", "Privacy Policy")
        }


    }


    companion object {
        fun newInstance() : StartFragment {
            return StartFragment()
        }
    }
}
