package com.gaurav.stockscanai;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  createChannel();
  if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
  web=new WebView(this); setContentView(web);
  WebSettings s=web.getSettings();
  s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setCacheMode(WebSettings.LOAD_NO_CACHE);
  web.clearCache(true);
  web.addJavascriptInterface(new Bridge(this),"StockScanAndroid");
  web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
  web.loadUrl("https://stock-scan-ai.hatchable.site/?app=v4");
 }
 private void createChannel(){
  if(Build.VERSION.SDK_INT>=26){
   NotificationChannel c=new NotificationChannel("signals","Stock Scan Signals",NotificationManager.IMPORTANCE_HIGH);
   c.setDescription("Paper trade signals, entries and exits");
   getSystemService(NotificationManager.class).createNotificationChannel(c);
  }
 }
 public static class Bridge {
  Context c; Bridge(Context c){this.c=c;}
  @JavascriptInterface public void notifySignal(String title,String body){
   NotificationCompat.Builder b=new NotificationCompat.Builder(c,"signals")
    .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(body)
    .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true);
   if(ActivityCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
    NotificationManagerCompat.from(c).notify((int)(System.currentTimeMillis()%100000),b.build());
  }
 }
 @Override public void onBackPressed(){if(web.canGoBack()) web.goBack(); else super.onBackPressed();}
}