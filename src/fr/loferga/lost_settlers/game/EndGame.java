package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.tasks.Game;

public class EndGame implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player snd = (Player) sender;
			if (args.length == 0) {
				if (Game.active()) {
					EndGameMngr.stop(null);
					return true;
				} else {
					snd.sendMessage(Func.format("&cNo game is running"));
					return false;
				}
			}
			sendInvalid(snd);
		}
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/stop"));
	}

}
