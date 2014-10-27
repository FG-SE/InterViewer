package utils;

import java.util.List;

public class StringHelper {
	/**
	 * Builds a human readable reply message
	 * 
	 * @param fault
	 *            - all human readable messages
	 * @param delimiter
	 *            - eg. " "
	 * @return String containing all messages
	 */
	public static String buildAggragationString(List<String> stringList,
			String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stringList.size(); i++) {
			sb.append(stringList.get(i));
			if(i != stringList.size() - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}
