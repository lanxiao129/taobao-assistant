package com.taobao.htao.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ScriptExecutionService extends Service {
    private static final String TAG = "ScriptService";
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("click_script")) {
            ClickScript script = (ClickScript) intent.getSerializableExtra("click_script");
            executeScript(script);
        }
        return START_STICKY;
    }
    
    private void executeScript(ClickScript script) {
        ClickAccessibilityService accessibilityService = ClickAccessibilityService.getInstance();
        if (accessibilityService != null) {
            accessibilityService.executeClickScript(script);
            Log.d(TAG, "脚本执行开始");
        } else {
            Log.e(TAG, "无障碍服务未启动");
        }
    }
}
