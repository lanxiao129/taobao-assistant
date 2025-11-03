package com.taobao.htao.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

public class RecordingOverlayActivity extends Activity {
    private static final String TAG = "RecordingOverlay";
    private WindowManager windowManager;
    private View overlayView;
    private boolean isRecording = false;
    private ClickScript currentScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createOverlayView();
        setupWindowManager();
    }

    private void createOverlayView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xCC000000);
        
        TextView title = new TextView(this);
        title.setText("点击录制器");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16);
        title.setPadding(20, 20, 20, 20);
        
        Button startBtn = new Button(this);
        startBtn.setText("开始录制");
        startBtn.setOnClickListener(v -> toggleRecording());
        
        Button saveBtn = new Button(this);
        saveBtn.setText("保存脚本");
        saveBtn.setOnClickListener(v -> saveScript());
        
        Button closeBtn = new Button(this);
        closeBtn.setText("关闭");
        closeBtn.setOnClickListener(v -> finish());
        
        layout.addView(title);
        layout.addView(startBtn);
        layout.addView(saveBtn);
        layout.addView(closeBtn);
        
        overlayView = layout;
    }

    private void setupWindowManager() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        
        windowManager.addView(overlayView, params);
        
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isRecording) return false;
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = ((WindowManager.LayoutParams) overlayView.getLayoutParams()).x;
                        initialY = ((WindowManager.LayoutParams) overlayView.getLayoutParams()).y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        recordClick(x, y);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayView.getLayoutParams();
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void toggleRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            currentScript = new ClickScript();
            Log.d(TAG, "开始录制点击脚本");
        } else {
            Log.d(TAG, "停止录制");
        }
    }

    private void recordClick(int x, int y) {
        if (currentScript != null) {
            currentScript.setX(x);
            currentScript.setY(y);
            currentScript.setDelay(1000);
            currentScript.setPackageName("com.taobao.taobao");
            
            Log.d(TAG, "录制点击坐标: " + x + ", " + y);
        }
    }

    private void saveScript() {
        if (currentScript != null) {
            ScriptManager.saveScript(this, currentScript);
            Log.d(TAG, "脚本已保存: " + currentScript.toString());
            
            Intent intent = new Intent("SCRIPT_SAVED");
            sendBroadcast(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
}
