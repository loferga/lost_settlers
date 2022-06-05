package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;

public class CloseWorld implements TabExecutor {
	
	private static Map<World, Set<Player>> discPlayers = new HashMap<>();
	
	public static void addWorld(World w) {
		discPlayers.put(w, new HashSet<>());
	}
	
	private static void removeWorld(World w) {
		discPlayers.remove(w);
	}
	
	public static void disconect(Player p) {
		World pw = p.getWorld();
		if (discPlayers.containsKey(pw))
			discPlayers.get(pw).add(p);
	}
	
	public static void reconnect(Player p) {
		World pw = p.getWorld();
		if (discPlayers.containsKey(pw) && discPlayers.get(pw).contains(p))
			discPlayers.get(pw).remove(p);
	}

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
							for (Player wp : w.getPlayers()) {
								System.out.println(wp.getName());
								MapMngr.spawnTeleport(wp);
							}
							for (Player discP : discPlayers.get(w)) {
								System.out.println(discP.getName());
								MapMngr.spawnTeleport(discP);
								discPlayers.get(w).remove(discP);
							}
							removeWorld(w);
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