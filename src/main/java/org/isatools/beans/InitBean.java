package org.isatools.beans;

import com.google.gson.JsonObject;

public class InitBean extends Bean {
	private String serviceID;
	private ConvertBean convertBean;

	public InitBean(String serviceID, ConvertBean convertBean) {
		super(ResponseTypes.INFO);
		this.serviceID=serviceID;
		this.convertBean=convertBean;
	}

	
	public String getUploadID() {
		return serviceID;
	}

	@Override
	public JsonObject toJSONObject() {
		JsonObject obj = convertBean.toJSONObject();
		JsonObject uploadObj = new JsonObject();
		uploadObj.addProperty("serviceID", serviceID);
		obj.add(responseType.toString(), uploadObj);
		return obj;
	}
	
	@Override
	public String toJSONString() {
		return toJSONObject().toString();
	}
}
