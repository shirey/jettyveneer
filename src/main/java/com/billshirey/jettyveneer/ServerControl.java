package com.billshirey.jettyveneer;

import java.io.File;
import java.io.FileInputStream;

import com.billshirey.util.PropertiesFile;

/**
 * The startup class for the bios-ws application.  Run this class to
 * start the application web service.  This service uses Jetty as a
 * server.
 * 
 * An optional single command line argument can be passed in when running
 * containing the port that the service will listen on.  If a port number is
 * specified in the configuration file it is used, otherwise the port
 * is defaulted to 8888.  
 * 
 * @author shirey
 *
 */
public abstract class ServerControl {

	/**
	 * Look for a configuration file specified as the only argument on the command line.
	 * If no config file is specified initialize the JettyProperties without one.
	 * 
	 * After the JettyProperties file is configured the logger is initialized using any
	 * logging dir and logging level information in the properties file.
	 * 
	 * @param args
	 */
	protected static void handleCommandLine(String [] args)
	{
		try
		{			
			//if a config file is passed in on the command line, set things up for it
			if(args.length >= 1)
			{
				try
				{
					File configFile = new File(args[0]);
					if(! configFile.exists() || ! configFile.canRead())
					{
						System.err.println("Configuration file " + args[0] + " does not exist or is not readable.");
						System.exit(1);
					}
					JettyProperties.initialize(new PropertiesFile(new FileInputStream(configFile)));
				}
				catch(Throwable t)
				{
					t.printStackTrace(System.err);
					System.err.println("Unexpected error while trying to access configuration file " + args[0]);
					System.exit(2);
				}
			}
			else
				JettyProperties.initialize(null);
			
			//If a log dir is specified use it and any log level to initialize the logger
			//otherwise initialize the logger with default values
			if(JettyProperties.instance().getLogDir() != null)
			{
				JettyLogger.configure(JettyProperties.instance().getLogDir(), JettyProperties.instance().getLogDir());
			}
			else
				JettyLogger.setConfigured();
		}
		catch(Throwable t)
		{
			t.printStackTrace(System.err);
			System.err.println("Unexpected error while parsing the command line.");
			System.exit(3);
		}
	}	
}
