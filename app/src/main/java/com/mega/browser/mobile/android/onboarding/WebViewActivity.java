package com.mega.browser.mobile.android.onboarding;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mega.browser.mobile.android.R;


public class WebViewActivity extends AppCompatActivity {


    private WebView webView;
    private TextView title_tv;
    private ImageView return_img;

    public static void start(Context context, String webUrl, String title) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("webUrl", webUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        init();
    }


    protected void init() {
        Intent intent = getIntent();
        String webUrl = intent.getStringExtra("webUrl");
        String extraTitle = intent.getStringExtra("title");

        webView = findViewById(R.id.web_view);
        return_img = findViewById(R.id.tv_back);
        return_img.setOnClickListener(view -> {finish();});
        title_tv = findViewById(R.id.tv_title);
        if (!TextUtils.isEmpty(extraTitle)) {
            title_tv.setText(extraTitle);
        }
        webView.getSettings().setJavaScriptEnabled(true);// 设置可以运行JS脚本
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);//隐藏Zoom缩放按钮

        //WebView加载web资源
        webView.loadUrl(webUrl);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (TextUtils.isEmpty(extraTitle)) {
                    title_tv.setText(title);
                }
            }
        });
    }

}