package com.taobao.htao.android;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private boolean isFromExternalLink = false;
    private static final String TAG = "MainActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);
        
        setupWebView();
        
        checkAccessibilityService();
        
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(scriptSavedReceiver, new IntentFilter("SCRIPT_SAVED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(scriptSavedReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isFromExternalLink = false;
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient());
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void startRecording() {
            runOnUiThread(() -> {
                startRecording();
            });
        }
    }

    private void startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, RecordingOverlayActivity.class);
            startActivity(intent);
        }
    }

    private BroadcastReceiver scriptSavedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("SCRIPT_SAVED".equals(intent.getAction())) {
                loadSavedScript();
                Log.d(TAG, "收到脚本保存通知");
            }
        }
    };

    private void loadSavedScript() {
        ClickScript savedScript = ScriptManager.loadScript(this);
        if (savedScript != null) {
            Log.d(TAG, "加载已保存的脚本: " + savedScript.toString());
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String url = data.toString();
                if (isTaobaoLink(url)) {
                    isFromExternalLink = true;
                    minimizeApp();
                    startScriptExecution(url);
                }
            }
        } else {
            loadHomePage();
        }
    }

    private void loadHomePage() {
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    private boolean isTaobaoLink(String url) {
        if (url == null) return false;
        return url.contains("taobao.com") ||
               url.contains("tmall.com") ||
               url.contains("tb.cn") ||
               (url.startsWith("http") && url.contains("id=") && 
                (url.contains("detail.htm") || url.contains("item.htm")));
    }

    private void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null) {
            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo service : enabledServices) {
                if (service.getId().contains(getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled()) {
            Log.w(TAG, "无障碍服务未启用");
        } else {
            Log.d(TAG, "无障碍服务已启用");
        }
    }

    private void startScriptExecution(String taobaoUrl) {
        if (!isAccessibilityServiceEnabled()) {
            Log.e(TAG, "无障碍服务未启用，无法执行脚本");
            return;
        }
        
        ClickScript savedScript = ScriptManager.loadScript(this);
        if (savedScript == null) {
            Log.e(TAG, "没有找到已保存的脚本");
            return;
        }
        
        Log.d(TAG, "开始执行点击脚本，淘宝链接: " + taobaoUrl);
        
        Intent serviceIntent = new Intent(this, ScriptExecutionService.class);
        serviceIntent.putExtra("click_script", savedScript);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Log.d(TAG, "脚本执行服务已启动");
    }

    @Override
    public void onBackPressed() {
        String currentUrl = webView.getUrl();
        boolean isHomePage = currentUrl == null || 
                           currentUrl.equals("file:///android_asset/www/index.html") ||
                           currentUrl.contains("index.html");
        
        if (webView.canGoBack() && !isHomePage) {
            webView.goBack();
        } else {
            Log.d(TAG, "在主页，忽略返回键");
        }
    }

    @Override
    protected void onDestroy() {
        Intent serviceIntent = new Intent(this, ScriptExecutionService.class);
        stopService(serviceIntent);
        
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
