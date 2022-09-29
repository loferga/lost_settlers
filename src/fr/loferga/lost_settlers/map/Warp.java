package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class Warp implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();
		Set<String> mapNames = Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps").getKeys(false);
		if (args.length == 1)
			res.addAll(Func.matches(mapNames, args[0]));
		return res;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 1) {
				if (Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps").getKeys(false).contains(args[0])) {
					if (args[0].equals("spawn"))
						MapMngr.spawnTeleport(p);
					else {
						World destw = MapMngr.newWorld(args[0]);
						p.teleport(destw.getSpawnLocation());
					}
					return true;
				}
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/warp <mapName>"));
	}

}