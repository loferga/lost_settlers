package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class Start implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();
		if (args.length == 1)
			for (String wn : Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps").getKeys(false))
				if (!wn.equals("spawn"))
					res.add(wn);
		return res;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player snd = (Player) sender;
			if (args.length == 1) {
				List<String> maps = new ArrayList<>();
				for (String wn : Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps").getKeys(false))
					if (!wn.equals("spawn"))
						maps.add(wn);
				if (maps.contains(args[0])) {
					GameMngr.start(args[0]);
					return true;
				}
			}
			sendInvalid(snd);
		}
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/start"));
	}
	
}
