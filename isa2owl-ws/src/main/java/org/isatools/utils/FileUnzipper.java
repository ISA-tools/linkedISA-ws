package org.isatools.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUnzipper {
	public static final int BUFFER_SIZE = 1024;
    // return name of parent directory to now use!

    public static String unzip(File toUnpack) throws IOException {
        ZipFile zf = new ZipFile(toUnpack);

        String parentDir = toUnpack.getParent();
        String pathToReturn = parentDir;

        Enumeration<? extends ZipEntry> entries = zf.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            // ignore any silly files as a result of the mac os file system (e.g. __MACOSX or .DS_STORE)
            if (!entry.getName().startsWith("_") && !entry.getName().startsWith(".")) {
                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    File newDirectory = new File(parentDir + File.separator + entry.getName());
                    pathToReturn = newDirectory.getPath();
                    if (!newDirectory.exists()) {
                        newDirectory.mkdirs();
                    }
                    continue;
                }
                copyInputStream(zf.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(parentDir + File.separator + entry.getName())));
            }
        }


        zf.close();
        return pathToReturn;

    }

    public static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

}
