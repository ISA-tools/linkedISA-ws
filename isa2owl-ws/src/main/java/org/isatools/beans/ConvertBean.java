package org.isatools.beans;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConvertBean extends Bean {

	public static class Stage {
		protected int progress;
		private MessagesBean ERROR;
		private MessagesBean WARNING;

		public MessagesBean getError() {
			return ERROR;
		}

		public MessagesBean getWarning() {
			return WARNING;
		}

		public Stage(int progress) {
			this.progress = progress;
		}

		public int getProgress() {
			return progress;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		public void setErrors(MessagesBean ERROR) {
			this.ERROR = ERROR;
		}

		public void setWarnings(MessagesBean WARNING) {
			this.WARNING = WARNING;
		}

		public Stage copy() {
			Stage stage = new Stage(this.progress);
			if (this.ERROR != null) {
				stage.setErrors(this.ERROR.copy());
			}
			if (this.WARNING != null) {
				stage.setWarnings(this.WARNING.copy());
			}
			return stage;
		}
	}

	private String stage;
    private Stage uploading;
	private Stage converting;
	private String filename;
	private long filesize;
	private Stage complete;
	private Stage cancelled;

	public ConvertBean(String stage, String filename, long filesize) {
		super(ResponseTypes.CONVERT);
        uploading=new Stage(0);
        converting=new Stage(0);
		complete = new Stage(0);
		cancelled = new Stage(0);
		this.filename = filename;
		this.filesize = filesize;
		this.stage = stage;
	}

	public ConvertBean() {
		super(ResponseTypes.CONVERT);
		this.stage = "uploading";
        uploading = new Stage(0);
        converting = new Stage(0);
		uploading = new Stage(0);
	}

	public ConvertBean(String stage, Stage uploading, Stage converting,Stage complete, Stage cancelled, String filename,
                       long filesize) {
		super(ResponseTypes.CONVERT);
		this.stage = stage;
		this.uploading = uploading;
		this.converting = converting;
		this.complete = complete;
		this.cancelled = cancelled;
		this.filename = filename;
		this.filesize = filesize;
	}

	public Stage getStage(String stage) {
		if (stage.equals("uploading"))
			return uploading;
		if (stage.equals("converting"))
			return converting;
		if (stage.equals("complete"))
			return complete;
		if (stage.equals("cancelled"))
			return cancelled;
		return null;
	}

	public String getCurrentStage() {
		return this.stage;
	}

	public void setCurrentStage(String stage) {
		this.stage = stage;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public ConvertBean copy() {
		Stage uploading = this.uploading.copy();
		Stage converting = this.converting.copy();
		Stage complete = this.complete.copy();
		Stage cancelled = this.cancelled.copy();
		return new ConvertBean(stage, uploading, converting,
				complete, cancelled, this.filename, this.filesize);
	}

	@Override
	public String toJSONString() {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(this);
		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
		jsonObj.remove("responseType");
		JsonObject mainObj = new JsonObject();
		mainObj.add(responseType.toString(), jsonObj);
		return gson.toJson(mainObj);
	}

	@Override
	public JsonObject toJSONObject() {
		JsonParser parser = new JsonParser();
		return parser.parse(toJSONString()).getAsJsonObject();
	}
}
