package me.nahkd.spigot.btg.pub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TabCompleteUtils {

	public static List<String> search(String kwd, String... subcmds) {
		kwd = kwd.toLowerCase();
		if (kwd.length() <= 0) return Arrays.asList(subcmds);
		List<String> out = new ArrayList<String>();
		for (String str : subcmds) if (str.startsWith(kwd)) out.add(str);
		return out;
	}
	public static List<String> search(String kwd, List<String> subcmds) {
		kwd = kwd.toLowerCase();
		if (kwd.length() <= 0) return subcmds;
		List<String> out = new ArrayList<String>();
		for (String str : subcmds) if (str.startsWith(kwd)) out.add(str);
		return out;
	}
	public static List<String> search(String kwd, Set<String> keySet) {
		kwd = kwd.toLowerCase();
		if (kwd.length() <= 0) return Arrays.asList(keySet.toArray(new String[0]));
		List<String> out = new ArrayList<String>();
		for (String str : keySet) if (str.startsWith(kwd)) out.add(str);
		return out;
	}
	
}
