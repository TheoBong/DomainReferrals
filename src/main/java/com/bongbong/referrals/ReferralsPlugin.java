package com.bongbong.referrals;

import com.bongbong.referrals.commands.BaseCommand;
import com.bongbong.referrals.commands.impl.ReferCommand;
import com.bongbong.referrals.database.Database;
import com.bongbong.referrals.database.sequel.SQL;
import com.bongbong.referrals.listeners.JoinListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class ReferralsPlugin extends JavaPlugin {

    private CommandMap commandMap;
    @Getter private Database storage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        switch (getConfig().getString("DATABASE.USE").toLowerCase()) {
//            case "mongo":
//                storage = new Mongo(this);
//                break;
            case "mysql":
                storage = new SQL(this, Database.Type.MySQL);
                break;
            case "sqlite":
                storage = new SQL(this, Database.Type.SQLite);
                break;
            default:
                getLogger().log(Level.SEVERE, "YOU MUST SELECT EITHER MONGO, MYSQL, OR SQLITE IN THE CONFIG!");
                onDisable();
                break;
        }

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerListener(new JoinListener(this));

        registerCommand(new ReferCommand(this, "refer"));
    }

    @Override
    public void onDisable() {
        storage.close();
    }

    public void registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }
}
