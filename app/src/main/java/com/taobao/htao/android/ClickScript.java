package com.taobao.htao.android;

import java.io.Serializable;

public class ClickScript implements Serializable {
    private String packageName;
    private String className;
    private int x;
    private int y;
    private long delay;
    private String text;
    private String viewId;
    
    public ClickScript() {}
    
    public ClickScript(String packageName, String className, int x, int y, long delay, String text, String viewId) {
        this.packageName = packageName;
        this.className = className;
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.text = text;
        this.viewId = viewId;
    }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public long getDelay() { return delay; }
    public void setDelay(long delay) { this.delay = delay; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getViewId() { return viewId; }
    public void setViewId(String viewId) { this.viewId = viewId; }
    
    @Override
    public String toString() {
        return "ClickScript{" +
                "packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", delay=" + delay +
                ", text='" + text + '\'' +
                ", viewId='" + viewId + '\'' +
                '}';
    }
}
