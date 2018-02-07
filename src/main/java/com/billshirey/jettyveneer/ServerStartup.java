package com.billshirey.jettyveneer;

public class ServerStartup extends ServerControl
{

	/**
	 * Main entry for the bios-ws.  This method instantiates the Jetty 
	 * server.
	 * 
	 * @param args An optional single argument specifying the listening port.
	 * @throws Exception If an error occurs during startup.
	 */
	public static void main(String[] args)
	{
		
		handleCommandLine(args);
		
		JettyServer server = new JettyServer();
		server.startup(JettyProperties.instance().getJerseyClasses());
		
	}	
}
