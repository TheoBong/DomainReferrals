package com.bongbong.referrals.database;

import java.util.UUID;

public abstract class Database {
    public enum Type {
        MONGO, SQLite, MySQL
    }

    public abstract Type type();

    //Returns whether the uuid exists
    public abstract void referCheck(UUID uuid, String hostname, Result result);

    public abstract void createDomain(UUID uuid, String hostname, Result result);

    public abstract void getReferredList(UUID uuid, Result result);

    public abstract void setGroup(UUID uuid, String group, Result result);

    public abstract void resetReferrals(UUID uuid, Result result);

    public abstract void checkDomain(String subdomain, Result result);

    public abstract void close();
}
