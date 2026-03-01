package net.kdt.pojavlaunch.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    /**
     * Check if a file denoted by a String path exists.
     * @param filePath the path to check
     * @return whether it exists (same as File.exists()
     */
    public static boolean exists(String filePath){
        return new File(filePath).exists();
    }

    /**
     * Get the file name from a path/URL string.
     * @param pathOrUrl the path or the URL of the file
     * @return the file's name
     */
    public static String getFileName(String pathOrUrl) {
        int lastSlashIndex = pathOrUrl.lastIndexOf('/');
        if(lastSlashIndex == -1) return null;
        return pathOrUrl.substring(lastSlashIndex);
    }

    /**
     * Remove the extension (all text after the last dot) from a path/URL string.
     * @param pathOrUrl the path or the URL of the file
     * @return the input with the extension removed
     */
    public static String removeExtension(String pathOrUrl) {
        int lastDotIndex = pathOrUrl.lastIndexOf('.');
        if(lastDotIndex == -1) return pathOrUrl;
        return pathOrUrl.substring(0, lastDotIndex);
    }

    /**
     * Ensure that a directory exists, is a directory and is writable.
     * @param targetFile the directory to check
     * @return if the check has succeeded
     */
    public static boolean ensureDirectorySilently(File targetFile) {
        if(targetFile.isFile()) targetFile.delete();
        if(targetFile.isDirectory()) return targetFile.canWrite();
        try {
            createDirectoriesFromAncestor(targetFile);
            return targetFile.isDirectory();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Ensure that the parent directory of a file exists and is writable
     * @param targetFile the File whose parent should be checked
     * @return if the check as succeeded
     */
    public static boolean ensureParentDirectorySilently(File targetFile) {
        File parentFile = targetFile.getParentFile();
        if(parentFile == null) return false;
        return ensureDirectorySilently(parentFile);
    }

    /**
     * Same as ensureDirectorySilently(), but throws an IOException telling why the check failed.
     * @param targetFile the directory to check
     * @throws IOException when the checks fail
     */
    public static void ensureDirectory(File targetFile) throws IOException{
        if(targetFile.isFile()) {
            if (!targetFile.delete()) {
                throw new IOException("Target directory is a file and cannot be deleted: " + targetFile.getAbsolutePath());
            }
        }
        if(targetFile.isDirectory()) {
            if(!targetFile.canWrite()) throw new IOException("Target directory is not writable: " + targetFile.getAbsolutePath());
            return;
        }
        createDirectoriesFromAncestor(targetFile);
        if(!targetFile.isDirectory()) {
            throw new IOException("Unable to create target directory: " + targetFile.getAbsolutePath());
        }
    }

    /**
     * Create directories by walking UP to find the deepest existing ancestor,
     * then creating each level one by one going DOWN.
     * This avoids Android 11+ scoped storage issues where mkdirs() fails because
     * it tries to stat restricted parent directories above the FUSE boundary.
     */
    private static void createDirectoriesFromAncestor(File targetFile) throws IOException {
        if (targetFile.isDirectory()) return;

        // First try mkdirs() - works fine for internal storage and pre-Android 11
        targetFile.mkdirs();
        if (targetFile.isDirectory()) return;

        // mkdirs() failed (likely scoped storage on Android 11+).
        // Walk UP to find the deepest accessible ancestor directory.
        java.util.ArrayList<String> segments = new java.util.ArrayList<>();
        File current = targetFile;
        while (current != null && !current.isDirectory()) {
            segments.add(0, current.getName());
            current = current.getParentFile();
        }

        if (current == null) {
            throw new IOException("No existing ancestor directory found for: " + targetFile.getAbsolutePath());
        }

        // Walk DOWN from the found ancestor, creating one directory at a time
        File base = current;
        for (String segment : segments) {
            base = new File(base, segment);
            if (base.isFile()) base.delete();
            if (!base.isDirectory()) {
                base.mkdir();
            }
        }
    }

    /**
     * Same as ensureParentDirectorySilently(), but throws an IOException telling why the check failed.
     * @param targetFile the File whose parent should be checked
     * @throws IOException when the checks fail
     */
    public static void ensureParentDirectory(File targetFile) throws IOException{
        File parentFile = targetFile.getParentFile();
        if(parentFile == null) throw new IOException("targetFile does not have a parent");
        ensureDirectory(parentFile);
    }
}