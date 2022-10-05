package fr.loferga.lost_settlers.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	public static List<String> matches(Iterable<String> list, String sample) {
		List<String> keeped = new ArrayList<>();
		Iterator<String> it = list.iterator();
		String lowSample = sample.toLowerCase();
		while (it.hasNext()) {
			String next = it.next();
			if (next.toLowerCase().startsWith(lowSample))
				keeped.add(next);
		}
		return keeped;
	}
	
	public static <T extends Enum<T>> T valueOf(Class<T> e, String str) {
		try {
			return Enum.valueOf(e, str);
		} catch (NullPointerException nullPException) {
			nullPException.printStackTrace();
		}
		return null;
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
		Glow.addGlow(ent.getEntityId(), p);
		ent.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, false, false));
	}
	
}