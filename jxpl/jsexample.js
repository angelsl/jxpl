importPackage(org.bukkit.event);
importPackage(java.util.logging);
/* Here we define the script name & version. 
 * This is REQUIRED! 
 */
scriptName = "JsExampleScript";
scriptVersion = "1";
// New: change the variable name for the 3 preset variables.
// If not specified, the values below are used as default.
HELPER_VARIABLE_NAME = "helper"
PLUGIN_VARIABLE_NAME = "plugin"
SERVER_VARIABLE_NAME = "server"

/* Here are the onEnable and onDisable functions.
 * These aren't required, but then exceptions would occur when they are called.
 */
function onEnable() { 
    helper.log(Level.INFO, "JsExampleScript loaded!"); 
    /* And here is how you register/handle events. */
    helper.registerEvent(Event.Type.PLAYER_JOIN, Event.Priority.Lowest, "onPlayerJoin");
    /* And so on. Refer to Bukkit docs for event types and priorities */
}
function onDisable() { helper.log(Level.INFO, "ExampleScript unloaded!"); }

/* Here is how you handle Events */
/* Note that method names are case-sensitive */
function onPlayerJoin(type, eventArgs)
{
    server.broadcastMessage("JsExampleScript says hi, " + eventArgs.getPlayer().getName());
}
/* Final remarks:
 * Never try to access helper, plugin or server outside a function! They are only defined immediately before enable is called, so if you try to access them, your script will not load.
 */
