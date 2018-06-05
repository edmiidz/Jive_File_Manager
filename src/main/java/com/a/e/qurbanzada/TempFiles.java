package com.a.e.qurbanzada;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TempFiles {

    private static File name_id;
    private static File files_deleted;
    private static File content_id;
    private static String logs_directory;

    private void setLogs_directory(String logs_directory) {
        TempFiles.logs_directory = logs_directory;
    }
    private void setContentId_filePath(String contentId_filePath) {
        content_id = new File(contentId_filePath);
    }
    private void setFilesDeleted(String filesDeleted) {
        files_deleted = new File(filesDeleted);
    }
    private void setFileNameID(String fileNameID) {
        name_id = new File(fileNameID);
    }


    TempFiles(){

        String Res = "/Users/" + System.getProperty("user.name") + "/Resource.txt";
        File res_file = new File(Res);

        if(res_file.exists()){
            Map<Integer,String> res_from_file = new HashMap<>();
            String line;
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(res_file));
                while ( (line = bufferedReader.readLine() ) != null ) {
                    String[] parts = line.split("<-::->", 2 );
                    if ( parts.length >= 2 ) {
                        int key = Integer.parseInt(parts[0]);
                        String value = parts[1];
                        res_from_file.put( key, value );
                    } else {
                        System.out.println( "ignoring line: " + line);
                    }
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            setFileNameID(res_from_file.get(1));
            setFilesDeleted(res_from_file.get(2));
            setContentId_filePath(res_from_file.get(3));
            setLogs_directory(res_from_file.get(4));

        }
        else {
            String directory_path = "/Users/" + System.getProperty("user.name") + "/Documents/Java";
            String logs_directory_path = "/Logs";

            File directory = new File(directory_path);
            File logsDirectory = new File(directory_path + logs_directory_path);

            if(directory.exists()){
                double suffix = Math.random();
                directory_path += "_" + suffix;
                directory = new File(directory_path);
                logsDirectory = new File(directory_path + logs_directory_path);
            }
            else {

            }

            final boolean mkdirs = directory.mkdir();
            final boolean logDir = logsDirectory.mkdir();

            if(mkdirs && logDir){

                String contentId_filePath = directory + "/ContentID.txt";
                String filesDeleted = directory + "/DeletedFiles.txt";
                String fileNameID = directory + "/FileNameID.txt";
                String resources = "/Users/" + System.getProperty("user.name") + "/Resource.txt";

                try {
                    name_id = new File(fileNameID);
                    name_id.createNewFile();

                    files_deleted = new File(filesDeleted);
                    files_deleted.createNewFile();

                    content_id = new File(contentId_filePath);
                    content_id.createNewFile();

                    logs_directory = logsDirectory.getAbsolutePath();

                    File file_resource = new File(resources);
                    updateResources(file_resource.getAbsolutePath(), fileNameID, filesDeleted, contentId_filePath, logs_directory);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFile_name_id() {
        return name_id.getAbsolutePath();
    }
    public static String getText_files_deleted() {
        return files_deleted.getAbsolutePath();
    }
    public static String getText_content_id() {
        return content_id.getAbsolutePath();
    }
    public static String getLogs_directory(){
        return logs_directory;
    }

    private void updateResources(String name,String resource_1,String resource_2,String resource_3,String resource_4){
        Map<Integer,String> resource_map = new HashMap<>();
        int a = 1 , b = 2, c = 3, d = 4;
        resource_map.put(a,resource_1);
        resource_map.put(b,resource_2);
        resource_map.put(c,resource_3);
        resource_map.put(d,resource_4);
        try {
            FileWriter fileWriter = new FileWriter(name,true);
            for(Integer k : resource_map.keySet()){
                fileWriter.write(k + "<-::->" + resource_map.get(k) + "\n");
            }
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
