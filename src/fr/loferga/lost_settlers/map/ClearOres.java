package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class ClearOres implements TabExecutor {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if (args.length == 0) {
			World pw = p.getWorld();
			MapSettings ms = MapMngr.getMapSettings(pw);
			MapMngr.mapBlocks(pw, ms, -63.0, ms.isLodesActive() ? (double) ms.highestGround : null, e -> {
				Block b = new Location(pw, e[0], e[1], e[2]).getBlock();
				String matName = b.getType().toString();
				if (matName.endsWith("_ORE") || matName.startsWith("RAW_"))
					if (matName.startsWith("DEEPSLATE_"))
						b.setType(Material.DEEPSLATE);
					else b.setType(Material.STONE);
			});
			p.sendMessage(Func.format(Main.MSG_DONE + "Ores Cleared!"));
			return true;
		}
		sendInvalid(p);
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format(Main.MSG_WARNING + "Invalid Usage, please use: /clearores"));
	}
	
}