package org.torproject.anyonebot.service.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.torproject.anyonebot.service.AnyoneBotService;

import java.util.Objects;

public class PowerConnectionReceiver extends BroadcastReceiver {

    private final AnyoneBotService mService;

    public PowerConnectionReceiver(AnyoneBotService service) {
        mService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Prefs.limitSnowflakeProxyingCharging()) {
            if (Objects.equals(intent.getAction(), Intent.ACTION_POWER_CONNECTED)) {
                mService.setHasPower(true);

            } else if (Objects.equals(intent.getAction(), Intent.ACTION_POWER_DISCONNECTED)) {
                mService.setHasPower(false);
            }
        }
    }

}