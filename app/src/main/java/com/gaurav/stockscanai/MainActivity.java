package com.gaurav.stockscanai;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.webkit.*;
import androidx.core.app.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        createChannel();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        web = new WebView(this);
        web.setKeepScreenOn(true);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setAllowContentAccess(false);
        if (Build.VERSION.SDK_INT >= 16) {
            s.setAllowFileAccessFromFileURLs(false);
            s.setAllowUniversalAccessFromFileURLs(false);
        }
        web.clearCache(true);
        web.addJavascriptInterface(new Bridge(this, web), "StockScanAndroid");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u=request.getUrl();
                return !("file".equalsIgnoreCase(u.getScheme()) && u.toString().startsWith("file:///android_asset/"));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !(url != null && url.startsWith("file:///android_asset/"));
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.loadUrl("file:///android_asset/index.html");
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("signals","Stock Scan Signals",NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Virtual trade signals, entries and exits");
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }
    public static class Bridge {
        private final Context c; private final WebView web; private final SharedPreferences prefs;
        Bridge(Context c,WebView web){this.c=c;this.web=web;this.prefs=c.getSharedPreferences("stock_scan_secure",Context.MODE_PRIVATE);}
        @JavascriptInterface public void saveApiKey(String key){if(key!=null)prefs.edit().putString("indianapi_key",key.trim()).apply();}
        @JavascriptInterface public boolean hasApiKey(){return !prefs.getString("indianapi_key","").isEmpty();}
        @JavascriptInterface public void clearApiKey(){prefs.edit().remove("indianapi_key").apply();}
        @JavascriptInterface public void apiGet(String requestId,String url){
            new Thread(()->{
                String body;
                try{
                    Uri u=Uri.parse(url);
                    if(!"https".equalsIgnoreCase(u.getScheme())||!"stock.indianapi.in".equalsIgnoreCase(u.getHost()))throw new IOException("Blocked API host");
                    String key=prefs.getString("indianapi_key",""); if(key.isEmpty())throw new IOException("IndianAPI key not saved");
                    HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();
                    con.setRequestMethod("GET");con.setConnectTimeout(15000);con.setReadTimeout(15000);
                    con.setRequestProperty("Accept","application/json");
                    con.setRequestProperty("X-API-Key",key);
                    con.setRequestProperty("User-Agent","StockScanAI/7.1");
                    int code=con.getResponseCode();
                    InputStream in=(code>=200&&code<400)?con.getInputStream():con.getErrorStream();
                    body=readAll(in);
                    if(body==null||body.trim().isEmpty()){JSONObject e=new JSONObject();e.put("status","error");e.put("message","HTTP "+code+" empty response");body=e.toString();}
                    else if(code>=400){try{JSONObject e=new JSONObject(body);if(!e.has("message"))e.put("message","HTTP "+code+" • "+body);body=e.toString();}catch(Exception ignore){}}
                    con.disconnect();
                }catch(Exception e){
                    try{JSONObject j=new JSONObject();j.put("status","error");j.put("message","Network/API error: "+e.getMessage());body=j.toString();}
                    catch(Exception ignored){body="{\"status\":\"error\",\"message\":\"Network/API error\"}";}
                }
                final String result=body;
                web.post(()->web.evaluateJavascript("window.onNativeApiResult("+JSONObject.quote(requestId)+","+JSONObject.quote(result)+");",null));
            }).start();
        }
        private static String readAll(InputStream in)throws IOException{
            if(in==null)return "";
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))>=0)out.write(buf,0,n);in.close();return out.toString(StandardCharsets.UTF_8.name());
        }
        @JavascriptInterface public void notifySignal(String title,String body){
            NotificationCompat.Builder b=new NotificationCompat.Builder(c,"signals").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(body).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true);
            if(ActivityCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
                NotificationManagerCompat.from(c).notify((int)(System.currentTimeMillis()%100000),b.build());
        }
    }
    @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}