package com.bongbong.referrals.commands.impl;

import com.bongbong.referrals.ConfigValues;
import com.bongbong.referrals.Locale;
import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.commands.BaseCommand;
import com.bongbong.referrals.utils.ThreadUtil;
import com.bongbong.referrals.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReferCommand extends BaseCommand {
    private final ReferralsPlugin plugin;

    public ReferCommand(ReferralsPlugin plugin, String name, String... aliases) {
        super(name);
        this.plugin = plugin;
        this.setAliases(aliases);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        ThreadUtil.runTask(true, plugin, () -> {
            if (args.length < 1) {
                sendHelp(sender);
                return;
            }

            Player player;
            Player target;
            UUID targetUUID;
            switch (args[0]) {
                case "create":
                    if (!sender.hasPermission(Locale.CREATE_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.ONLY_PLAYERS.format(plugin));
                        return;
                    }

                    player = (Player) sender;

                    if (args.length < 2) {
                        sendHelp(sender);
                        return;
                    }

                    String[] separated = args[1].split("\\.");
                    String subdomain = separated[0];

                    for (String serverDomain : ConfigValues.SERVER_SUBDOMAINS.formatLines(plugin)) {
                        if (subdomain.equals(serverDomain)) {
                            sender.sendMessage(Locale.NO_ORIGIN.format(plugin));
                            return;
                        }
                    }


                    plugin.getStorage().createDomain(player.getUniqueId(), subdomain, result -> {
                        if (result instanceof Boolean) {
                            boolean success = (boolean) result;
                            if (success) {
                                List<String> list = new ArrayList<>();
                                for (String string : Locale.SUBDOMAIN_CLAIMED.formatLines(plugin)) {
                                    list.add(string.replace("{subdomain}", subdomain));
                                }

                                player.sendMessage(String.join("\n", list));
                            } else {
                                player.sendMessage(Locale.SUBDOMAIN_CLAIM_FAIL.format(plugin));
                            }
                        }
                    });

                    break;
                case "rewards":
                    sender.sendMessage("Rewards is work in progress");
                    break;
                case "check":
                    if (!sender.hasPermission(Locale.CHECK_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }
                    if (args.length < 2) {
                        if (sender instanceof Player) {
                            target = (Player) sender;
                        } else {
                            sendHelp(sender);
                            break;
                        }
                    } else {
                        target = Bukkit.getPlayer(args[1]);
                    }

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    if (targetUUID == null) {
                        sender.sendMessage(Locale.PLAYER_INVALID.format(plugin));
                        return;
                    }

                    plugin.getStorage().getReferredList(targetUUID, result -> {
                        if (result == null) {
                            sender.sendMessage(Locale.PLAYER_CHECK_FAIL.format(plugin).replace("{target}", args[1]));
                            return;
                        }

                        String referredString = (String) result;
                        String[] test = referredString.split(",");
                        List<String> referredList = new ArrayList<>(Arrays.asList(test));

                        referredList.forEach(sender::sendMessage);

                        sender.sendMessage(Locale.PLAYER_CHECK_SUCCESS.format(plugin)
                                .replace("{amount}", referredList.size() + "")
                                .replace("{target}", args[1])
                        );
                    });
                    break;
                case "checkdomain":
                    if (!sender.hasPermission(Locale.CHECK_DOMAIN_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }
                    if (args.length < 2) {
                        sendHelp(sender);
                        break;
                    }

                    plugin.getStorage().checkDomain(args[1], result -> {
                        if (result == null) {
                            sender.sendMessage(Locale.DOMAIN_CHECK_FAIL.format(plugin));
                            return;
                        }

                        UUID uuid = UUID.fromString((String) result);

                        WebPlayer webPlayer = new WebPlayer(uuid);
                        String targetName = webPlayer.getName();
                        sender.sendMessage(Locale.DOMAIN_CHECK_SUCCESS.format(plugin).replace("{target}", targetName));

                        plugin.getStorage().getReferredList(uuid, result1 -> {
                            if (result1 == null) {
                                sender.sendMessage(Locale.PLAYER_CHECK_FAIL.format(plugin).replace("{target}", targetName));
                                return;
                            }

                            String referredString = (String) result1;
                            String[] test = referredString.split(",");
                            List<String> referredList = new ArrayList<>(Arrays.asList(test));

                            referredList.forEach(sender::sendMessage);
                            sender.sendMessage(Locale.PLAYER_CHECK_SUCCESS.format(plugin)
                                    .replace("{amount}", referredList.size() + "")
                                    .replace("{target}", targetName)
                            );
                        });
                    });
                    break;
                case "reset":
                    if (!sender.hasPermission(Locale.RESET_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }

                    if (args.length < 2) {
                        sendHelp(sender);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    if (targetUUID == null) {
                        sender.sendMessage(Locale.PLAYER_INVALID.format(plugin));
                        return;
                    }

                    plugin.getStorage().resetReferrals(targetUUID, result -> {
                        boolean success = (boolean) result;
                        if (success) {
                            sender.sendMessage(Locale.RESET_SUCCESS.format(plugin).replace("{target}", args[1]));
                        } else {
                            sender.sendMessage(Locale.RESET_FAIL.format(plugin).replace("{target}", args[1]));
                        }
                    });
                    break;
                case "reload":
                    if (!sender.hasPermission(Locale.RELOAD_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }

                    plugin.reloadMessages();
                    sender.sendMessage(Locale.RELOAD_MESSAGE.format(plugin));
                    break;
                case "setgroup":
                    if (!sender.hasPermission(Locale.SETGROUP_PERMISSION.format(plugin))) {
                        sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                        return;
                    }
                    if (args.length < 3) {
                        sendHelp(sender);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    String group = args[2];

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    if (targetUUID == null) {
                        sender.sendMessage(Locale.PLAYER_INVALID.format(plugin));
                        return;
                    }

                    plugin.getStorage().setGroup(targetUUID, group, result -> {
                        boolean success = (boolean) result;
                        if (success) {
                            sender.sendMessage(Locale.SETGROUP_SUCCESS.format(plugin)
                                    .replace("{target}", args[1])
                                    .replace("{group}", group));
                        } else {
                            sender.sendMessage(Locale.SETGROUP_FAIL.format(plugin));
                        }
                    });
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        });
    }

    private void sendHelp(CommandSender sender) {
        boolean createPermission = sender.hasPermission(Locale.CREATE_PERMISSION.format(plugin));
        boolean checkPermission = sender.hasPermission(Locale.CHECK_PERMISSION.format(plugin));
        boolean checkDomainPermission = sender.hasPermission(Locale.CHECK_DOMAIN_PERMISSION.format(plugin));
        boolean resetPermission = sender.hasPermission(Locale.RESET_PERMISSION.format(plugin));
        boolean reloadPermission = sender.hasPermission(Locale.RELOAD_PERMISSION.format(plugin));
        boolean setGroupPermission = sender.hasPermission(Locale.SETGROUP_PERMISSION.format(plugin));

        if (!(createPermission || checkPermission || checkDomainPermission || resetPermission || reloadPermission || setGroupPermission)) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        List<String> list = new ArrayList<>(Locale.HELP_MESSAGE_START.formatLines(plugin));

        if (createPermission) list.add(Locale.HELP_CREATE.format(plugin));
        if (checkPermission) list.add(Locale.HELP_CHECK.format(plugin));
        if (checkDomainPermission) list.add(Locale.HELP_CHECK_DOMAIN.format(plugin));
        if (resetPermission) list.add(Locale.HELP_RESET.format(plugin));
        if (reloadPermission) list.add(Locale.HELP_RELOAD.format(plugin));
        if (setGroupPermission) list.add(Locale.HELP_SETGROUP.format(plugin));

        list.addAll(Locale.HELP_MESSAGE_END.formatLines(plugin));

        sender.sendMessage(String.join("\n", list));
    }
}
