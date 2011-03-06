/*     */
package com.rena4ka.bukkit.auth;
/*     */
/*     */

import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ public class AuthorizePlayerListener extends PlayerListener
/*     */ {
    /*     */   private final Authorize plugin;

    /*     */
/*     */
    public AuthorizePlayerListener(Authorize instance)
/*     */ {
/*  26 */
        this.plugin = instance;
/*     */
    }

    /*     */
/*     */
    public void onPlayerJoin(PlayerEvent event)
/*     */ {
/*  31 */
        Player player = event.getPlayer();
/*     */
        try {
/*  33 */
            if (this.plugin.isRegistered(player.getName())) {
/*  34 */
                this.plugin.storeInventory(player.getName(), player.getInventory().getContents());
/*  35 */
                player.getInventory().clear();
/*  36 */
                player.sendMessage(this.plugin.loginMessage);
/*  37 */
            } else if (this.plugin.forceRegister) {
/*  38 */
                this.plugin.storeInventory(player.getName(), player.getInventory().getContents());
/*  39 */
                player.getInventory().clear();
/*  40 */
                player.sendMessage(this.plugin.registerMessage);
/*     */
            } else {
/*  42 */
                this.plugin.authorize(event.getPlayer().getEntityId());
/*     */
            }
/*     */
        } catch (IOException e) {
/*  44 */
            this.plugin.log.severe("[Authorize] Inventory file error:");
/*  45 */
            e.printStackTrace();
/*  46 */
            player.sendMessage(Color.red + "Error happend, report to admins!");
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void onPlayerQuit(PlayerEvent event)
/*     */ {
/*  52 */
        Player player = event.getPlayer();
/*  53 */
        ItemStack[] inv = this.plugin.getInventory(player.getName());
/*  54 */
        if ((inv != null) && (!this.plugin.isAuthorized(player.getEntityId()))) {
/*  55 */
            player.getInventory().setContents(inv);
/*  56 */
            player.kickPlayer("inventory protection kicked");
/*     */
        }
/*  58 */
        this.plugin.unauthorize(player.getEntityId());
/*     */
    }

    /*     */
/*     */
    public void onPlayerCommandPreprocess(PlayerChatEvent event)
/*     */ {
/*  63 */
        String[] split = event.getMessage().split(" ");
/*  64 */
        Player player = event.getPlayer();
/*  65 */
        if (split[0].equals("/login")) {
/*  66 */
            if (this.plugin.isAuthorized(player.getEntityId())) {
/*  67 */
                player.sendMessage(this.plugin.authorizedMessage);
/*     */
            }
/*  69 */
            else if (split.length < 2) {
/*  70 */
                player.sendMessage(this.plugin.loginUsageMessage);
/*     */
            }
/*  72 */
            else if (this.plugin.checkPassword(player.getName(), split[1])) {
/*  73 */
                ItemStack[] inv = this.plugin.getInventory(player.getName());
/*  74 */
                if (inv != null)
/*  75 */ player.getInventory().setContents(inv);
/*  76 */
                this.plugin.authorize(player.getEntityId());
/*  77 */
                player.sendMessage(this.plugin.passwordAcceptedMessage);
/*  78 */
            } else if (this.plugin.kickOnBadPassword) {
/*  79 */
                ItemStack[] inv = this.plugin.getInventory(player.getName());
/*  80 */
                if (inv != null)
/*  81 */ player.getInventory().setContents(inv);
/*  82 */
                player.kickPlayer(this.plugin.badPasswordMessage);
/*     */
            } else {
/*  84 */
                player.sendMessage(this.plugin.badPasswordMessage);
/*     */
            }
/*  86 */
            event.setMessage("/login ******");
/*  87 */
            event.setCancelled(true);
/*  88 */
        } else if (split[0].equals("/register")) {
/*  89 */
            if (split.length < 2) {
/*  90 */
                player.sendMessage(this.plugin.registerUsageMessage);
/*     */
            }
/*  92 */
            else if (this.plugin.isRegistered(player.getName()))
/*  93 */ player.sendMessage(this.plugin.alreadyRegisteredMessage);
/*  94 */
            else if (!this.plugin.allowRegister)
/*  95 */ player.sendMessage(this.plugin.registrationNotAllowedMessage);
/*  96 */
            else if ((this.plugin.requireEmail) && (split.length < 3))
/*  97 */ player.sendMessage(this.plugin.emailRequiredMessage);
/*  98 */
            else if ((split.length >= 3) && (!this.plugin.checkEmail(split[2])))
/*  99 */ player.sendMessage(this.plugin.emailUnexpectedMessage);
/*     */
            else {
/*     */
                try {
/* 102 */
                    if (split.length >= 3)
/* 103 */ this.plugin.register(player.getName(), split[1], split[2]);
/*     */
                    else
/* 105 */             this.plugin.register(player.getName(), split[1]);
/* 106 */
                    ItemStack[] inv = this.plugin.getInventory(player.getName());
/* 107 */
                    if (inv != null)
/* 108 */ player.getInventory().setContents(inv);
/* 109 */
                    player.sendMessage(this.plugin.registeredMessage);
/* 110 */
                    this.plugin.authorize(player.getEntityId());
/*     */
                } catch (IOException e) {
/* 112 */
                    player.sendMessage(this.plugin.registerErrorMessage);
/* 113 */
                    e.printStackTrace();
/*     */
                } catch (SQLException e) {
/* 115 */
                    player.sendMessage(this.plugin.registerErrorMessage);
/* 116 */
                    e.printStackTrace();
/*     */
                }
/*     */
            }
/* 119 */
            event.setMessage("/register *****");
/* 120 */
            event.setCancelled(true);
/* 121 */
        } else if (split[0].equals("/password")) {
/* 122 */
            if (split.length < 3) {
/* 123 */
                player.sendMessage(this.plugin.passwordUsageMessage);
/*     */
            }
/* 125 */
            else if (!this.plugin.isRegistered(player.getName()))
/* 126 */ player.sendMessage(this.plugin.passwordNotRegisteredMessage);
/* 127 */
            else if (!this.plugin.allowPassChange)
/* 128 */ player.sendMessage(this.plugin.passNotAllowedMessage);
/* 129 */
            else if (!this.plugin.checkPassword(player.getName(), split[1]))
/* 130 */ player.sendMessage(this.plugin.badOldPasswordMessage);
/*     */
            else {
/*     */
                try {
/* 133 */
                    this.plugin.changePassword(player.getName(), split[2]);
/* 134 */
                    player.sendMessage(this.plugin.passwordChangedMessage);
/*     */
                } catch (IOException e) {
/* 136 */
                    player.sendMessage(this.plugin.passwordChangeErrorMessage);
/* 137 */
                    e.printStackTrace();
/*     */
                } catch (SQLException e) {
/* 139 */
                    player.sendMessage(this.plugin.passwordChangeErrorMessage);
/* 140 */
                    e.printStackTrace();
/*     */
                }
/*     */
            }
/* 143 */
            event.setMessage("/password ***** *****");
/* 144 */
            event.setCancelled(true);
/* 145 */
        } else if (split[0].equals("/email")) {
/* 146 */
            if (split.length < 3)
/* 147 */ player.sendMessage(this.plugin.emailUsageMessage);
/* 148 */
            else if (!this.plugin.allowEmailChange)
/* 149 */ player.sendMessage(this.plugin.emailChangeNotAllowedMessage);
/* 150 */
            else if (!this.plugin.checkEmail(split[2])) {
/* 151 */
                player.sendMessage(this.plugin.emailUnexpectedMessage);
/*     */
            }
/* 153 */
            else if (this.plugin.checkPassword(player.getName(), split[1]))
/* 154 */ player.sendMessage(this.plugin.badPasswordMessage);
/*     */
            else {
/*     */
                try {
/* 157 */
                    this.plugin.changeEmail(player.getName(), split[2]);
/* 158 */
                    player.sendMessage(this.plugin.emailChangedMessage);
/*     */
                } catch (SQLException e) {
/* 160 */
                    player.sendMessage(this.plugin.emailChangeErrorMessage);
/* 161 */
                    e.printStackTrace();
/*     */
                }
/*     */
            }
/* 164 */
            event.setMessage("/email ***** *****");
/* 165 */
            event.setCancelled(true);
/* 166 */
        } else if (split[0].equals("/unregister")) {
/* 167 */
            if (split.length < 2) {
/* 168 */
                player.sendMessage(this.plugin.unregisterUsageMessage);
/*     */
            }
/* 170 */
            else if (!this.plugin.isRegistered(player.getName()))
/* 171 */ player.sendMessage(this.plugin.unregisterNotRegisteredMessage);
/* 172 */
            else if (!this.plugin.allowUnregister)
/* 173 */ player.sendMessage(this.plugin.unregNotAllowedMessage);
/* 174 */
            else if (!this.plugin.checkPassword(player.getName(), split[1]))
/* 175 */ player.sendMessage(this.plugin.badPasswordMessage);
/*     */
            else {
/*     */
                try {
/* 178 */
                    this.plugin.unregister(player.getName());
/* 179 */
                    player.sendMessage(this.plugin.unregisteredMessage);
/*     */
                } catch (IOException e) {
/* 181 */
                    player.sendMessage(this.plugin.unregisterErrorMessage);
/* 182 */
                    e.printStackTrace();
/*     */
                } catch (SQLException e) {
/* 184 */
                    player.sendMessage(this.plugin.unregisterErrorMessage);
/* 185 */
                    e.printStackTrace();
/*     */
                }
/*     */
            }
/* 188 */
            event.setMessage("/unregiter *****");
/* 189 */
            event.setCancelled(true);
/* 190 */
        } else if (split[0].equals("/loginreset")) {
/* 191 */
            if (!this.plugin.isAdmin(player.getName()))
/* 192 */ player.sendMessage(this.plugin.notAdminMessage);
/* 193 */
            else if (split.length < 2)
/* 194 */ player.sendMessage(this.plugin.resetUsageMessage);
/*     */
            else
/*     */         try {
/* 197 */
                    this.plugin.unregister(split[1]);
/* 198 */
                    player.sendMessage(split[1] + " " + this.plugin.userResettedMessage);
/*     */
                } catch (SQLException e) {
/* 200 */
                    player.sendMessage("Error happend!");
/* 201 */
                    e.printStackTrace();
/*     */
                } catch (IOException e) {
/* 203 */
                    player.sendMessage("Error happend!");
/* 204 */
                    e.printStackTrace();
/*     */
                }
/*     */
        }
/* 207 */
        else if (split[0].equals("/loginreload")) {
/* 208 */
            if (!this.plugin.isAdmin(player.getName())) {
/* 209 */
                player.sendMessage(this.plugin.notAdminMessage);
/*     */
            } else {
/* 211 */
                this.plugin.onDisable();
/* 212 */
                this.plugin.onEnable();
/* 213 */
                player.sendMessage(this.plugin.reloadedMessage);
/*     */
            }
/* 215 */
        } else if (!this.plugin.isAuthorized(player.getEntityId())) {
/* 216 */
            event.setCancelled(true);
/* 217 */
            event.setMessage("/authorize_notloggedin_donothandlethis");
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void onPlayerMove(PlayerMoveEvent event)
/*     */ {
/* 223 */
        if (!this.plugin.isAuthorized(event.getPlayer().getEntityId())) {
/* 224 */
            event.setCancelled(true);
/* 225 */
            event.getPlayer().teleportTo(event.getFrom());
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void onPlayerChat(PlayerChatEvent event)
/*     */ {
/* 231 */
        if (!this.plugin.isAuthorized(event.getPlayer().getEntityId()))
/* 232 */ event.setCancelled(true);
/*     */
    }

    /*     */
/*     */
    public void onPlayerItem(PlayerItemEvent event)
/*     */ {
/* 237 */
        if (!this.plugin.isAuthorized(event.getPlayer().getEntityId()))
/* 238 */ event.setCancelled(true);
/*     */
    }
/*     */
}

/* Location:           D:\Misc\Games\Minecraft\Beta\Development\Server Bukkit\Authorize.jar
 * Qualified Name:     com.rena4ka.bukkit.auth.AuthorizePlayerListener
 * JD-Core Version:    0.6.0
 */