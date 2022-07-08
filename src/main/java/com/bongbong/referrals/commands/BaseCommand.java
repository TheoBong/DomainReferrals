package com.bongbong.referrals.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public abstract class BaseCommand extends Command {

    public BaseCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        execute(sender, args);
        return true;
    }

    public void setAliases(String... aliases) {
        if (aliases.length > 0) {
            this.setAliases(aliases.length == 1 ? Collections.singletonList(aliases[0]) : Arrays.asList(aliases));
        }
    }

    public Player getPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            sender.sendMessage("Only players!");
            return null;
        }
    }

    protected abstract void execute(CommandSender sender, String[] args);
}
