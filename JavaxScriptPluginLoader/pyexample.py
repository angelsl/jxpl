from org.bukkit.event import Event
from java.util.logging import Level

scriptName = "PyExampleScript"
scriptVersion = "1"

def onEnable():
    plugin.log(Level.INFO, "PyExampleScript loaded!")
    plugin.registerEvent(Event.Type.PLAYER_JOIN, Event.Priority.Lowest, "onPlayerJoin")

def onDisable():
    plugin.log(Level.INFO, "PyExampleScript unloaded!")

def onCommand(sender, command, label, args):
    return False

def onPlayerJoin(type, args):
    plugin.getServer().broadcastMessage("PyExampleScript says hi, " + args.getPlayer().getName())


   