package com.bongbong.referrals.database;

import java.util.UUID;

public abstract class Database {
    public enum Type {
        MONGO, SQLite, MySQL
    }

    public abstract Type type();

    //Returns whether the uuid exists
    public abstract void referCheck(UUID uuid, String hostname);

    public abstract void createDomain(UUID uuid, String hostname, Result result);

    public abstract void getReferredList(UUID uuid, Result result);

    //Creates a profile for the referred and adds entry referrer_uuid.
//    //Adds the referred uuid to the list of referred_uuids in the referrers profile.
//    public abstract void firstLogin(boolean async, UUID uuidReferred, UUID uuidReferrer);

    public abstract void close();
}
