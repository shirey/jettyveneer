package com.billshirey.jettyveneer;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import com.billshirey.util.ApplicationException;
import com.billshirey.util.FileHelper;
import com.billshirey.util.StringHelper;

public class JettyLogger {

	private static boolean isConfigured = false;
	private static Hashtable<Class<?>, JettyLogger> loggers = new Hashtable<Class<?>, JettyLogger>();
	
	private Logger log = null;
	private Class<?> classToLogFor;
	
	private JettyLogger(Class<?> cls)
	{
		classToLogFor = cls;
		if(isConfigured)
			log =  Logger.getLogger(JettyServer.class);	
	}
	
	public synchronized static JettyLogger getLogger(Class<?> cls)
	{
		if(!loggers.containsKey(cls))
		{
			JettyLogger logr = new JettyLogger(cls);
			loggers.put(cls, logr);
		}
		return(loggers.get(cls));
	}
	
	
	/**
	 * A method to mark the logger as configured.
	 * Used in the case when a logging directory is not
	 * provided in the properties file, in which case the standard
	 * log4j configuration hierarchy will be followed.
	 */
	public synchronized static void setConfigured()
	{
		pvtSetConfigured();
	}
	
	//private method to set configured flag to true
	//this is needed because two public synchronized methods
	//need to perform this same function
	private static void pvtSetConfigured()
	{
		//if there were loggers created before the configuration
		//add backing log4j loggers for them now.
		for(Class<?> cls : loggers.keySet())
		{
			JettyLogger logr = loggers.get(cls);
			logr.log = Logger.getLogger(cls);
		}
		
		isConfigured = true;		
	}
	
	/**
	 * Configure the log4j root logger to append to console and a rotating log named jetty.log.  Use
	 * the given, optional log directory and logging level.  If a direcotry is not provided the
	 * current working directory is used "./logs/jetty.log".  If no logging level is provided or
	 * an invalid level is supplied DEBUG will be used.
	 * 
	 * CAUTION!: This method only works if the log4j root logger hasn't been created yet- i.e. by any
	 * calls to create a log4j Logger.
	 * 
	 * @param logDir the path to a writable directory where the log file will be placed. If null,
	 *               ./logs/ will be used.
	 *               
	 * @param level The log message level as a string.  Valid values are ERROR, WARN, INFO, DEBUG
	 */
	public synchronized static void configure(String logDir, String level) throws ApplicationException
	{
		String dir;
		Level logLevel = null;
		
		if(StringHelper.isEmpty(logDir)) 
			dir = "." + File.separator + "logs";
		else
			dir = FileHelper.ensureTrailingFileSeparator(logDir);
		
		File f = new File(dir);
		if(f.exists() && ! f.canWrite())
			throw new ApplicationException("The logging direcotry " + FileHelper.getBestAbsolutePath(f) + " is not writable.");
		
		if(!f.exists())
		{
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-rw-rw-");
			FileAttribute<Set<PosixFilePermission>> attr =  PosixFilePermissions.asFileAttribute(perms);
			Path p = f.toPath();
			try{Files.createDirectory(p, attr);}
			catch(IOException e){throw new ApplicationException("Unable to create the log directory at " + FileHelper.getBestAbsolutePath(f), e);}
		}
		
		if(StringHelper.isEmpty(level))
			logLevel = Level.DEBUG;
		else
		{
			logLevel = Level.toLevel(level.trim().toUpperCase(), null);
			if(logLevel == null) logLevel = Level.DEBUG;
		}

		ConfigurationBuilder< BuiltConfiguration > builder =
		        ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
		        ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(builder.newLayout("PatternLayout").
		        addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		builder.add( appenderBuilder );
		
		builder.setStatusLevel( org.apache.logging.log4j.Level.ALL);
		builder.setConfigurationName("RollingBuilder");
		LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
		        .addAttribute("pattern", "%d [%t] %-5level: %msg%n");
		ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
		        .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
		        .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));
		appenderBuilder = builder.newAppender("rolling", "RollingFile")
		        .addAttribute("fileName", dir + "rolling.log")
		        .addAttribute("filePattern", dir + "archive/rolling-%d{MM-dd-yy}.log.gz")
		        .add(layoutBuilder)
		        .addComponent(triggeringPolicy);
		builder.add(appenderBuilder);
		
		Object obj = appenderBuilder.build();
		System.out.println(obj.getClass());
		
		builder.add( builder.newRootLogger( logLevel )
		        .add( builder.newAppenderRef( "rolling" ) ).add(builder.newAppenderRef( "Stdout" )) );
		Configurator.initialize(builder.build());
		
		pvtSetConfigured();
	}
	
	private void toConsole(String lvl, String description, Throwable cause)
	{
		toConsole(lvl, description, cause, null);
	}
	private void toConsole(String lvl, String description, Throwable cause, PrintStream out)
	{
		if(out == null)
			out = System.out;
		
		if(description != null)
			out.println(lvl + ": " + description);
		
		if(cause != null)
			cause.printStackTrace(out);
	}
	
	public void error(String description, Throwable cause)
	{
		if(log == null) toConsole("ERROR", description, cause, System.err);
		else log.error(description, cause);
	}
	public void error(String description)
	{
		if(log == null) toConsole("ERROR", description, null, System.err);
		else log.error(description);
	}
	public void error(Throwable cause)
	{
		if(log == null) toConsole("ERROR", null, cause, System.err);
		else log.error(cause);		
	}
	
	public void debug(String description, Throwable cause)
	{
		if(log == null) toConsole("DEBUG", description, cause);
		else log.debug(description, cause);
	}
	public void debug(String description)
	{
		if(log == null) toConsole("DEBUG", description, null);
		else log.debug(description);
	}
	public void debug(Throwable cause)
	{
		if(log == null) toConsole("DEBUG", null, cause, System.err);
		else log.debug(cause);		
	}	
	public void info(String description, Throwable cause)
	{
		if(log == null) toConsole("INFO", description, cause);
		else log.info(description, cause);
	}
	public void info(String description)
	{
		if(log == null) toConsole("INFO", description, null);
		else log.info(description);
	}
	public void info(Throwable cause)
	{
		if(log == null) toConsole("INFO", null, cause, System.err);
		else log.info(cause);		
	}
	
	public void warn(String description, Throwable cause)
	{
		if(log == null) toConsole("WARN", description, cause);
		else log.warn(description, cause);
	}
	public void warn(String description)
	{
		if(log == null) toConsole("WARN", description, null);
		else log.warn(description);
	}
	public void warn(Throwable cause)
	{
		if(log == null) toConsole("WARN", null, cause, System.err);
		else log.warn(cause);		
	}
	
	public void fatal(String description, Throwable cause)
	{
		if(log == null) toConsole("FATAL", description, cause, System.err);
		else log.fatal(description, cause);
	}
	public void fatal(String description)
	{
		if(log == null) toConsole("FATAL", description, null, System.err);
		else log.fatal(description);
	}
	public void fatal(Throwable cause)
	{
		if(log == null) toConsole("FATAL", null, cause, System.err);
		else log.fatal(cause);		
	}	
}
