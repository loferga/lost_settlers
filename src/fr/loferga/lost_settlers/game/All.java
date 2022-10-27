package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class All implements TabExecutor {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		Game game = GameMngr.gameIn(p);
		if (game == null) {
			p.sendMessage(Func.format(Main.MSG_ERROR + "No Game Running"));
			return false;
		}
		StringBuilder msg = new StringBuilder();
		for (String arg : args)
			msg.append(' ' + arg);
		for (Player player : game.getWorld().getPlayers())
			player.sendMessage(game.getTeam(p).getChatColor() + "[" + p.getName() + "]" + ChatColor.WHITE + msg.toString());
		return true;
	}
	
}