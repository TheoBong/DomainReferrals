package com.bongbong.referrals.database.sequel;

import com.bongbong.referrals.ConfigValues;
import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.database.Database;
import com.bongbong.referrals.database.Result;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SQL extends Database {

    private final ReferralsPlugin plugin;
    private Connection connection;
    private final Type type;

    public SQL(ReferralsPlugin plugin, Type type) {
        this.plugin = plugin;
        this.type = type;

        openDatabaseConnection();
    }

    public void referCheck(UUID uuid, String hostname, Result result) {
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

                PreparedStatement ps2 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain, rewards_group, claimed_rewards) VALUES (?,?,?,?,?,?)");
                ps2.setString(1, uuid.toString());
                ps2.setString(2, referrer);
                ps2.setString(3, null);
                ps2.setString(4, null);
                ps2.setString(5, ConfigValues.DEFAULT_REWARD_GROUP.format(plugin));
                ps2.setString(6, null);
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

                PreparedStatement ps3 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain, rewards_group, claimed_rewards) VALUES (?,?,?,?,?,?)");
                ps3.setString(1, rs1.getString("id"));
                ps3.setString(2, rs1.getString("referrer"));
                ps3.setString(3, referredListString);
                ps3.setString(4, rs1.getString("subdomain"));
                ps3.setString(5, rs1.getString("rewards_group"));
                ps3.setString(6, rs1.getString("claimed_rewards"));
                ps3.executeUpdate();

                result.call(rs1.getString("rewards_group"));
            } else {
                result.call(null);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            result.call(null);
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
                PreparedStatement ps2 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain, rewards_group, claimed_rewards) VALUES (?,?,?,?,?,?)");
                ps2.setString(1, rs1.getString("id"));
                ps2.setString(2, rs1.getString("referrer"));
                ps2.setString(3, rs1.getString("referred"));
                ps2.setString(4, hostname);
                ps2.setString(5, rs1.getString("rewards_group"));
                ps2.setString(6, rs1.getString("claimed_rewards"));
                ps2.executeUpdate();
            }

            result.call(true);
        } catch (SQLException exception) {
            exception.printStackTrace();
            result.call(false);
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
            result.call(null);
        }
    }

    public void setGroup(UUID uuid, String group, Result result) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (rs.next()) {
                PreparedStatement ps1 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain, rewards_group, claimed_rewards) VALUES (?,?,?,?,?,?)");
                ps1.setString(1, rs.getString("id"));
                ps1.setString(2, rs.getString("referrer"));
                ps1.setString(3, rs.getString("referred"));
                ps1.setString(4, rs.getString("subdomain"));
                ps1.setString(5, group);
                ps1.setString(6, rs.getString("claimed_rewards"));
                ps1.executeUpdate();

                result.call(true);
            } else {
                result.call(false);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            result.call(false);
        }
    }

    public void resetReferrals(UUID uuid, Result result) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (rs.next()) {
                PreparedStatement ps1 = getConnection().prepareStatement("REPLACE INTO players(id, referrer, referred, subdomain, rewards_group, claimed_rewards) VALUES (?,?,?,?,?,?)");
                ps1.setString(1, rs.getString("id"));
                ps1.setString(2, rs.getString("referrer"));
                ps1.setString(3, rs.getString(null));
                ps1.setString(4, rs.getString("subdomain"));
                ps1.setString(5, rs.getString("rewards_group"));
                ps1.setString(6, rs.getString("claimed_rewards"));
                ps1.executeUpdate();

                result.call(true);
            } else {
                result.call(false);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            result.call(false);
        }
    }

    public void checkDomain(String subdomain, Result result) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM players WHERE subdomain = ?");
            ps.setString(1, subdomain);
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
            }

            if (rs.next()) {
                result.call(rs.getString("id"));
            } else {
                result.call(null);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            result.call(null);
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
                connection = DriverManager.getConnection("jdbc:mysql://" + ConfigValues.SQL_HOST.format(plugin) + ":" + ConfigValues.SQL_PORT.format(plugin) + "/" + ConfigValues.SQL_DATABASE.format(plugin) + "?autoReconnect=true", ConfigValues.SQL_USER.format(plugin), ConfigValues.SQL_PASSWORD.format(plugin));
            }

            Connection con = getConnection();

            PreparedStatement stat = con.prepareStatement("CREATE TABLE IF NOT EXISTS players (id VARCHAR(36) PRIMARY KEY, referrer VARCHAR(36), referred LONGTEXT, subdomain LONGTEXT, rewards_group LONGTEXT, claimed_rewards LONGTEXT);");
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


