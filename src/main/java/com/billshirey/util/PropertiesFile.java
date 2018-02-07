package com.billshirey.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A sub-class for the standard Java Properties class that provides
 * a convenience constructor that takes and InputStream
 * 
 * @author shirey
 *
 */
public class PropertiesFile extends Properties
{
	private static final long serialVersionUID = 52342231614354l;

	public PropertiesFile(InputStream input) throws IOException
	{
		super();
		this.load(input);
	}
	
}
