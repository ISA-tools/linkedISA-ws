package org.isatools.handlers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileDeleteStrategy;
import org.isatools.beans.ConvertBean.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConvertHandlerTest {

	private ConvertHandler handler;

	@Before
	public void before() {
		new File(getClass().getResource("/").getFile()+"converted/").mkdirs();
		new File(getClass().getResource("/").getFile()+"converted/cancel/").mkdirs();
	}

	@After
	public void after() throws IOException {
		handler.cancel();
		File files = new File(getClass().getResource("/converted/").getFile());
		if (files.exists())
			for (File file : files.listFiles()) {
				try {
					if (file.exists())
						FileDeleteStrategy.FORCE.delete(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}

	@Test
	public void testUpload() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {

		File files = new File(getClass().getResource("/files/").getFile());
		for (File f : files.listFiles()) {
			InputStream is = new FileInputStream(f);
			handler = new ConvertHandler("testid",f.getName(),f.length());
			handler.init(is, getClass().getResource("/converted/").getFile());
			Method method = ConvertHandler.class.getDeclaredMethod("upload",
					Stage.class);
			method.setAccessible(true);
			method.invoke(handler, new Stage(0));
			File[] filesconverted = new File(handler.getFilePath()).listFiles();
			File file2 = filesconverted[filesconverted.length - 1];
			assertEquals(f.getName(), file2.getName());
			assertEquals(f.length(), file2.length());
			assertEquals(f.getTotalSpace(), file2.getTotalSpace());
			assertEquals(f.getUsableSpace(), file2.getUsableSpace());
		}
	}


	@Test
	public void testConvert() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {
		createFiles();
		Method method = ConvertHandler.class.getDeclaredMethod("convert",
				Stage.class, File.class);
		method.setAccessible(true);
		File files = new File(getClass().getResource("/converted/").getFile()
				+ handler.getServiceID());
		for (File f : files.listFiles()) {
			File file = f.listFiles()[0];
			File result = (File) method.invoke(handler,
					new Stage(0), file);
		}

	}

	private void createFiles() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {
		File files = new File(getClass().getResource("/files/").getFile());
		for (File f : files.listFiles()) {
			InputStream is = new FileInputStream(f.getPath());
			handler = new ConvertHandler("testid",f.getName(),f.length());
			handler.init(is, getClass().getResource("/converted/").getFile());
			Method method = ConvertHandler.class.getDeclaredMethod("upload",
					Stage.class);
			method.setAccessible(true);
			method.invoke(handler, new Stage(0));
		}
	}

	@Test
	public void testPersist() {

	}

	@Test
	public void testCancel() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InterruptedException, IOException,
			NoSuchFieldException {
		File files = new File(getClass().getResource("/files/").getFile());

		for (File f : files.listFiles()) {
			InputStream is = new FileInputStream(f);
			new File(getClass().getResource("/converted/").getFile()+"cancel").mkdir();
			handler = new ConvertHandler("testid",f.getName(),f.length());
			handler.init(is, getClass().getResource("/converted/cancel/").getFile());
			new Thread() {
				public void run() {
					handler.startConverting();
				}
			}.start();
			Thread.sleep(3000);
			String path=handler.getDeletePath();
			try {
				handler.cancel();
			} catch (IOException e) {

			}
			handler.destroy();
		}

		assertEquals(0, new File(getClass().getResource("/converted/cancel/")
				.getFile()).listFiles().length);
	}

}
