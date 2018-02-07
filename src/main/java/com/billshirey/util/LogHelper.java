package com.billshirey.util;

import com.billshirey.jettyveneer.JettyLogger;

public class LogHelper
{
	private final static JettyLogger log = JettyLogger.getLogger(LogHelper.class);
	
	//logs an error and kills the process
	public static void logAndKill(String message, int shutdownLevel, Throwable t)
	{
		System.err.println(message);
		if(t != null)
		{
			t.printStackTrace(System.err);
			log.fatal(message, t);
		}
		else
			log.fatal(message);
		System.exit(shutdownLevel);
	}

}
