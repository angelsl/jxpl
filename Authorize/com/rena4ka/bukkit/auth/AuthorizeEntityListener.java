/*    */
package com.rena4ka.bukkit.auth;
/*    */
/*    */

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

/*    */
/*    */
/*    */

/*    */
/*    */ public class AuthorizeEntityListener extends EntityListener
/*    */ {
    /*    */   private final Authorize plugin;

    /*    */
/*    */
    public AuthorizeEntityListener(Authorize instance)
/*    */ {
/* 18 */
        this.plugin = instance;
/*    */
    }

    /*    */
/*    */
    public void onEntityDamage(EntityDamageEvent event) {
/* 22 */
        if (((event.getEntity() instanceof Player)) &&
/* 23 */       (!this.plugin.isAuthorized(event.getEntity().getEntityId())))
/* 24 */ event.setCancelled(true);
/*    */
    }
/*    */
}

/* Location:           D:\Misc\Games\Minecraft\Beta\Development\Server Bukkit\Authorize.jar
 * Qualified Name:     com.rena4ka.bukkit.auth.AuthorizeEntityListener
 * JD-Core Version:    0.6.0
 */