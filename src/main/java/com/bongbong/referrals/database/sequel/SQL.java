package com.bongbong.referrals.database.sequel;

import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.database.Database;
import com.bongbong.referrals.database.Result;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class SQL extends Database {

    private final ReferralsPlugin plugin;
    private Connection connection;
    private final FileConfiguration config;
    private final Type type;

    public SQL(ReferralsPlugin plugin, Type type) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.type = type;

        openDatabaseConnection();
    }

    public void referCheck(UUID uuid, String hostname) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (!rs.next()) {
                PreparedStatement ps1 = getConnection().prepareStatement("SELECT * FROM players WHERE subdomain = ?");
                ps1.setString(1, hostname);
                ResultSet rs1 = ps1.executeQuery();

                if (plugin.getStorage().type() == Database.Type.MySQL) {
                    rs1.beforeFirst();
                }

                String referrer = rs1.next() ? rs1.getString("id") : null;

                PreparedStatement ps2 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain) VALUES (?,?,?,?)");
                ps2.setString(1, uuid.toString());
                ps2.setString(2, referrer);
                ps2.setString(3, null);
                ps2.setString(4, null);
                ps2.executeUpdate();

                if (referrer == null) {
                    return;
                }

                String referredListString;
                if (rs1.getString("referred") == null) {
                    referredListString = uuid.toString();
                } else {
                    String[] test = rs.getString("referred").split(",");
                    List<String> referredList = new ArrayList<>(Arrays.asList(test));
                    referredList.add(uuid.toString());

                    referredListString = String.join(",", referredList);
                }

                PreparedStatement ps3 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain) VALUES (?,?,?,?)");
                ps3.setString(1, rs1.getString("id"));
                ps3.setString(2, rs1.getString("referrer"));
                ps3.setString(3, referredListString);
                ps3.setString(4, rs1.getString("subdomain"));
                ps3.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void createDomain(UUID uuid, String hostname, Result result) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE subdomain = ?");
            ps.setString(1, hostname);
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (rs.next()) {
                result.call(false);
                return;
            }

            PreparedStatement ps1 = getConnection().prepareStatement("SELECT * FROM players WHERE id = ?");
            ps1.setString(1, uuid.toString());
            ResultSet rs1 = ps1.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs1.beforeFirst();
            }

            if (rs1.next()) {
                PreparedStatement ps2 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain) VALUES (?,?,?,?)");
                ps2.setString(1, rs1.getString("id"));
                ps2.setString(2, rs1.getString("referrer"));
                ps2.setString(3, rs1.getString("referred"));
                ps2.setString(4, hostname);
                ps2.executeUpdate();
            }

            result.call(true);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void getReferredList(UUID uuid, Result result) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (rs.next()) {
                result.call(rs.getString("referred"));
            } else {
                result.call(null);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Type type() {
        return type;
    }
    public void close() {
        closeConnection();
    }

    private void openDatabaseConnection() {
        try {
            if (type.equals(Type.SQLite)) {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException localClassNotFoundException) {
                    localClassNotFoundException.printStackTrace();
                }
                File localFile = new File(plugin.getDataFolder().toString() + File.separator + "data.db");
                if (!localFile.exists()) {
                    try {
                        localFile.createNewFile();
                    } catch (IOException localIOException) {
                        localIOException.printStackTrace();
                    }
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + localFile.getAbsolutePath());
            } else {
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("DATABASE.MYSQL.HOST") + ":" + config.getString("DATABASE.MYSQL.PORT") + "/" + config.getString("DATABASE.MYSQL.DATABASE") + "?autoReconnect=true", config.getString("DATABASE.MYSQL.USER"), config.getString("DATABASE.MYSQL.PASSWORD"));
            }

            Connection con = getConnection();

            PreparedStatement stat = con.prepareStatement("CREATE TABLE IF NOT EXISTS players (id VARCHAR(36) PRIMARY KEY, referrer VARCHAR(36), referred LONGTEXT, subdomain LONGTEXT);");
            stat.execute();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            if ((connection == null) || (connection.isClosed())) {
                openDatabaseConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}


