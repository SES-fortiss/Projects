package org.fortiss.smg.webrest.impl.front;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fortiss.smg.webrest.impl.types.TestXMLObject;

/**
 * Used for unit testing and maybe for the client to check whether the api is
 * working or not.
 */
@Path("/health")
public class HealthOfServer {

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
			MediaType.TEXT_PLAIN })
	@Path("/online")
	public String user(@PathParam("devid") String user) {

		return "200";
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/onlin2")
	public TestXMLObject user2() {

		TestXMLObject test = new TestXMLObject();
		test.setName("aaa");
		test.setPin(12);

		return test;
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/onlin3")
	public TestXMLObject user3() {

		TestXMLObject test = new TestXMLObject();
		test.setName("aaa");
		test.setPin(12);

		return test;
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/onlin4")
	public String user4() {

		return "200";
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/testxml/{test}")
	public TestXMLObject testxml(@PathParam("test") String user) {

		TestXMLObject test = new TestXMLObject();
		test.setName(user);
		test.setPin(12);

		return test;
	}

}