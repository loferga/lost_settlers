This project is a little minecraft minigame where the goal is either to capture all the ennemies flags or killing all the ennemies,
the core gameplay is a defense/capture the flag where you and your team build a defense in a camp that only you is able to place, break and interact with blocs in it,
the only things ennemies can do in your camp is placing/destroying and igniting tnt.
All the ressources you will need has to be gathered by leaving your camp letting it defenseless against attacks.
Some rules on the vanilla game are here to balance the game but the gameplay is pretty vanilla with random generated ores

Rules:
No Shields
Only Overworld
Ender Pearls does highly more damages based on your remaining health
Arrows with bonus effects does no damages to any player hited
You drop 100% of your XP when you die
All Ores give you XP and it gradualy increase with the ore type and it's doubled if it's in deepslate
If you take damages you won't be able to regain health within a specific amount of seconds
All Fireworks you craft take your team color as their color
Invisibility effect no longer have particles, but other effects have
If you smith an item to netherite, all enchantments will increase by 1 even if it's not vanilla-friendly
You can updrage all already existent enchantment of an item to the vanilla maximum by putting it in an enchanting table
A dead can be respawned if one of his team mate kill a member of the team of his killer
If you die alone (if a player haven't hurt you directly or indirectly (traps) within a fixed amount of seconds) you will create a tombstone and respawn
If you cannot respawn (your team does not have any camp) you will be placed in a queue, as soon as your team get a camp you will respawn in it

This plugins messages are in French but errors are in english

HOW TO USE:
you need maps to run the plugin, you can import pre built maps in the server folder OR
you can create one by specifying a world that does not exist in the config.yml
all parameters are optional but you need at least to write something to get your world to load, they are referenced in the config.yml as comments

now that you have a world you can warp to the world by using /warp command to edit it or check if it's set,
/warp teleports you to the center of world (or world_spawn), to set the center of the world to the center of the camps you need to specify the camps
start a game on that world by using /start command.
when you're done you can close the world and either save the edit you made or restore to it's default state (before warp)

pre_load field is false by default but if you have a good config you can set it to true
it load worlds on the launch of the server (before a game) and after you closed it
