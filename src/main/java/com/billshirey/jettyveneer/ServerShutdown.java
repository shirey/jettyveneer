package com.billshirey.jettyveneer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Server shutdown class.  The server key is retrieved from the file left by BiosDataStartup and used
 * to call the shutdown WS method in the running server.
 * 
 * @author shirey
 *
 */
public class ServerShutdown extends ServerControl
{
	/**
	 * Main to shutdown the server.  Retrieves the server key from the file left by BiosDataStartup and calls
	 * the ws shutdown method.  Optionally the server listener port can be passed in as a command line argument.
	 * If the port is not specified on the command line tt will retrieve the port from the configuration file or
	 * default port via BiosDataStartup.
	 * 
	 * @param args An optional single argument specifying the listening port.
	 * @throws Exception If an error occurs during the shutdown.
	 */	
	public static void main(String [] args) throws Exception
	{

		handleCommandLine(args);
		String shutdownKey = JettyServer.getShutdownKey();
		Client client = ClientBuilder.newClient();
		Response resp = client.target("http://localhost:" + JettyProperties.instance().getPortNumber() + "/shutdown?shutdownkey=" + shutdownKey.trim())
                .request(MediaType.TEXT_PLAIN).get();
		if(resp.getStatus() == 200)
			System.out.println("Shutdown Started");
		else
		{
			System.out.println("Error during shutdown.");
			System.out.println(resp.readEntity(String.class));
		}
	}
}
