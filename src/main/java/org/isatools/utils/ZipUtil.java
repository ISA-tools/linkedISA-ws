package org.isatools.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipUtil {

    private static Logger log = Logger.getLogger(ZipUtil.class.getName());

    private Set<String> addedFilePaths;


    public ZipUtil() {
        addedFilePaths = new HashSet<String>();
    }

    /**
     * Zips up a list of files.
     *
     * @param archiveName - What do you want to call the archive
     * @param directory   - Directory to save archive in
     * @param files    - List of ISAfiles to be zipped up
     * @return Returns null if unsuccessful, final zip file otherwise.
     */
    public File zipDirectoryContents(String archiveName, String directory, File[] files) {

        File archiveLocation;
        byte[] buffer = new byte[1024];
        ZipOutputStream out;

        try {

            archiveLocation = new File(directory + File.separator + archiveName + ".zip");
            log.info("Attempting output to " + archiveLocation.getAbsolutePath());
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archiveLocation)));

            out.setLevel(Deflater.BEST_SPEED);

            // zip up the rest of the ISAfiles
            log.info("zipping files");

            for (File file : files) {
                if (file.isFile()) {
                    try {
                        zipFile(file, out, buffer);
                    } catch (Exception e) {
                        log.error("Bad news from zipsville..." + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            out.close();
            return archiveLocation;

        } catch (FileNotFoundException e) {
            log.error("file not found..." + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            log.error("IOException... " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void zipFile(File file, ZipOutputStream out, byte[] buffer) throws IOException {

        if (!addedFilePaths.contains(file.getAbsolutePath())) {

            FileInputStream fis = new FileInputStream(file);

            // check if file has been added already before getting to this stage.
            out.putNextEntry(new ZipEntry(file.getName()));

            int length;
            while ((length = fis.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.closeEntry();
            fis.close();
            addedFilePaths.add(file.getAbsolutePath());
        } else {
            log.info(file.getAbsolutePath() + " has already been added to zip directory. We're ignoring it");
        }
    }

    private String getPathForZip(String sourceDir, String path) {
        return path.substring(path.lastIndexOf(sourceDir));
    }

}
