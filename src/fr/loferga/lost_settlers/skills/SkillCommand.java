package fr.loferga.lost_settlers.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.util.Func;

public class SkillCommand implements TabExecutor {
	
	private static final List<String> SKILLS_NAMES = skillsNames();
	private static List<String> skillsNames() {
		List<String> res = new ArrayList<>(Arrays.asList("#null"));
		for (Skill skill : SkillSelection.getSkills())
			res.add(Func.toReadable(skill.toString(), 1));
		return res;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if (GameMngr.gameIn(p) == null) {
			if (args.length == 1)
				return Func.matches(SKILLS_NAMES, args[0]);
			if (args.length == 2 && p.isOp())
				return null;
		}
		return new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
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
				SkillSelection.deselect(p);
				p.sendMessage(Func.format("&eVous ne possédez plus de talent"));
				return true;
			}
			boolean selection = SkillSelection.select(p, args[0].toUpperCase());
			if (selection)
				p.sendMessage(Func.format("&eVous possédez désormais le talent &3" + args[0]));
			return selection;
		}
		
		sendInvalid(p);
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/skill <skill>"));
		if (p.isOp())
			p.sendMessage(Func.format("&cor /skill <skill> <player>"));
	}

}
