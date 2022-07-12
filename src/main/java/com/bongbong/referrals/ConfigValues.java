package com.bongbong.referrals;

import com.bongbong.referrals.utils.Colors;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public enum ConfigValues {
//    MONGO_DATABASE("DATABASE.MONGO.DB"),
//    MONGO_URI("DATABASE.MONGO.URI"),

    DEFAULT_REWARD_GROUP("GENERAL.DEFAULT_REWARD_GROUP"),
    SERVER_SUBDOMAINS("GENERAL.SERVER_SUBDOMAINS"),

    SQL_HOST("DATABASE.MYSQL.HOST"),
    SQL_PORT("DATABASE.MYSQL.PORT"),
    SQL_USER("DATABASE.MYSQL.USER"),
    SQL_PASSWORD("DATABASE.MYSQL.PASSWORD"),
    SQL_DATABASE("DATABASE.MYSQL.DATABASE");

    private String path;

    public String format(ReferralsPlugin plugin) {
        return Colors.convertLegacyColors(plugin.getConfig().getString(path));
    }

    public List<String> formatLines(ReferralsPlugin plugin) {
        List<String> lines = new ArrayList<>();

        for (String string : plugin.getMessagesFile().getStringList(path)) {
            lines.add(Colors.convertLegacyColors(string));
        }

        return lines;
    }
}