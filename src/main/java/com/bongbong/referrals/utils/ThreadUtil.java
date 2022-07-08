package com.bongbong.referrals.utils;


import com.bongbong.referrals.ReferralsPlugin;

public class ThreadUtil {
    public static void runTask(boolean async, ReferralsPlugin plugin, Runnable runnable) {
        if(async) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }
}
