package com.bongbong.referrals;

import com.bongbong.referrals.utils.Colors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConfigValues {
//    MONGO_DATABASE("DATABASE.MONGO.DB"),
//    MONGO_URI("DATABASE.MONGO.URI"),

    SQL_HOST("DATABASE.MYSQL.HOST"),
    SQL_PORT("DATABASE.MYSQL.PORT"),
    SQL_USER("DATABASE.MYSQL.USER"),
    SQL_PASSWORD("DATABASE.MYSQL.PASSWORD"),
    SQL_DATABASE("DATABASE.MYSQL.DATABASE");

    private String path;

    public String format(ReferralsPlugin plugin) {
        return Colors.convertLegacyColors(plugin.getConfig().getString(path));
    }
}