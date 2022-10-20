package fr.loferga.lost_settlers.util;

import java.util.HashMap;

public class BiMap<K extends Object, V extends Object> extends HashMap<K, V> {
	
	private static final long serialVersionUID = 6515718561226362163L;

	public K getKey(V v) {
		for (Entry<K, V> e : entrySet())
			if (e.getValue() == v)
				return e.getKey();
		return null;
	}
	
}