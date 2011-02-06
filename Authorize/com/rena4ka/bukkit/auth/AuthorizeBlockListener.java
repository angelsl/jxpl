/*    */
package com.rena4ka.bukkit.auth;
/*    */
/*    */

import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/*    */
/*    */
/*    */

/*    */
/*    */ public class AuthorizeBlockListener extends BlockListener
/*    */ {
    /*    */   private final Authorize plugin;

    /*    */
/*    */
    public AuthorizeBlockListener(Authorize instance)
/*    */ {
/* 17 */
        this.plugin = instance;
/*    */
    }

    /*    */
/*    */
    public void onBlockPlace(BlockPlaceEvent event) {
/* 21 */
        if (!this.plugin.isAuthorized(event.getPlayer().getEntityId()))
/* 22 */ event.setCancelled(true);
/*    */
    }

    /*    */
/*    */
    public void onBlockDamage(BlockDamageEvent event) {
/* 26 */
        if (!this.plugin.isAuthorized(event.getPlayer().getEntityId()))
/* 27 */ event.setCancelled(true);
/*    */
    }
/*    */
}

/* Location:           D:\Misc\Games\Minecraft\Beta\Development\Server Bukkit\Authorize.jar
 * Qualified Name:     com.rena4ka.bukkit.auth.AuthorizeBlockListener
 * JD-Core Version:    0.6.0
 */