package com.billshirey.jettyveneer.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.billshirey.jettyveneer.JettyServer;
import com.billshirey.util.StringHelper;

@Path("/veneer/")
public class VeneerEndpoint
{
	@GET
	@Path("shutdown")
	@Produces(MediaType.TEXT_PLAIN)
	public Response shutdown(@QueryParam("shutdownkey") String shutdownKey)
	{
		if(StringHelper.isEmpty(shutdownKey))
		{
			return(Response.status(400).type(MediaType.TEXT_PLAIN).entity("shutdownkey is a required parameter").build());
		}
		if(JettyServer.shutdown(shutdownKey))
			return(Response.status(200).type(MediaType.TEXT_PLAIN).entity("Shutting down.").build());
		else
			return(Response.status(500).type(MediaType.TEXT_PLAIN).entity("Invalid shudownKey").build());
	}
}
