package com.billshirey.jettyveneer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.billshirey.util.PropertiesFile;
import com.billshirey.util.StringHelper;

public class JettyProperties
{
	private static JettyLogger log = JettyLogger.getLogger(JettyProperties.class);
	
	//The default port to listen on
	//this can be overridden by a "server.port" entry in a configuration file
	private static final Integer DEFAULT_PORT = 8888;
	private static Integer portNumber = null;
	private static String logDir = null;
	private static String logLevel = null;
	private static List<Class<?>> jerseyClasses = null;
	
	private static PropertiesFile props = null;
	private static boolean initialized = false;
	private static JettyProperties instance = null;
	
	/**
	 * Initialize by providing a property file or null, to provide
	 * default values.
	 * 
	 * @param propFile
	 */
	public synchronized static void initialize(PropertiesFile propFile)
	{
		props = propFile;
		initialized = true;
	}
	
	public synchronized static JettyProperties instance()
	{
		if(! initialized)
			throw new Error("JettyProperties was never initialized, initialize before using.");
		
		if(instance == null) instance = new JettyProperties();
		
		return(instance);
	}
	private JettyProperties(){}
	
	public Integer getPortNumber()
	{
		if(portNumber == null)
		{
			if(props != null)
			{
				String strPort = props.getProperty("server.port");
				if(strPort == null) portNumber = DEFAULT_PORT;
				else
				{
					try{portNumber = Integer.parseInt(strPort);}
					catch(NumberFormatException e)
					{
						log.warn("Configured port number " + strPort + " is not a valid integer.  The default port " + DEFAULT_PORT + " will be used.");
						portNumber = DEFAULT_PORT;
					}
				}	
			}
			else
				portNumber = DEFAULT_PORT;
		}
		return(portNumber);
	}
	
	public String getLogDir()
	{
		if(logDir == null)
		{
			if(props != null)
			{
				logDir = props.getProperty("log.dir");
			}
			if(logDir == null)
				logDir = "." + File.separator + "logs";			
		}
		return(logDir);
	}

	public String getLoggingLevel()
	{
		if(logLevel == null)
		{
			if(props != null)
			{
				logLevel = props.getProperty("log.level");
			}
			if(logLevel == null)  logLevel = "DEBUG";
		}
		return(logLevel);
	}

	public List<Class<?>> getJerseyClasses()
	{
		if(jerseyClasses == null)
		{
			String clss = props.getProperty("jersey.classes");
			jerseyClasses = new ArrayList<Class<?>>();
			if(! StringHelper.isEmpty(clss))
			{
				StringTokenizer tknizer = new StringTokenizer(clss, " ,");
				while(tknizer.hasMoreTokens())
				{
					String clsStr = tknizer.nextToken();
					try
					{
						Class<?> cls = Class.forName(clsStr);
						jerseyClasses.add(cls);
					}
					catch (ClassNotFoundException e)
					{
						log.error("Class " + clsStr + " was not found and couldn't be used as a Jersey endpoint.", e);
					}
					
				}
			}
				
		}
		return(jerseyClasses);
	}
	
	public String getProperty(String name)
	{
		if(props == null) return(null);
		return(props.getProperty(name));
	}

	public String getProperty(String name, String defaultVal)
	{
		if(props == null) return(defaultVal);
		return(props.getProperty(name, defaultVal));
	}
}
