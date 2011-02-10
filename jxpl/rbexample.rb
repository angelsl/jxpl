# Ruby example script, by Myers Carpenter of Bukkit forums.

require 'java'

scriptName = "RbExampleScript"
scriptVersion = "1"

Event = org.bukkit.event.Event
Level = java.util.logging.Level

def onEnable()
  $plugin.log(Level::INFO, "RbExampleScript loaded!")
  $plugin.registerEvent(Event::Type::PLAYER_JOIN, Event::Priority::Lowest, "onPlayerJoin")
end

def onDisable()
  $plugin.log(Level::INFO, "RbExampleScript unloaded!")
end

def onCommand(sender, command, label, args)
  return False
end

def onPlayerJoin(type, args)
  $plugin.getServer().broadcastMessage("RbExampleScript says hi, " + args.getPlayer().getName())
end