package org.isatools.beans;

import com.google.gson.JsonObject;

public abstract class Bean {

	public enum ResponseTypes {
		ERROR("ERROR"), CONVERT("CONVERT"), INFO("INFO"), WARNING("WARNING");

		private String name;

		ResponseTypes(String name) {
			this.name = name;
		}
		
		public String toString(){
			return name;
		}
	}

	protected ResponseTypes responseType;
	
	public Bean(ResponseTypes type) {
		this.responseType=type;
	}

	public abstract String toJSONString();

	public abstract JsonObject toJSONObject();
	
}
