package org.jaggeryjs.jaggery.app.mgt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JaggeryDeploymentUtil {

    private static final Log log = LogFactory.getLog(
            JaggeryDeploymentUtil.class);

    private static final int BYTE_BUFFER_SIZE = 8192;
    
    private JaggeryDeploymentUtil() {
        //disable external instantiation
    }
    
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    static File getConfig(File webAppFile) {
        if (webAppFile.isDirectory()) {
            File f = new File(webAppFile + File.separator + JaggeryCoreConstants.JAGGERY_CONF_FILE);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_OUT"})
    static void unZip(InputStream is, String destDir) {
    	
        try {
            File unzipDestinationDirectory = new File(destDir);
            if (!unzipDestinationDirectory.mkdir()) {
                log.error("could not create " + destDir);
            }
            BufferedOutputStream dest = null;
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File entryDir = new File(unzipDestinationDirectory.getAbsolutePath(), entry.getName());
                    boolean created = entryDir.mkdir();
                    if (!created) {
                    	log.error("Could not create DIR : " + unzipDestinationDirectory.getAbsolutePath() + 
                    			File.separator + entry.getName());
                    }
                } else {
                    int count;
                    byte data[] = new byte[BYTE_BUFFER_SIZE];
                    // write the files to the disk
                    FileOutputStream fos = null;
                    try {
                        final File zipEntryFile = new File(unzipDestinationDirectory.getAbsolutePath(), entry.getName());
                        if (!zipEntryFile.toPath().normalize().startsWith(unzipDestinationDirectory.getAbsolutePath())) {
                            throw new IOException("Bad zip entry");
                        }
                        fos = new FileOutputStream(zipEntryFile);
	                    dest = new BufferedOutputStream(fos, BYTE_BUFFER_SIZE);
	                    while ((count = zis.read(data, 0, BYTE_BUFFER_SIZE))
	                            != -1) {
	                        dest.write(data, 0, count);
	                    }
	                    dest.flush();
                    } catch (IOException e) {
                    	log.error("Error opening output stream " + entry.getName(), e);
                    } finally {
                    	try {
	                    	if (dest != null ) {
	                    		dest.close();
	                    	}
                    	} catch (IOException e) {
                    		log.error("Error closing output stream " + entry.getName(), e);
                    	}
                    }
                    
                }
            }
            zis.close();
        } catch (IOException e) {
            log.error("Could not unzip the Jaggery App Archive", e);
        } 
    }
}
