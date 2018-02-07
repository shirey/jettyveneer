package com.billshirey.jettyveneer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.billshirey.jettyveneer.ws.VeneerEndpoint;
import com.billshirey.util.ApplicationException;
import com.billshirey.util.ApplicationProperties;

import com.billshirey.util.FileHelper;
import com.billshirey.util.LogHelper;

public class JettyServer
{

	private static JettyLogger log = JettyLogger.getLogger(JettyServer.class);
	
	private static Server server = null;
	
	public synchronized static boolean shutdown(String key)
	{
		try
		{
			String instKey = getShutdownKey();
			if(! instKey.equals(key))
				return(false);
		}
		catch(Throwable t)
		{
			String msg = "Unable to obtain current instance key, therefore unable to shut down instance.  Checking against key:" + key;
			System.err.println(msg);
			t.printStackTrace(System.err);
			log.error(msg, t);
			return(false);
		}
		
		try
		{
			if(server != null)
			{
				new Thread(new Stopper()).start();
			}
			else{LogHelper.logAndKill("Error during shutdown of service on port " + JettyProperties.instance().getPortNumber() + ".  A server instance was not found.", 9, null);}
		}
		catch(Throwable t)
		{
			LogHelper.logAndKill("Unhandled error during shutdown", 10, t);
		}
		return(true);
	}
	
	private static class Stopper implements Runnable
	{

		public void run() {
			try
			{
				Thread.sleep(5000);
				server.stop();
			}
			catch(Throwable t)
			{
				LogHelper.logAndKill("Error in stopping thread while shutting down server.", 11, t);
			}
		}
		
	}
		
	/**
	 * Start the Jetty server
	 * 
	 * @param serverPort The port to listen on
	 * @throws Exception when an error occurs during startup.
	 */
	public void startup(List<Class<?>> jerseyClasses)
	{
		if(server != null)
		{
			LogHelper.logAndKill("An instance of this service is running in the same JVM on port " + JettyProperties.instance().getPortNumber(), 7, null);
		}

		server = configure(jerseyClasses);
		
		File confDir = ApplicationProperties.instance().getConfDir();
		if(! confDir.exists() && confDir.canWrite()){LogHelper.logAndKill("Error: The configuration directory " + confDir.getAbsolutePath() + " must exist and be writable.", 1, null);}
		
        try{server.start();}
        catch(BindException be){LogHelper.logAndKill("Error starting the server.  Possibly another server is running on port " + JettyProperties.instance().getPortNumber(), 2, be);}
        catch(Throwable t){LogHelper.logAndKill("Error starting the server.  An unexpected error occurred.", 3, t);}
        
        try{createInstanceFile();}
        catch(Throwable t){LogHelper.logAndKill("Error while starting server. Unable to write instance file for port " + JettyProperties.instance().getPortNumber() + " in " + ApplicationProperties.instance().getConfDir().getAbsolutePath(), 8, t);}
        
        log.info("Server starting on port " + JettyProperties.instance().getPortNumber());
        
        try{server.join();}
        catch(InterruptedException ie)
        {
        	teardown();
        	LogHelper.logAndKill("Server interrupted.", 12, ie);
        }
        teardown();
        log.info("Server Exited.");
        System.out.println("server exited");
        System.exit(0);
	}	

	public Integer getPort()
	{
		return(JettyProperties.instance().getPortNumber());
	}
	
	private void teardown()
	{
		try
		{
			removeInstanceFile();
		}
		catch(Throwable t){
			String msg = "Unexpected error during server teardown.";
			System.err.println(msg);
			t.printStackTrace(System.err);
			log.fatal(msg, t);
		}
	}
	

	
	//configure the Jetty server to use the endpoint defined in BIOSDataEndpoint,
	//and to use Jackson for resolving json
	private Server configure(List<Class<?>> jerseyClasses)
	{
		ResourceConfig resourceConfig = new ResourceConfig();
		
		resourceConfig.getClasses().add(VeneerEndpoint.class);
		for(Class<?> cls : jerseyClasses)
			resourceConfig.getClasses().add(cls);
		
		resourceConfig.register(JacksonFeature.class);
		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);                
		Server server = new Server(JettyProperties.instance().getPortNumber());		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(sh, "/*");
		server.setHandler(context);
		return server;
	}
	

	
	public static String getShutdownKey() throws ApplicationException, IOException  
	{
		File instFile = getInstanceFile();
		BufferedReader reader = new BufferedReader(new FileReader(instFile));
		String instKey = reader.readLine();
		reader.close();
		return(instKey);
	}

	private synchronized void removeInstanceFile() throws ApplicationException
	{
		File instanceFile = getInstanceFile();
		if(instanceFile.exists())
			instanceFile.delete();
	}
	
	private static File getInstanceFile() throws ApplicationException
	{
		String filename = "jetty-instance-" + JettyProperties.instance().getPortNumber();
		String filepath = FileHelper.ensureTrailingFileSeparator(ApplicationProperties.instance().getConfDir().getAbsolutePath()) + filename;
		File instanceFile = new File(filepath);
		return(instanceFile);
	}
	
	private void createInstanceFile() throws IOException, ApplicationException
	{
		String instanceNum = UUID.randomUUID().toString();
		File instanceFile = getInstanceFile();
		if(instanceFile.exists())
			instanceFile.delete();
		FileWriter fw = new FileWriter(instanceFile);
		fw.write(instanceNum);
		fw.close();
	}		
	
}
