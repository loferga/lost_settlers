package fr.loferga.lost_settlers.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;

import com.google.common.io.Files;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class ReloadMap implements TabExecutor {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player snd = (Player) sender;
			if (args.length == 0) {
				reloadMap();
				return true;
			}
			sendInvalid(snd);
		}
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/reloadmap"));
	}
	
	private static Server server = Main.getPlugin(Main.class).getServer();
	
	public static void reloadMap() {
		// tp before
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(new Location(Bukkit.getWorld("waitingWorld"), 0.0, 5.0, 0.0));
		}
		Bukkit.broadcastMessage(Func.format("[LostSettlers]&c La carte est en train de se recharger, patientez un instant"));
		
		String mapName = Main.map.getName();
		File mapF = new File(server.getWorldContainer().getPath() + File.separator + mapName);
		File bckF = new File(server.getWorldContainer().getPath() + File.separator + "backup");
		Bukkit.getWorlds().remove(Main.map);
		Bukkit.unloadWorld(Main.map, false);
		try {
			FileUtils.deleteDirectory(mapF);
			// world should be deleted and no longer known by the serer
			Files.copy(bckF, mapF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// WorldCreator().createWorld() seems to create a world if it don't already exist, load it otherwise (as I experienced)
		server.getWorlds().add(0, new WorldCreator(mapName).createWorld()); // so the call should load the world as the folder exist
		Main.map = Bukkit.getWorlds().get(0);
		// tp after
		for (Player p : Bukkit.getOnlinePlayers()) {
			Location ploc = MapMngr.getMapCenter();
			MapMngr.teleportAround(ploc, p);
			p.sendMessage(p.getLocation().getWorld().getName());
		}
	}
	
}
