package org.jaggeryjs.jaggery.app.mgt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.wso2.carbon.CarbonException;
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


    /**
     * Generic file validation method that combines all validation logic.
     * Validates file name, extension, and path security.
     *
     * @param fileName          File name to validate
     * @param baseDirectory     Base directory where files should be contained
     * @param allowedExtensions Array of allowed file extensions (without dot),
     *                          e.g., {"csv", "xml"}
     * @throws CarbonException if validation fails
     */
    public static void validateFileName(String fileName, String baseDirectory, String[] allowedExtensions)
            throws CarbonException {
        // 1. Basic null/empty validation
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("Invalid file name. File name is not available");
        }

        fileName = fileName.trim();

        // 2. File extension validation
        if (allowedExtensions != null && allowedExtensions.length > 0) {
            boolean validExtension = false;
            String lowerFileName = fileName.toLowerCase();

            for (String extension : allowedExtensions) {
                if (lowerFileName.endsWith("." + extension.toLowerCase())) {
                    validExtension = true;

                    // Ensure filename has content before the extension
                    int minLength = extension.length() + 1; // extension + dot
                    if (fileName.length() <= minLength) {
                        throw new RuntimeException(
                                "Invalid file name: filename cannot be just an extension. File: " + fileName);
                    }
                    break;
                }
            }

            if (!validExtension) {
                String allowedExts = String.join(", ", allowedExtensions);
                throw new RuntimeException(
                        "Invalid file type. Only " + allowedExts + " files are allowed. File: " + fileName);
            }
        }

        // 3. Path validation
        if (baseDirectory != null) {
            try {
                // Canonical path validation
                File baseDir = new File(baseDirectory);
                File targetFile = new File(baseDir, fileName);

                String baseDirCanonical = baseDir.getCanonicalPath();
                String targetFileCanonical = targetFile.getCanonicalPath();

                // Ensure resolved path stays within base directory
                if (!targetFileCanonical.startsWith(baseDirCanonical + File.separator) &&
                        !targetFileCanonical.equals(baseDirCanonical)) {
                    throw new RuntimeException("File validation failed: Invalid file name: " + fileName);
                }

            } catch (IOException e) {
                throw new CarbonException("File validation failed: Invalid file name: " + fileName);
            }
        }
    }

    /**
     * Convenience method for file validation.
     * 
     * @param fileName      File name to validate
     * @param baseDirectory Base directory where files should be contained
     * @throws CarbonException if validation fails
     */
    public static void validateFileName(String fileName, String baseDirectory) throws CarbonException {
        validateFileName(fileName, baseDirectory, new String[] { "jag", "zip" });
    }

    /**
     * Validate the given fileName and path.
     *
     * @param fileName      File name which needs to be validated.
     * @param baseDirectory Base directory where files should be contained
     * @throws CarbonException if validation fails
     */
    public static void validatePath(String fileName, String baseDirectory) throws CarbonException {
        validateFileName(fileName, baseDirectory, null);
    }
}
