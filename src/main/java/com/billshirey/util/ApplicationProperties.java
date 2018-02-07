package com.billshirey.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.billshirey.jettyveneer.JettyLogger;

/**
 * A singleton class to manage reading properties from an application specific property file.
 * By default the property file will be named bios-ws.properties and located in a 
 * "conf" directory at the location where the application was run from (CWD).  The
 * directory can be overridden by calling setConfDir before the initial instantiation
 * of the class. 
 * 
 * @author shirey
 *
 */
public class ApplicationProperties
{
	private final static JettyLogger log = JettyLogger.getLogger(ApplicationProperties.class);
	
	public static final String APPLICATION_NAME = "bios-ws";
	private static final String DEFAULT_CONF_DIR = "conf";
	
	private static ApplicationProperties appProps = null;
	private static boolean erroredSetup = false;
	
	private static File confDir = null;
	
	private PropertiesFile props = null;
			
	private ApplicationProperties(boolean createPropFile) throws FileNotFoundException, IOException, ApplicationException
	{
		if(createPropFile)
		{
			if(confDir == null)
				setConfDir(DEFAULT_CONF_DIR);
			String propFileName = APPLICATION_NAME.trim() + ".properties";
			File propFile = new File(FileHelper.ensureTrailingFileSeparator(confDir.getAbsolutePath()) + propFileName);
			if(! propFile.exists())
				throw new FileNotFoundException("Properties file " + propFile.getAbsolutePath() + " does not exist.");
			if(! propFile.isFile())
				throw new ApplicationException("Properties file " + propFile.getAbsolutePath() + " is not a valid file.");
			if(! propFile.canRead())
				throw new IOException("Propertes file " + propFile.getAbsolutePath() + " cannot be read.");
			
			props = new PropertiesFile(new FileInputStream(propFile));
		}
	}
	
	public File getConfDir()
	{
		return(confDir);
	}
	
	public static void setConfDir(String dir) throws FileNotFoundException, IOException, ApplicationException
	{
		if(confDir != null)
			throw new ApplicationException("The application property configuration directory is already set to " +  confDir.getAbsolutePath());
		
		File confD = new File(dir);
		if(! confD.exists())
			throw new FileNotFoundException("Configuration directory " + confD.getAbsolutePath() + " was not found.");
		if(! confD.isDirectory())
			throw new ApplicationException("Configuration directory " + confD.getAbsolutePath() + " is not a directory.");
		if(! confD.canRead())
			throw new IOException("Configuration directory " + confD.getAbsolutePath() + " is not readable.");
		
		confDir = confD;
	}
	
	public static ApplicationProperties instance()
	{
		try
		{
			if(erroredSetup)
				return(appProps);

			if(appProps == null)
				appProps = new ApplicationProperties(true);

			return(appProps);			
		}
		catch(Throwable t)
		{
			erroredSetup = true;
			log.error("Unable to create an ApplicationProperties instance.", t);
			try{appProps = new ApplicationProperties(false);}
			catch(Throwable tr){log.error(tr);}
			return(appProps);
		}
	}
	
	public String getProperty(String key)
	{
		if(props != null)
			return(props.getProperty(key));
		else
			return(null);
	}
	public String getProperty(String key, String defaultValue)
	{
		if(props != null)
			return(props.getProperty(key, defaultValue));
		else
			return(defaultValue);
	}
}
