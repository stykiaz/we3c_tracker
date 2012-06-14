package utils;

import java.util.regex.Pattern;

public class Validation {

	static Pattern emailPattern = Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?");
	
	public static boolean isEmail(String value) {
		return emailPattern.matcher( value ).matches();
	}
	
}
