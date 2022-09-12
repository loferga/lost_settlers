package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;

public class CloseWorld implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();
		if (args.length == 1)
			for (World w : MapMngr.worlds)
				res.add(w.getName().substring(4));
		if (args.length == 2)
			res.addAll(Arrays.asList("save", "restore"));
		return res;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 2)
				if (args[1].equals("save") || args[1].equals("restore"))
					for (World w : MapMngr.worlds)
						if (w.getName().endsWith(args[0])) {
							for (Player wp : w.getPlayers())
								MapMngr.spawnTeleport(wp);
							if (args[1].equals("save")) {
								MapMngr.forget(w, true);
								p.sendMessage(Func.format("&aLa carte est sauvegardee"));
							} else {
								MapMngr.forget(w, false);
								p.sendMessage(Func.format("&aLa carte est restauree"));
							}
							return true;
						}
			sendInvalid(p);
		}
		return false;
	}
	
	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/close <mapName> <mode>"));
	}
	
}