from org.bukkit.event import Event
from java.util.logging import Level

scriptName = "PyExampleScript"
scriptVersion = "1"
HELPER_VARIABLE_NAME = "helper"
PLUGIN_VARIABLE_NAME = "plugin"
SERVER_VARIABLE_NAME = "server"

def onEnable():
    helper.log(Level.INFO, "PyExampleScript loaded!")
    helper.registerEvent(Event.Type.PLAYER_JOIN, Event.Priority.Lowest, "onPlayerJoin")

def onDisable():
    helper.log(Level.INFO, "PyExampleScript unloaded!")

def onPlayerJoin(type, args):
    server.broadcastMessage("PyExampleScript says hi, " + args.getPlayer().getName())


   