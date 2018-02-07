package com.billshirey.util;

/**
 * A helper class to assist with String functions.
 * 
 * @author shirey
 *
 */
public class StringHelper
{
	/**
	 * Checks the value to see if it is empty.  A string is considered
	 * empty if it is null of consists of no characters or only white space characters.
	 *  
	 * @param value The value to check
	 * @return true if the string is null, contains no characters (length zero) or contains only
	 *         white space characters, otherwise false. 
	 */
	public static boolean isEmpty(String value)
	{
		if(value == null) return(true);
		return(value.trim().equals(""));
	}
	
	public static String unEscapeQuotes(String val)
	{
		if(val == null)
			return(null);
		String rVal = val.replace("\\\"", "\"");
		return(rVal);
	}
	
	public static boolean equalsIgnoreCase(String val1, String val2)
	{
		if(val1 == null && val2 == null) return(true);
		if(val1 == null || val2 == null) return(false);
		return(val1.toUpperCase().trim().equals(val2.toUpperCase().trim()));
	}
}
