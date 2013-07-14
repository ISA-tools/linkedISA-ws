package org.isatools.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

public class ConvertServicesTest {

	private static String serviceID;
	private static String url = "http://localhost:8080/isa2owl-ws/";

	@Before
	public void setUp() throws ParserConfigurationException, IOException,
			SAXException {
		Client client = Client.create();
		WebResource service = client.resource(UriBuilder.fromUri(
				url + "convert/init").build());
		service.get(ClientResponse.class);
		ClientResponse resp = service.get(ClientResponse.class);
		String response = resp.getEntity(String.class);
		JsonElement json = new JsonParser().parse(response);
		JsonObject jobject = json.getAsJsonObject();
		JsonObject jobject2 = jobject.get("INFO").getAsJsonObject();
		serviceID = jobject2.get("serviceID").getAsString();
	}

	@After
	public void after() {
		Client client = Client.create();
		WebResource service = client.resource(UriBuilder.fromUri(
				url + "convert/cancel?serviceID=" + serviceID).build());
		service.get(ClientResponse.class);
	}

//	@Test
	public void testGetProgressError() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Client client = Client.create();
		WebResource service = client
				.resource(UriBuilder.fromUri(
						url + "convert/progress?serviceID=" + serviceID + "asdas")
						.build());
		ClientResponse resp = service.get(ClientResponse.class);
		assertEquals(200, resp.getStatus());
		String response = resp.getEntity(String.class);
		JsonElement json = new JsonParser().parse(response);
		JsonObject jobject = json.getAsJsonObject();
		JsonElement jobject2 = jobject.get("ERROR");
		assertTrue(jobject2 != null);
	}

//	@Test
	public void testGetProgress() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Client client = Client.create();

		WebResource service = client.resource(UriBuilder.fromUri(
				url + "convert/progress?serviceID=" + serviceID).build());
		ClientResponse resp = service.get(ClientResponse.class);
		assertEquals(200, resp.getStatus());
		String response = resp.getEntity(String.class);
		JsonElement json = new JsonParser().parse(response);
		JsonObject jobject = json.getAsJsonObject();
		JsonObject jobject2 = jobject.get("CONVERT").getAsJsonObject();
		assertTrue(jobject2 != null);
	}

//	@Test
	public void testPostUploadErrors() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException,
			IllegalAccessException, FileNotFoundException {
		Client client = Client.create();
		WebResource service = client.resource(UriBuilder.fromUri(
				url + "convert/").build());

		FormDataMultiPart formData = new FormDataMultiPart();
		File file = new File(getClass().getResource("/files/").getFile()+"ARMSTRONG-3.zip");
		formData.bodyPart(new FileDataBodyPart("file", file));
		formData.field("serviceID", "");
		formData.field("filesize", Long.toString(file.length()));
		ClientResponse resp = service.type(MediaType.MULTIPART_FORM_DATA).post(
				ClientResponse.class, formData);
		assertEquals(200, resp.getStatus());
		FormDataMultiPart respForm = resp.getEntity(FormDataMultiPart.class);
		String response = respForm.getField("response").getValue();
		JsonElement json = new JsonParser().parse(response);
		JsonObject jobject = json.getAsJsonObject();
		JsonElement jobject2 = jobject.get("ERROR");
		assertTrue(jobject2 != null);

		formData = new FormDataMultiPart();
		formData.field("serviceID", serviceID);
		formData.field("filesize", Long.toString(file.length()));
		resp = service.type(MediaType.MULTIPART_FORM_DATA).post(
				ClientResponse.class, formData);
		assertEquals(200, resp.getStatus());
		respForm = resp.getEntity(FormDataMultiPart.class);
		response = respForm.getField("response").getValue();
		json = new JsonParser().parse(response);
		jobject = json.getAsJsonObject();
		jobject2 = jobject.get("ERROR");
		assertTrue(jobject2 != null);

		formData = new FormDataMultiPart();
		formData.bodyPart(new FileDataBodyPart("file", file));
		formData.field("serviceID", serviceID);
		resp = service.type(MediaType.MULTIPART_FORM_DATA).post(
				ClientResponse.class, formData);
		assertEquals(200, resp.getStatus());
		respForm = resp.getEntity(FormDataMultiPart.class);
		response = respForm.getField("response").getValue();
		json = new JsonParser().parse(response);
		jobject = json.getAsJsonObject();
		jobject2 = jobject.get("ERROR");
		assertTrue(jobject2 != null);
	}

	@Test
	public void testPostUploadFile() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException,
			IllegalAccessException, FileNotFoundException {
		Client client = Client.create();

		WebResource service = client.resource(UriBuilder.fromUri(
				url + "convert/").build());

		FormDataMultiPart formData = new FormDataMultiPart();
		File file = new File(getClass().getResource("/files/").getFile()+"ARMSTRONG-3.zip");
		formData.bodyPart(new FileDataBodyPart("file", file));
		formData.field("serviceID", serviceID);
		formData.field("filesize", Long.toString(file.length()));
		ClientResponse resp = service.type(MediaType.MULTIPART_FORM_DATA).post(
				ClientResponse.class, formData);
		assertEquals(200, resp.getStatus());
		
		FormDataMultiPart respForm = resp.getEntity(FormDataMultiPart.class);
		String response = respForm.getField("response").getValue();
		JsonElement json = new JsonParser().parse(response);
		JsonObject jobject = json.getAsJsonObject();
		JsonElement jobject2 = jobject.get("CONVERT");
		assertTrue(jobject2 != null);
		FormDataBodyPart owlfile = respForm.getField("owlfile");
		assertTrue(owlfile != null);
	}

//	@Test
	public void testPostUploadFileAndProgress() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException,
			IllegalAccessException, FileNotFoundException, InterruptedException {
		Client client = Client.create();

		class Flag {
			boolean value = false;
		}

		final Flag flag = new Flag();

		new Thread() {
			public void run() {
				Client client = Client.create();
				WebResource service = client.resource(UriBuilder.fromUri(
						url + "convert/").build());

				FormDataMultiPart formData = new FormDataMultiPart();
				File file = new File(getClass().getResource("/files/").getFile()+"ARMSTRONG-3.zip");
				formData.bodyPart(new FileDataBodyPart("file", file));
				formData.field("serviceID", serviceID);
				formData.field("filesize", Long.toString(file.length()));
				ClientResponse resp = service.type(
						MediaType.MULTIPART_FORM_DATA).post(
						ClientResponse.class, formData);
				assertEquals(200, resp.getStatus());
				FormDataMultiPart respForm = resp.getEntity(FormDataMultiPart.class);
				String response = respForm.getField("response").getValue();
				JsonElement json = new JsonParser().parse(response);
				JsonObject jobject = json.getAsJsonObject();
				JsonElement jobject2 = jobject.get("CONVERT");
				assertTrue(jobject2 != null);
				FormDataBodyPart owlfile = respForm.getField("owlfile");
				assertTrue(owlfile != null);
				flag.value = true;
			}
		}.start();

		int prevProgress = 0;
		while (!flag.value) {
			client = Client.create();
			WebResource service = client.resource(UriBuilder.fromUri(
					url + "convert/progress?serviceID=" + serviceID).build());
			ClientResponse resp = service.get(ClientResponse.class);
			assertEquals(200, resp.getStatus());
			String response = resp.getEntity(String.class);
			JsonElement json = new JsonParser().parse(response);
			JsonObject jobject = json.getAsJsonObject();
			JsonElement jobject2 = jobject.get("CONVERT");
			JsonElement jobject3 = jobject.get("ERROR");
			assertTrue(jobject2 != null || jobject3 != null);
			JsonObject jobjectJ = null;
			
			if (jobject2 != null) {
				jobjectJ = jobject2.getAsJsonObject();
				String strStage = jobjectJ.get("stage").getAsString();
				if (strStage == "complete")
					break;
				JsonObject currStage = jobjectJ.get(
						jobjectJ.get("stage").getAsString()).getAsJsonObject();
				int currProgress = currStage.get("progress").getAsInt();
				assertTrue(currProgress >= prevProgress);
				Thread.sleep(300);
			}
		}
	}

}
