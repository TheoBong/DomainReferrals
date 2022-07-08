package com.bongbong.referrals;

import com.bongbong.referrals.utils.Colors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConfigValues {
    MONGO_DATABASE("DATABASE.MONGO.DB"),
    MONGO_URI("DATABASE.MONGO.URI");

    private String path;

    public String format(ReferralsPlugin plugin) {
        return Colors.convertLegacyColors(plugin.getConfig().getString(path));
    }
}