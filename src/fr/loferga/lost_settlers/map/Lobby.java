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

public class Lobby implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if (args.length == 0) {
			World w = p.getWorld();
			MapMngr.spawnTeleport(p);
			// TODO when do we want to forget on lobby?
			if (w.getPlayers().isEmpty()) MapMngr.forget(w, false);
			return true;
		}
		sendInvalid(p);
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format(Main.MSG_WARNING + "Invalid Usage, please use: /lobby"));
	}
	
}