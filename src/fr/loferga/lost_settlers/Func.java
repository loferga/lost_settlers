package fr.loferga.lost_settlers;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Func {
	
	public static String format(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static ChatColor unformat(char c) {
		return ChatColor.getByChar(c);
	}
	
	// return the a list<String> only with the matching element with the *sample*
	public static String[] matches(String[] list, String sample) {
		int i = 0;                  // iteration pointer
		int j = 0;                  // numer of keeped elements
		int size = list.length;     // size
		String[] keeped = new String[size];
		while (i++<size)
			if (list[i].startsWith(sample))
				keeped[j++] = list[i];
		return Arrays.copyOfRange(keeped, 0, j);
	}
	
	public static String toReadable(String msg, int i) {
		String msgu = msg.toUpperCase();
		return msgu.substring(0, i).concat(msgu.substring(i).toLowerCase());
	}
	
	public static double random(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max);
	}
	
	public static <T> boolean primeContain(T[] array, T e) {
		int i = 0, length = array.length;
		boolean contain = false;
		while (i<length && !contain) {
			if (array[i] == e)
				contain = true;
			i++;
		}
		return contain;
	}
	
	public static <T> T pickRandom(T[] array) {
		return array[ThreadLocalRandom.current().nextInt(array.length)];
	}
	
	public static String[] toReadableTime(long time) {
		String[] rt = new String[] {"", "", ""};
		long t = time;
		long h = t/3600000; t%=3600000;
		long m = t/60000; t%=60000;
		long s = t/1000;
		if (h!=0) rt[0] = h + "h ";
		if (m!=0) rt[1] = m + "m ";
		if (s!=0) rt[2] = s + "s";
		return rt;
	}
	
	public static double gauss(int n) {
		double rng = Math.random();
		for (int i = 0; i<n; i++)
			rng += Math.random();
		return rng/n;
	}
	
	public static double onBounds(double a, double b, double v) {
		if (v<a) return a;
		if (b<v) return b;
		return v;
	}
	
	public static Location getPosLoc(World w, double[] pos) {
		return new Location(w, pos[0], pos[1], pos[2]);
	}
	
	public static void sendActionbar(Player p, String msg) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
	}
	
	public static void glowFor(LivingEntity ent, Set<Player> p, int duration) {
		Main.glow.put(ent.getEntityId(), p);
		ent.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, false, false));
	}
	
}
