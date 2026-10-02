package com.gaurav.stockscanai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle b){super.onCreate(b); web=new WebView(this); setContentView(web); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient()); web.loadUrl("https://stock-scan-ai.hatchable.site");}
 @Override public void onBackPressed(){if(web.canGoBack()) web.goBack(); else super.onBackPressed();}
}