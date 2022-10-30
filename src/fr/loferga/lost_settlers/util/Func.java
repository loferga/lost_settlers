package fr.loferga.lost_settlers.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.map.geometry.Vector;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Func {
	
	private Func() {/*fonction holder class, it should never be instantiated*/}
	
	public static String format(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
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
	
	private static Random rng = new Random();
	
	public static double random(double min, double max) {
		if (max<=min || Double.isNaN(min) || Double.isNaN(max)) return min;
		return rng.nextDouble(min, max);
	}
	
	public static int randomInt(int min, int max) {
		if (max<=min) return min;
		return rng.nextInt(min, max);
	}
	
	public static <T> boolean primeContain(T[] array, T e) {
		int i = 0;
		int length = array.length;
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
	
	public static <T> T pop(List<T> l) {
		T res = l.get(0);
		l.remove(0);
		return res;
	}
	
	private static final int X = 2;
	
	public static void dropExp(Location loc, int xpAmount) {
		int r = xpAmount;
		int x = X;
		while (r > 0) {
			int xp;
			if (r >= x) {
				xp = x;
				r -= x;
			} else {
				xp = r;
				r = 0;
			}
			ExperienceOrb orb = (ExperienceOrb) loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
			orb.setExperience(xp);
			x *= X;
		}
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
	
	// square shaped distance check
	public static boolean isNearBy(double pos, double around, double by) {
		return around - by <= pos && pos <= around + by;
	}
	
	public static void sendActionbar(Player p, String msg) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
	}
	
	public static double[] getMaxAbs(Vector... vectors) {
		if (vectors.length == 0) return new double[] {0, 0, 0};
		double[] maxs = new double[] {vectors[0].x, vectors[0].y, vectors[0].z};
		for (Vector v : vectors) {
			if (v.x > maxs[0])
				maxs[0] = v.x;
			if (v.y > maxs[1])
				maxs[1] = v.y;
			if (v.z > maxs[2])
				maxs[2] = v.z;
		}
		return maxs;
	}
	
}
