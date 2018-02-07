package com.billshirey.util;

/**
 * A simple Exception type to use internally to this application.
 * 
 * @author shirey
 *
 */
public class ApplicationException extends Exception
{
	private static final long serialVersionUID = 2929739284234234l;
	
	public ApplicationException(){super();}
	public ApplicationException(String msg){super(msg);}
	public ApplicationException(Throwable t){super(t);}
	public ApplicationException(String msg, Throwable t){super(msg, t);}
}
