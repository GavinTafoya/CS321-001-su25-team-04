package cs321.search;

import cs321.btree.BTree;
import cs321.btree.TreeObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Searches a  B-Tree for keys listed in a query file and prints to stdout
 *
 */
public class SSHSearchBTree {
	public static void main(String[] args) {
		SSHSearchBTreeArguments a;
		try {
			a = SSHSearchBTreeArguments.parse(args);
		} catch (IllegalArgumentException e) {
			System.err.println("Error: " + e.getMessage());
			printUsage();
			return;
		}

		if (a.getDebug() == 1) {
			System.err.println(a.toString());
		}

		 final BTree tree;
		try {
			if (a.isUseCache() && a.getCacheSize() > 0) {
				tree = new BTree(a.getDegree(), a.getBtreeFile(), true, a.getCacheSize());
			} else {
				tree = new BTree(a.getDegree(), a.getBtreeFile());
			}
		} catch (Exception e) {
			System.err.println("Error opening BTree file: " + e.getMessage());
			return;
		}

		 final List<String> keys = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(a.getQueryFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String k = line.trim();
				if (!k.isEmpty()) keys.add(k);
			}
		} catch (IOException e) {
			System.err.println("Error reading query file: " + e.getMessage());
			return;
		}

		final Map<String, Long> freq = new HashMap<>(Math.max(16, keys.size()));
		for (String k : keys) {
			long c = 0;
			try {
				String nk = normalizeKey(k);
				TreeObject to = tree.search(nk);
				if (to != null) c = to.getCount();
			} catch (IOException e) {
				System.err.println("Error searching key '" + k + "': " + e.getMessage());
			}
			freq.put(k, c);
		}

		if (a.getTopFrequencyCount() > 0) {
			int topN = a.getTopFrequencyCount();
			List<Map.Entry<String, Long>> entries = new ArrayList<>(freq.entrySet());
			entries.removeIf(e -> e.getValue() <= 0);

			entries.sort((e1, e2) -> {
				int cmp = Long.compare(e2.getValue(), e1.getValue());
				return (cmp != 0) ? cmp : e1.getKey().compareTo(e2.getKey());
			});

			int printed = 0;
			for (Map.Entry<String, Long> e : entries) {
				if (printed >= topN) break;
				System.out.println(e.getKey() + " " + e.getValue());
				printed++;
			}
		} else {
			for (String k : keys) {
				System.out.println(k + " " + freq.get(k));
			}
		}

	}

	private static void printUsage() {
		System.err.println("Usage:");
		System.err.println("  java -jar SSHSearchBTree.jar \\");
		System.err.println("    --btree-file=<path> --query-file=<path> --degree=<int> \\");
		System.err.println("    [--cache=0|1] [--cache-size=<int>] [--debug=0|1] [--top-frequency=<int>]");
	}


	private static  String normalizeKey(String s) {
		if (s.length() > 32) {
			return s.substring(0, 32);
		}
		return s;
	}

}
