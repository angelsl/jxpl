# Ruby example script, by Myers Carpenter of Bukkit forums.

require 'java'

scriptName = "RbExampleScript"
scriptVersion = "1"
HELPER_VARIABLE_NAME = "helper"
PLUGIN_VARIABLE_NAME = "plugin"
SERVER_VARIABLE_NAME = "server"

Event = org.bukkit.event.Event
Level = java.util.logging.Level

def onEnable()
  $helper.log(Level::INFO, "RbExampleScript loaded!")
  $helper.registerEvent(Event::Type::PLAYER_JOIN, Event::Priority::Lowest, "onPlayerJoin")
end

def onDisable()
  $helper.log(Level::INFO, "RbExampleScript unloaded!")
end

def onCommand(sender, command, label, args)
  return False
end

def onPlayerJoin(type, args)
  $server.broadcastMessage("RbExampleScript says hi, " + args.getPlayer().getName())
end