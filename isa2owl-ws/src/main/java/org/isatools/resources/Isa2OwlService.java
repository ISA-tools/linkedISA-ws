package org.isatools.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.isatools.beans.Bean.ResponseTypes;
import org.isatools.beans.InitBean;
import org.isatools.beans.MessagesBean;
import org.isatools.handlers.ConvertHandler;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.file.FileDataBodyPart;

@Path("/")
public class Isa2OwlService {

	private static final String CONVERT_PATH = "convert/";
	private static HashMap<String, ConvertHandler> convertHandlers;

	static {
		// ISAConfigurationSet
		// .setConfigPath("C:/bin/apache-tomcat-6.0.37/bin/config/default-config");
		convertHandlers = new HashMap<String, ConvertHandler>();
	}

	private static Logger log = Logger
			.getLogger(Isa2OwlService.class.getName());

	@Context
	private UriInfo context;

	// Creates a new instance of MICheckout

	public Isa2OwlService() {
	}

	/**
	 * Retrieves representation of an instance of MICheckout
	 * 
	 * @return an instance of java.lang.String
	 */
	@GET
	@Path("testit")
	@Produces("text/plain")
	public String getTestChecklist(@QueryParam("name") String name) {
		return "This is a test output " + name
				+ "! :D The service is working...";
	}

	/**
	 * Retrieves the progress session cookie
	 * 
	 * @return an instance of java.lang.String
	 */
	@GET
	@Path("convert/progress")
	@Produces("text/plain")
	public String getProgress(@QueryParam("serviceID") String serviceID) {
		if (serviceID == null || serviceID.length() == 0) {
			log.info(serviceID + " received progress ERROR Invalid Request");
			return new MessagesBean(ResponseTypes.ERROR, 1, "Invalid Request")
					.toJSONString();
		}

		ConvertHandler handler = convertHandlers.get(serviceID);
		if (!isValidParameters(handler)) {
			log.info(serviceID + " received progress ERROR Invalid Request");
			return new MessagesBean(ResponseTypes.ERROR, 1, "Invalid Request")
					.toJSONString();
		}

		String progressStr = handler.bean().toJSONString();
		log.info(serviceID + " received progress " + progressStr);
		// All done destroy handler and remove it from map
		if (handler.bean().getCurrentStage() == "error"
				|| handler.bean().getCurrentStage() == "complete"
				|| handler.bean().getCurrentStage() == "cancelled") {
			convertHandlers.remove(serviceID);
		}
		return progressStr;
	}

	/**
	 * Retrieves the progress session cookie
	 * 
	 * @return an instance of java.lang.String
	 */
	@POST
	@Path("convert/init")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/plain")
	public String postInit(@FormDataParam("filename")String filename,@FormDataParam("filesize")long filesize) {
		String serviceID = Long.toString(System.currentTimeMillis());
		ConvertHandler handler = new ConvertHandler(serviceID,filename,filesize);
		convertHandlers.put(serviceID, handler);
		log.info(serviceID + " convertion initialised");
		return new InitBean(serviceID, handler.bean()).toJSONString();
	}
	
	/**
	 * Retrieves the progress session cookie
	 * 
	 * @return an instance of java.lang.String
	 */
	@GET
	@Path("convert/cancel")
	@Produces("text/plain")
	public String getCancel(@QueryParam("serviceID") String serviceID) {
		ConvertHandler handler = convertHandlers.get(serviceID);

		if (!isValidParameters(serviceID) || serviceID.length() == 0)
			return new MessagesBean(ResponseTypes.ERROR, 1, "Invalid Request")
					.toJSONString();

		String messages = "File convert cancelled";
		String path = null;
		if (handler != null)
			try {
				path = handler.getDeletePath();
				handler.cancel();
				handler.destroy();
			} catch (IOException e) {
				messages = "Cancelling Failed, file could not be cancelled";
			}
		ConvertHandler.cleanUp(path);
		convertHandlers.remove(serviceID);
		log.info(serviceID + " cancelled convert");
		return new MessagesBean(ResponseTypes.INFO, 1, messages).toJSONString();
	}

	/**
	 * Retrieves the ISATAB Nano Zip file, stores it locally, perform the
	 * validation and return the response.
	 * 
	 * @return an instance of java.lang.String
	 */
	@POST
	@Path("convert")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.MULTIPART_FORM_DATA)
	public Response convertFile(
			@FormDataParam("serviceID") final String serviceID,
			@FormDataParam("file") InputStream convertedFile,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("filesize") long filesize) {

		final ConvertHandler handler = convertHandlers.get(serviceID);
		// Check POST
		if (!isValidParameters(serviceID, convertedFile, fileDetail, handler)
				|| serviceID.length() == 0 || filesize <= 0) {
			FormDataMultiPart formData = new FormDataMultiPart();
			formData.field("response", new MessagesBean(ResponseTypes.ERROR, 1,
					"Invalid Request").toJSONString());
			ResponseBuilder response = Response.ok((Object) formData);
			return response.build();
		}

		handler.init(convertedFile, CONVERT_PATH);
		convertHandlers.put(serviceID, handler);
		log.info(serviceID + " started file convert");
		handler.startConverting();
		String jsonString = handler.bean().toJSONString();

		FormDataMultiPart formData = new FormDataMultiPart();
		if (handler.getOwlFile() != null)
			formData.bodyPart(new FileDataBodyPart("owlfile", handler
					.getOwlFile()));
		formData.field("response", jsonString);
		ResponseBuilder response = Response.ok((Object) formData);

		// Delete file after 10 mins
		destroyHandler(handler);

		return response.build();
	}

	private void destroyHandler(final ConvertHandler handler) {
		new Thread() {
			public void run() {
				try {
					sleep(60000);
					String path = handler.getDeletePath();
					handler.destroy();
					ConvertHandler.cleanUp(path);
					convertHandlers.remove(handler.getServiceID());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private boolean isValidParameters(Object... objects) {
		for (Object object : objects) {
			if (object == null)
				return false;
		}
		return true;
	}
}