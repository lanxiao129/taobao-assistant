package com.taobao.htao.android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class ClickAccessibilityService extends AccessibilityService {
    private static final String TAG = "ClickAccessibility";
    private Handler handler = new Handler();
    
    private static ClickAccessibilityService instance;
    private ClickScript currentScript;
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (currentScript != null) {
            executeScript(currentScript);
        }
    }
    
    @Override
    public void onInterrupt() {
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "无障碍服务已连接");
    }
    
    public static ClickAccessibilityService getInstance() {
        return instance;
    }
    
    public void executeClickScript(ClickScript script) {
        this.currentScript = script;
        executeScript(script);
    }
    
    private void executeScript(ClickScript script) {
        if (script == null) return;
        
        handler.postDelayed(() -> {
            if (script.getX() > 0 && script.getY() > 0) {
                clickAtCoordinates(script.getX(), script.getY());
                return;
            }
            
            if (script.getViewId() != null && !script.getViewId().isEmpty()) {
                clickByViewId(script.getViewId());
                return;
            }
            
            if (script.getText() != null && !script.getText().isEmpty()) {
                clickByText(script.getText());
                return;
            }
            
        }, script.getDelay());
    }
    
    private void clickAtCoordinates(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        
        GestureDescription gesture = builder.build();
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "坐标点击完成: " + x + ", " + y);
            }
        }, null);
    }
    
    private void clickByViewId(String viewId) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (!nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "通过ID点击: " + viewId);
                    break;
                }
            }
        }
    }
    
    private void clickByText(String text) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (!nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "通过文本点击: " + text);
                    break;
                }
            }
        }
    }
}
