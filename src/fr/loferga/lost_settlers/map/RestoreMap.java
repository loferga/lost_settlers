package fr.loferga.lost_settlers.map;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class RestoreMap implements TabExecutor {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player snd = (Player) sender;
			if (args.length == 0) {
				if (Main.map != null) {
					restore();
					return true;
				} else {
					snd.sendMessage(Func.format("&cThere is no map to reload"));
					return false;
				}
			}
			sendInvalid(snd);
		}
		return false;
	}

	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/reloadmap"));
	}
	
	private static Server server = Main.getPlugin(Main.class).getServer();
	
	public static void restore() {
		// tp before
		for (Player p : Bukkit.getOnlinePlayers()) {
			MapMngr.spawnTeleport(p);
		}
		
		String mapName = Main.map.getName();
		File mapF = new File(server.getWorldContainer().getPath() + File.separator + mapName);
		File bckF = new File(mapF.getPath() + File.separator + "backup");
		Bukkit.getWorlds().remove(Main.map);
		try {
			for (File bckFile : bckF.listFiles()) {
				if (bckFile.isDirectory())
					for (File subFile : bckFile.listFiles())
						Files.copy(
								subFile.toPath(),
								new File(mapF.getPath() + File.separator + bckFile.getName() + File.separator + subFile.getName()).toPath(),
								StandardCopyOption.REPLACE_EXISTING
							);
				else
					Files.copy(
							bckFile.toPath(),
							new File(mapF.getPath() + File.separator + bckFile.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING
						);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Bukkit.broadcastMessage(Func.format("&aLa carte est restauree"));
	}
	
}
