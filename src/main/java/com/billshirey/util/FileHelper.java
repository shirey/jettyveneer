package com.billshirey.util;

import java.io.File;
import java.io.IOException;

/**
 * Helper class for file activites
 * 
 * @author shirey
 *
 */
public class FileHelper
{
	/**
	 * Make sure the supplied directory path ends with a system/OS specific separator
	 *  
	 * @param path  The directory path to check
	 * @return The directory path ending in a separator or null if a null path was provided.
	 */
	public static String ensureTrailingFileSeparator(String path)
	{
		if(path == null) return(null);
		
		String rVal = path.trim();
		if(! rVal.endsWith(File.separator))
			rVal = rVal + File.separator;
		
		return(rVal);
	}
	
	public static String getBestAbsolutePath(File f)
	{
		String path = null;
		try{ path = f.getCanonicalPath();}
		catch(IOException e){path = f.getAbsolutePath();}
		return(path);
	}
}
