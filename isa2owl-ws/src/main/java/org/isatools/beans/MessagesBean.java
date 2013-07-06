package org.isatools.beans;

import com.google.gson.JsonObject;

public class MessagesBean extends Bean {
	
	public static final String CONVERT_CANCEL = new MessagesBean(
			ResponseTypes.INFO, 1, "Convert cancelled").toJSONString();
	public static final String CONVERT_FAILED = new MessagesBean(
			ResponseTypes.ERROR, 1, "Convert failed, Please check File")
			.toJSONString();
	public static final String CONVERT_SUCCESS = new MessagesBean(
			ResponseTypes.INFO, 1, "Convert successful").toJSONString();
	
	private String messages;
	private int total;

	public MessagesBean(ResponseTypes type,int total, String messages) {
		super(type);
		this.total = total;
		this.messages=Character.toUpperCase(messages.charAt(0))
		+ messages.substring(1);
	}

	public String getMessages() {
		return messages;
	}

	public int getTotal() {
		return total;
	}
	
	@Override
	public JsonObject toJSONObject() {
		JsonObject obj=new JsonObject();
		obj.addProperty("total", total);
		obj.addProperty("messages", messages);
		JsonObject mainobj = new JsonObject();
		mainobj.add(responseType.toString(), obj);
		return mainobj;
	}
	
	@Override
	public String toJSONString() {
		return toJSONObject().toString();
	}

	public MessagesBean copy() {
		return new MessagesBean(this.responseType, total, messages);
	}
}
