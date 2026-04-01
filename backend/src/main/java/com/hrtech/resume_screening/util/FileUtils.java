package com.hrtech.resumescreening.util;

public class FileUtils {

    public static String getFileExtension(String fileName) {

        if(fileName == null || fileName.isEmpty()){
            return "";
        }

        int lastDot = fileName.lastIndexOf(".");

        if(lastDot == -1){
            return "";
        }

        return fileName.substring(lastDot + 1);
    }

    public static boolean isResumeFile(String fileName){

        String extension = getFileExtension(fileName);

        return extension.equalsIgnoreCase("pdf")
                || extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx");
    }

}