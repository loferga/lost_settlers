package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.util.Func;

public class Lobby implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0) {
				World w = p.getWorld();
				MapMngr.spawnTeleport(p);
				if (w.getPlayers().size() == 0) MapMngr.forget(w, false);
				return true;
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid Usage, please use: /lobby"));
	}
	
}