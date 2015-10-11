// Calculates the number of occureneces of a tag within the database and returns the highest result
// associated with the food
private static Map<File, String> calculate(Map<File, List<String>> result) {
	Map<File, String> master = new HashMap<File, String>();
	for (File f : result.keySet()) {
		List<String> tags = result.get(f);
		Map<String, Integer> frequency = new HashMap<String, Integer>();
		for (String tag : tags) {
			Set<String> results = getFood(tag); //todo
			for (String s : results) {
				if (!frequency.containsKey(s)) {
					frequency.put(s, 0);
				}
				frequency.put(s, frequency.get(s) + 1);
			}
		}
		String placeInMaster = determine(frequency);
		master.put(f, placeInMaster);
	}
	return master;
}

// Returns the string from map that occurs the most
private static String determine(Map<String, Integer> frequency) {
	String found = "";
	int max = 0;
	for (String s : frequency.keySet()) {
		if (frequency.get(s) > max) {
			found = s;
			max = frequency.get(s);
		}
	}
	return found;
}