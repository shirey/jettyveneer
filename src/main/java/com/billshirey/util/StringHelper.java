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
}
