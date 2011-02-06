importPackage(org.bukkit.event);
importPackage(java.util.logging);
/* Here we define the script name & version. 
 * This is REQUIRED! 
 */
scriptName = "ExampleScript";
scriptVersion = "1";

/* Here is the onEnable, onDisable, and onCommand functions.
 * These aren't required, but then exceptions would occur when they are called.
 */
function onEnable() { 
    plugin.log(Level.INFO, "ExampleScript loaded!"); 
    /* And here is how you register/handle events. */
    plugin.registerEvent(Event.Type.PLAYER_JOIN, Event.Priority.Lowest, "onPlayerJoin");
    /* And so on. Refer to Bukkit docs for event types and priorities */
}
function onDisable() { plugin.log(Level.INFO, "ExampleScript unloaded!"); }
function onCommand(sender, command, commandLabel, args) { return false; }

/* N.B. Above onCommand may become plugin.registerCommand(...) */
/* N.B. At the moment, there is no way to use the above way to handle commands. */
/* Handle PLAYER_COMMAND event instead */
/* Here is how you handle Events */
/* Note that method names are case-sensitive */
function onPlayerJoin(type, eventArgs)
{
    plugin.getServer().broadcastMessage("ExampleScript says hi, " + eventArgs.getPlayer().getName());
}
/* Final remarks:
 * Never try to access 'plugin' outside a method! 'plugin' is only defined immediately before enable is called.
 */
