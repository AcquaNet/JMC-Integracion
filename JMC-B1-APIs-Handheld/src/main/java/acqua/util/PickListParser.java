package acqua.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PickListParser {

	public static HashMap<String,Object> parseString(String str) {
		
		HashMap<String,Object> map = new HashMap<>();
		if (str == null) {
			return null;
		}
		String input = str;
		Pattern regex = Pattern.compile("(.+)\\-(.+)");
		Matcher match = regex.matcher(input);
		
		System.out.println(match.groupCount());
		if (match.groupCount() < 2) {
			return null;
		}
		if (match.find()) {
			map.put("id",  match.group(1));
			map.put("code", match.group(2));
			return map;
		}
		return null;
	}

}
