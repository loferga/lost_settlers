package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

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
		if (args.length == 1)
			for (String wn : Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps").getKeys(false))
				res.add(wn);
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