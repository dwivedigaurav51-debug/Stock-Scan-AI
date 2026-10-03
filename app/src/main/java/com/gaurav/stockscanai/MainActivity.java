package com.gaurav.stockscanai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  WebView.setWebContentsDebuggingEnabled(true);
  web=new WebView(this); setContentView(web);
  WebSettings s=web.getSettings();
  s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setCacheMode(WebSettings.LOAD_NO_CACHE);
  web.clearCache(true);
  web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
  web.loadUrl("https://stock-scan-ai.hatchable.site/?app=v2");
 }
 @Override public void onBackPressed(){if(web.canGoBack()) web.goBack(); else super.onBackPressed();}
}