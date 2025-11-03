package com.taobao.htao.android;

import android.content.Context;
import android.content.SharedPreferences;

public class ScriptManager {
    private static final String PREF_NAME = "click_scripts";
    private static final String KEY_SCRIPT = "saved_script";
    
    public static void saveScript(Context context, ClickScript script) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String scriptJson = script.toString();
        prefs.edit().putString(KEY_SCRIPT, scriptJson).apply();
    }
    
    public static ClickScript loadScript(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String scriptJson = prefs.getString(KEY_SCRIPT, null);
        if (scriptJson != null) {
            try {
                ClickScript script = new ClickScript();
                String[] parts = scriptJson.replace("ClickScript{", "").replace("}", "").split(", ");
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        switch (keyValue[0]) {
                            case "packageName":
                                script.setPackageName(keyValue[1].replace("'", ""));
                                break;
                            case "x":
                                script.setX(Integer.parseInt(keyValue[1]));
                                break;
                            case "y":
                                script.setY(Integer.parseInt(keyValue[1]));
                                break;
                            case "delay":
                                script.setDelay(Long.parseLong(keyValue[1]));
                                break;
                        }
                    }
                }
                return script;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
