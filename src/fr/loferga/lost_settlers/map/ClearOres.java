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

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.map.settings.MapSettings;

public class ClearOres implements TabExecutor {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0) {
				World pw = p.getWorld();
				MapSettings ms = MapMngr.getMapSettings(pw);
				Location center = MapMngr.getMapCenter(pw, ms);
				int area = ms.playableArea;
				for (int x = (int) (center.getX()-area); x < center.getX()+area; x++)
					for (int y = 2; y < ms.highestGround; y++)
						for (int z = (int) (center.getZ()-area); z < center.getZ()+area; z++) {
							Block b = new Location(p.getWorld(), x, y, z).getBlock();
							if (b.getType().toString().endsWith("ORE"))
								if (b.getType().toString().startsWith("DEEPSLATE"))
									b.setType(Material.DEEPSLATE);
								else
									b.setType(Material.STONE);
						}
				p.sendMessage(Func.format("&eOres Cleared!"));
				return true;
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid Usage, please use: /clearores"));
	}
	
}