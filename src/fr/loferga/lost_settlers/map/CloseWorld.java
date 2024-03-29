package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class CloseWorld implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();
		if (args.length == 1)
			for (World w : MapMngr.getWorlds()) {
				String wname = w.getName().substring(3);
				if (wname.startsWith(args[0]))
					res.add(wname);
			}
		if (args.length == 2)
			res.addAll(Func.matches(List.of("save", "restore"), args[1]));
		return res;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if (args.length == 2
		&& args[1].equals("save") || args[1].equals("restore"))
				for (World w : MapMngr.getWorlds())
					if (w.getName().endsWith(args[0])) {
						for (Player wp : w.getPlayers())
							MapMngr.spawnTeleport(wp);
						if (args[1].equals("save")) {
							MapMngr.forget(w, true);
							p.sendMessage(Func.format(Main.MSG_DONE + "La carte est sauvegardee"));
						} else {
							MapMngr.forget(w, false);
							p.sendMessage(Func.format(Main.MSG_DONE + "La carte est restauree"));
						}
						return true;
					}
		sendInvalid(p);
		return false;
	}
	
	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format(Main.MSG_WARNING + "Invalid usage, please use:\n/close <mapName> <mode>"));
	}
	
}