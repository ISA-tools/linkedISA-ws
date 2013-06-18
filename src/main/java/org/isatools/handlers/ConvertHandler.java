package org.isatools.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileDeleteStrategy;
import org.isatools.beans.Bean.ResponseTypes;
import org.isatools.beans.ConvertBean;
import org.isatools.beans.ConvertBean.Stage;
import org.isatools.beans.MessagesBean;
import org.isatools.isa2owl.converter.ISAtab2OWLConverter;
import org.isatools.isa2owl.mapping.ISA2OWLMappingParser;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMapping;
import org.isatools.isa2owl.mapping.ISASyntax2OWLMappingFiles;
import org.isatools.utils.FileUnzipper;

public class ConvertHandler {

	private static Logger log = Logger.getLogger(ConvertHandler.class.getName());

	private static List<Thread> runningDeletions;

	static{
		runningDeletions=new ArrayList<Thread>();
	}
	
	public enum STATE {
		STARTED, STOPPED, READY, CANCEL
	}

	private static final int CANCEL_WAIT_TIME = 10000;

	private final String serviceID;
	private InputStream uploadedFile;
	private ConvertBean bean;
	private volatile Enum<STATE> state;
	private Object monitor;
	private String filepath;
	private String deletePath;
	private File owlFile;

	public ConvertHandler(String serviceID) {
		this.serviceID = serviceID;
		this.bean = new ConvertBean("uploading", null, -1);
		state = STATE.READY;
	}

	public ConvertHandler(String serviceID, InputStream uploadedFile,
			ConvertBean bean, Enum<STATE> state, Object monitor, String filepath) {
		this.serviceID = serviceID;
		this.uploadedFile = uploadedFile;
		this.bean = bean;
		this.state = state;
		this.monitor = monitor;
		this.filepath = filepath;
	}

	public void startConverting() {
		if (monitor == null) {
			throw new IllegalAccessError("Init must be called first");
		}

		Stage currentStage = null;
		if (state != STATE.READY) {
			state = STATE.STOPPED;
			return;
		}

		state = STATE.STARTED;
		try {

			if (isCancelled())
				return;
			currentStage = bean.getStage("uploading");
			File isaTabFile = upload(currentStage);
			if (isaTabFile == null) {
				currentStage.setErrors(new MessagesBean(ResponseTypes.ERROR, 1,
						"Uploading Failed, Please check File"));
				return;
			}
			log.info(isaTabFile.getPath() + " uploaded");
			currentStage.setProgress(100);

			if (isCancelled())
				return;
			currentStage = bean.getStage("converting");
			File owlFile= convert(currentStage, isaTabFile);
			if (owlFile==null) {
				currentStage.setErrors(new MessagesBean(ResponseTypes.ERROR, 1,
                        "File could not be converted, please check file"));
				return;
			}
			this.owlFile=owlFile;
			log.info(isaTabFile.getPath() + " converted");
			currentStage.setProgress(100);

		} catch (Exception e) {
			currentStage.setErrors(new MessagesBean(ResponseTypes.ERROR, 1,
					bean.getCurrentStage() + " Failed, Server error"));
			e.printStackTrace();
			state = STATE.STOPPED;
			return;
		}
		state = STATE.STOPPED;
		bean.setStage("complete");
	}

	public void destroy() {
		try {
			finalize();
			System.gc();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private File convert(Stage currentStage, File isaTabFile)
            throws InterruptedException, URISyntaxException, IOException {

        String path = isaTabFile.getAbsolutePath();
        if (!isaTabFile.isDirectory()) {
            System.out.println("Unzipping!");
            path = FileUnzipper.unzip(isaTabFile);
        }
        currentStage.setProgress(30);

        String configDir = getClass().getResource("/configurations/isaconfig-default_v2013-02-13/").getFile();
        ISA2OWLMappingParser parser = new ISA2OWLMappingParser();
        String filePath = getClass().getResource("/"+ISASyntax2OWLMappingFiles.ISA_OBI_MAPPING_FILENAME).getFile();
        parser.parseCSVMappingFile(filePath);
       
        currentStage.setProgress(40);
        ISASyntax2OWLMapping mapping = parser.getMapping();
        ISAtab2OWLConverter isatab2owl = new ISAtab2OWLConverter(configDir, mapping);
        String iri = "http://isa-tools.org/isa/isa.owl";
        try{
        isatab2owl.convert(path, iri);
        currentStage.setProgress(60);
        }catch (NullPointerException e){
            return null;
        }
        String savePath = isaTabFile.getParent() +"/"+ isaTabFile.getName().substring(0,isaTabFile.getName().lastIndexOf(".")) + ".owl";
        isatab2owl.saveOntology(savePath);
        File owl=new File(savePath);
        currentStage.setProgress(90);
        return owl;
	}

	private File upload(Stage currentStage) {
		bean.setStage("uploading");
		long filesize = bean.getFilesize();
		File file = new File(filepath + bean.getFilename());
		file.getParentFile().mkdirs();
		System.out.println("Saving to " + file.getAbsolutePath());
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedFile.read(bytes)) != -1
					&& state != STATE.STOPPED) {
				fos.write(bytes, 0, read);

				// Update progress
				// 75% left to complete uploading, calculate progress made
				// after each write
				int progressMade = (int) (read * 75 / filesize);
				currentStage.setProgress(currentStage.getProgress()
						+ progressMade);
			}
		} catch (IOException e) {
			file = null;
			e.printStackTrace();
		} finally {
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				file = null;
			}
		}

		return file;
	}

	private boolean isCancelled() {
		if (state == STATE.CANCEL) {
			state = STATE.STOPPED;
			synchronized (monitor) {
				monitor.notify();
			}
			return true;
		}
		return false;
	}

	public ConvertBean bean() {
		return bean;
	}

	public void cancel() throws IOException {
		String path = this.filepath;
		bean.setCurrentStage("cancelled");
		if (state == STATE.STARTED) {
			state = STATE.CANCEL;
			int timeout = 0;
			synchronized (monitor) {
				while (state != STATE.STOPPED && timeout <= CANCEL_WAIT_TIME) {
					try {
						timeout += 2000;
						monitor.wait(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			state = STATE.STOPPED;
		}
		log.info(path + " cancelled");
	}

	public void init(InputStream uploadedFile, String uploadDirectory,
			String filename, long filesize) {
		this.uploadedFile = uploadedFile;
		new File(uploadDirectory).mkdirs();
		bean.setFilename(filename);
		bean.setFilesize(filesize);
		bean.getStage("uploading").setProgress(25);
		monitor = new Object();
		String fileAsDir = filename.substring(0, filename.lastIndexOf("."));
		this.deletePath = uploadDirectory + serviceID;
		this.filepath = uploadDirectory + serviceID + "/" + fileAsDir + "/";
	}

	public String getServiceID() {
		return serviceID;
	}

	public String getFilePath() {
		return filepath;
	}

	public Enum<STATE> getState() {
		return state;
	}

	public void setState(Enum<STATE> state) {
		this.state = state;
	}
	
	public synchronized static void cleanUp(final String path) {
		File f = null;
		if (path != null) {
			f = new File(path);
			if (f.exists())
				try {
					FileDeleteStrategy.FORCE.delete(f);
				} catch (IOException e) {
					e.printStackTrace();
					if (runningDeletions.contains(Thread.currentThread()))
						runningDeletions.remove(Thread.currentThread());
					else {
						// Try again in a minute
						Thread t = new Thread() {
							public void run() {
								try {
									sleep(30000);
									ConvertHandler.cleanUp(path);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						};
						runningDeletions.add(t);
						t.start();
					}
				}
		}
	}

	
	public File getOwlFile() {
		return owlFile;
	}
	
	public String getDeletePath() {
		return this.deletePath;
	}

}
