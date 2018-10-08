package com.littlecat.powerbank.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.SystemProperties;
import com.littlecat.powerbank.MainActivity;


public class BootBroadcastReceiver extends BroadcastReceiver {  
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    //hefang add AutoClick 20180928
        public boolean isFirstBootup = SystemProperties.get("persist.id.first.poweron").equals("1");


    @Override  
    public void onReceive(Context context, Intent intent) {
        if (isFirstBootup) {
            isFirstBootup = false;
            SystemProperties.set("persist.id.first.poweron", "0");
        } else {
            if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
    }  
  
}  