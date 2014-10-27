package utils;

import java.util.regex.Pattern;

public class Inputcheck {

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	/***
	 * checks the correct Syntax of an Email-address
	 * @param email the address to be checked
	 * @return
	 */
	public static boolean isEmail(String email){
		return VALID_EMAIL_ADDRESS_REGEX.matcher(email).find();
	}
}
