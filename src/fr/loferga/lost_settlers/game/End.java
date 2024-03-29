package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class End implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if (args.length == 0) {
			Game game = GameMngr.getGame(p.getWorld());
			if (game != null) {
				GameMngr.stop(game, null);
				return true;
			} else {
				p.sendMessage(Func.format(Main.MSG_ERROR + "No game is running"));
				return false;
			}
		}
		sendInvalid(p);
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format(Main.MSG_WARNING + "Invalid usage, please use:\n/stop"));
	}

}
