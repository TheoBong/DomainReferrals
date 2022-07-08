//package com.bongbong.referrals.database.mongo;
//
//import com.bongbong.referrals.ConfigValues;
//import com.bongbong.referrals.ReferralsPlugin;
//import com.bongbong.referrals.database.Database;
//import com.bongbong.referrals.utils.ThreadUtil;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.Filters;
//import com.mongodb.client.model.Updates;
//import org.bson.Document;
//import org.bson.UuidRepresentation;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class Mongo extends Database {
//    private final ReferralsPlugin plugin;
//    private final MongoDatabase mongoDatabase;
//
//    public Mongo(ReferralsPlugin plugin) {
//        this.plugin = plugin;
//
//        MongoClient mongoClient;
//        if (plugin.getConfig().getBoolean("DATABASE.MONGO.LOCALHOST_NO_AUTH")) {
//            mongoClient = MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build());
//            mongoDatabase = mongoClient.getDatabase(ConfigValues.MONGO_DATABASE.format(plugin));
//        } else {
//            MongoClientSettings mcs = MongoClientSettings.builder()
//                    .uuidRepresentation(UuidRepresentation.STANDARD)
//                    .applyConnectionString(new ConnectionString(plugin.getConfig().getString(ConfigValues.MONGO_URI.format(plugin))))
//                    .build();
//
//            mongoClient = MongoClients.create(mcs);
//            mongoDatabase = mongoClient.getDatabase(ConfigValues.MONGO_DATABASE.format(plugin));
//        }
//    }
//
//    public void referCheck(UUID uuid, String hostname) {
//        getDocument(false, "players", "_id", uuid, document -> {
//            if (document == null) {
//                getDocument(false, "players", "subdomain", hostname, document1 -> {
//                    if (document1 != null) {
//                        UUID referrerUUID = (UUID) document1.get("_id");
//
//                        //Referred player document
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("referrer", referrerUUID);
//
//                        MongoUpdate mu = new MongoUpdate("players", uuid);
//                        mu.setUpdate(map);
//                        massUpdate(false, mu);
//
//                        //Referrer document
//                        Map<String, Object> map2 = new HashMap<>();
//                        map2.put("referrer", referrerUUID);
//
//                        MongoUpdate mu2 = new MongoUpdate("players", uuid);
//                        mu2.setUpdate(map2);
//                        massUpdate(false, mu2);
//                    }
//                });
//            }
//        });
//    }
//
//    public void createDomain(UUID uuid, String hostname) {
//        getDocument(false, "players", "_id", uuid, document -> {
//            if (document != null) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("subdomain", hostname);
//
//                MongoUpdate mu = new MongoUpdate("players", uuid);
//                mu.setUpdate(map);
//                massUpdate(false, mu);
//            }
//        });
//    }
//
//    public Type type() {
//        return Type.MONGO;
//    }
//
//    public void close() {
//
//    }
//
//    public void getDocument(boolean async, String collectionName, String fieldName, Object id, MongoResult mongoResult) {
//        ThreadUtil.runTask(async, plugin, () -> {
//            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
//
//            if (collection.find(Filters.eq(fieldName, id)).iterator().hasNext()) {
//                mongoResult.call(collection.find(Filters.eq(fieldName, id)).first());
//            } else {
//                mongoResult.call(null);
//            }
//        });
//    }
//
//    public void massUpdate(boolean async, MongoUpdate mongoUpdate) {
//        massUpdate(async, mongoUpdate.getCollectionName(), mongoUpdate.getId(), mongoUpdate.getUpdate());
//    }
//
//    private void massUpdate(boolean async, String collectionName, Object id, Map<String, Object> updates) throws LinkageError {
//        ThreadUtil.runTask(async, plugin, () -> {
//            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
//
//            Document document = collection.find(new Document("_id", id)).first();
//            if(document == null) {
//                collection.insertOne(new Document("_id", id));
//            }
//
//            updates.forEach((key, value) -> collection.updateOne(Filters.eq("_id", id), Updates.set(key, value)));
//        });
//    }
//}
