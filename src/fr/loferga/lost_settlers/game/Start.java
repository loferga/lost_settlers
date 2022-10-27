package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class Start implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();
		Set<String> mapNames = Main.PLG.getConfig().getConfigurationSection("maps").getKeys(false);
		mapNames.remove("lobby");
		if (args.length == 1)
			res.addAll(Func.matches(mapNames, args[0]));
		return res;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
		Player snd = (Player) sender;
		if (args.length == 1) {
			List<String> maps = new ArrayList<>();
			for (String wn : Main.PLG.getConfig().getConfigurationSection("maps").getKeys(false))
				if (!wn.equals("lobby"))
					maps.add(wn);
			if (maps.contains(args[0])) {
				GameMngr.start(args[0]);
				return true;
			}
		}
		sendInvalid(snd);
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format(Main.MSG_WARNING + "Invalid usage, please use:\n/start"));
	}
	
}
