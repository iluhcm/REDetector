/**
 * ���ܣ�
 * 	1���ļ���ȡ��ɾ��
 */

package com.xl.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class FileUtils {

    public static void writeData(String path, String content) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(file);
            out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static boolean createDirectory(String path) {
        File f = new File(path);
        return f.exists() || f.mkdirs();
    }

    public static String getFileNameFromURL(String url) {

        int lastIndexOfSlash = url.lastIndexOf("/");

        String fileName;

        if (lastIndexOfSlash > -1) {
            fileName = url.substring(lastIndexOfSlash + 1, url.length());
        } else {
            fileName = url;
        }

        return fileName;
    }

    /**
     * @param path
     * @param content
     */
    public static void appendData(String path, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(path, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            if (file.delete()) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param path
     * @return
     */
    public static boolean deleteFileWithSuffix(String path, String suffix) {
        boolean flag = false;
        File file = new File(path);
        if (file.isFile() && file.exists() && file.getName().endsWith(suffix)) {
            if (file.delete()) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param path
     * @return
     */
    public static boolean deleteDirectory(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param path
     * @return
     */
    public static boolean deleteAllFilesWithSuffix(String path, String suffix) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFileWithSuffix(files[i].getAbsolutePath(), suffix);
                if (!flag)
                    break;
            }
        }
        return flag;
    }

    private static List<File> fileList = new ArrayList<File>();

    public static List<File> researchfile(String fileName, File directory) {
        if (directory.isDirectory()) {
            File[] filearry = directory.listFiles();
            if (filearry != null) {
                for (File f : filearry) {
                    if (f.isDirectory()) {
                        researchfile(fileName, f);
                    } else {
                        if (f.getAbsolutePath().contains(fileName)) {
                            fileList.add(f);
                        }
                    }
                }
            }
        }
        return fileList;
    }

}
