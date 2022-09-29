package fr.loferga.lost_settlers.teams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.game.GameMngr;

public class SelectTeam implements TabExecutor {
	
	public static final List<String> TEAMS_NAMES = teamsNames();
	private static List<String> teamsNames() {
		List<String> res = new ArrayList<>(Arrays.asList("#null"));
		for (LSTeam team : TeamMngr.get())
			res.add(team.getRawName());
		return res;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if (GameMngr.gameIn(p) == null) {
			if (args.length == 1)
				return Func.matches(TEAMS_NAMES, args[0]);
			if (args.length == 2 && p.isOp())
				return null;
		}
		return new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 2) {
				if (p.isOp())
					p = Bukkit.getPlayer(args[1]);
				else {
					p.sendMessage(Func.format("\"" + args[1] + "\"&c is not a valid player"));
					return false;
				}
			}
			if (args.length == 1 || args.length == 2) {
				if (GameMngr.gameIn(p) != null) return false;
				if (args[0].equals("#null")) {
					TeamMngr.remove(p);
					p.sendMessage(Func.format("&eVous ne faites plus parti d'une équipe"));
					return true;
				}
				LSTeam team = null;
				for (LSTeam t : TeamMngr.get()) {
					if (t.getRawName().equals(args[0])) {
						team = t;
						break;
					}
				}
				if (team != null) {
					TeamMngr.join(p, team);
					p.sendMessage(Func.format("&eVous faites désormais parti de l'équipe " + team.getName()));
					return true;
				} else {
					p.sendMessage(Func.format("\"" + args[0] + "\"&c is not a valid team"));
					return false;
				}
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/lsteam <team>"));
		if (p.isOp())
			p.sendMessage(Func.format("&cor /lsteam <team> <player>"));
	}

}
