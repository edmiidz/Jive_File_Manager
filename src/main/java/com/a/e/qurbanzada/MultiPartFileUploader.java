package com.a.e.qurbanzada;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Ahmad Qurbanzada on 02/06/2017.
 */
public class MultiPartFileUploader {
    private static final int UPDATE = 100;
    private static final int CREATE = 101;
    private String contentURL = "https://" + community_url + "/api/core/v3/contents/";
    private String parentPlace;
    private String source;
    private String filePath = TempFiles.getText_content_id();
    public File[] files;
    private String categories;
    private String tags;

    public List<String> uploaded = new ArrayList<>();
    public List<String> notUploaded = new ArrayList<>();
    public Map<String,String> map = new HashMap<>();

    public HashMap<String, String> mapID = new HashMap<>();
    public List<String> noUpdate = new ArrayList<>();
    public List<String> updated = new ArrayList<>();
    private ArrayList<File> filesArrayList = new ArrayList<>();
    private MultipartUtility multipartUtility;
    private static String community_url;

    static void setCommunity_url(String url){
        community_url = url;
    }

    MultiPartFileUploader(String id, String dir, String categoryVal, String tagVal){
        parentPlace = "'https://" + community_url + "/api/core/v3/places/" + id + "'";
        source = dir;
        categories = categoryVal;
        tags = tagVal;
    }

    public MultipartUtility getMultipartUtility() {
        return multipartUtility;
    }

    public void init() {
        if( source != null ) {
            listFiles( source );
            files = new File[filesArrayList.size()];
            System.out.println( filesArrayList.size() );
            files = filesArrayList.toArray(files);
        }
        populateMap( filePath , mapID );
    }

    private void populateMap( String filePath, HashMap mapID ) {
        try {
            BufferedReader reader = new BufferedReader( new FileReader( filePath ) );

            String line;
            while ( ( line = reader.readLine() ) != null ) {
                String[] parts = line.split("<-::->", 2);
                if ( parts.length >= 2 ) {
                    String key = parts[0];
                    String value = parts[1];
                    mapID.put( key, value );
                } else {
                    System.out.println("ignoring line: " + line);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(HashMap<String, String> mapID, List<String> noUpdate, List<String> updated, int i) throws IOException, JSONException {
        if(getLastModified(files[i])) {
            String updateURL = System.getProperty("url",contentURL+mapID.get(files[i].getName()));
            String jsonUpdateData = "{'type': 'file','subject':"+files[i].getName()+",'parent':"+parentPlace+",'content':{'type':'text/html','text':''},'tags':["+tags+"],'categories':["+categories+"]}";
            try {
                multipartUtility = new MultipartUtility(updateURL,UPDATE);
                multipartUtility.addJSONData(jsonUpdateData);
                multipartUtility.addFilePart("fileUpload", files[i]);
                String response = multipartUtility.finish();

                if ( multipartUtility.getHttpConn().getResponseCode() == 200 ) {

                    JSONObject jo = new JSONObject(response);

                    if ( response.contains( "updated" ) ) {
                        updated.add( "\n" + ("[" + i + "/" + files.length + "]" + files[i].getName()));
                        System.out.println( files[i].getName() + " - Updated in jive [" + i + "/" + files.length + "]" );
                    }
                }
                else if ( multipartUtility.getHttpConn().getResponseCode() == 404 ) {
                    upload( uploaded , notUploaded , map , i );
                }
                else {
                    System.out.println(files[i].getName()+" - Not Updated in jive ["+i+"/"+files.length+"]"+multipartUtility.getHttpConn().getResponseCode()+" "+multipartUtility.getHttpConn().getResponseMessage());
                    noUpdate.add("\n"+ ("[" + i + "/" + files.length + "]" + files[i].getName() + "  " + multipartUtility.getHttpConn().getResponseCode() + " " + " " + multipartUtility.getHttpConn().getResponseMessage()));
                }

            } catch (IOException ex) {
                System.out.println( "ERROR: " + ex.getMessage() );
                ex.printStackTrace();
            }
        }
        else {
            noUpdate.add( ( "\n" + ("[" + i + "/" + files.length + "] " + files[i].getName() + " " + new Date(files[i].lastModified()))) );
            Date lastModifiedDate = new Date( files[i].lastModified() );
            System.out.println( "Nothing to update - Last modified: " + lastModifiedDate );
        }
    }

    public void upload(List<String> uploaded, List<String> notUploaded, Map<String, String> map, int i) throws JSONException {
        String jsonUploadData = "{'type': 'file', 'subject':"+files[i].getName()+",'parent':"+parentPlace+",'content':{'type':'text/html','text':''},'tags':["+tags+"],'categories':["+categories+"]}";

        try {
            MultipartUtility multipart = new MultipartUtility(contentURL,CREATE);
            multipart.addJSONData(jsonUploadData);
            multipart.addFilePart("fileUpload", files[i]);
            String response = multipart.finish();

            if ( multipart.getHttpConn().getResponseCode() == 201 ) {
                JSONObject jo = new JSONObject( response );
                if ( response.contains( "contentID" ) ) {
                    map.put( files[i].getName() , jo.get( "contentID" ).toString() );
                    System.out.println( files[i].getName() + "- Uploaded to jive [" + i + "/" + files.length + "]" );
                    uploaded.add( "\n" + ("[" + i + "/" + files.length + "] " + files[i].getName()));
                }
            }
            else {
                System.out.println(files[i].getName()+" - Not Uploaded to jive ["+i+"/"+files.length+"] "+multipart.getHttpConn().getResponseCode()+" "+multipart.getHttpConn().getResponseMessage());
                notUploaded.add("\n"+ (files[i].getName() + "  Not Uploaded to jive [" + i + "/" + files.length + "]  " + multipart.getHttpConn().getResponseCode() + "  " + multipart.getHttpConn().getResponseMessage()));
            }
        } catch (IOException ex) {
            System.out.println( "ERROR: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }

    public void recordUploadedFiles( Map<String, String> map ) {
        for(String k : map.keySet()) {
            try {
                FileWriter fileWriter = new FileWriter( filePath,true );
                fileWriter.write(( k + "<-::->" + map.get(k) ) + "\n" );
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }//End for loop
    }

    private boolean getLastModified( File file ) {
        Date modifiedDate;
        modifiedDate = new Date(file.lastModified());

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.YEAR, -1);
        Date alertDate = cal.getTime();
        return modifiedDate.after(alertDate);
    }
    //Logs results of upload and update
    public void log(String update, String notUpdated, String uploaded, String notUploaded) {
        Logger loggerUpdate = Logger.getLogger("MyLog1");
        Logger loggerNotUpdated = Logger.getLogger("MyLog2");
        Logger loggerUploaded = Logger.getLogger("MyLog3");
        Logger loggerNotUploaded = Logger.getLogger("MyLog4");

        FileHandler updateFH,not_updatedFH,uploadedFH, notUploadedFH;

        try {
            // This block configure the logger with handler and formatter
            updateFH = new FileHandler(TempFiles.getLogs_directory() + "/Update.log",true);
            not_updatedFH = new FileHandler(TempFiles.getLogs_directory() + "/NotUpdated.log",true);
            uploadedFH = new FileHandler(TempFiles.getLogs_directory() + "/Uploaded.log",true);
            notUploadedFH = new FileHandler(TempFiles.getLogs_directory() + "/NotUploaded.log",true);

            loggerUpdate.addHandler(updateFH);
            loggerNotUpdated.addHandler(not_updatedFH);

            loggerUploaded.addHandler(uploadedFH);
            loggerNotUploaded.addHandler(notUploadedFH);

            SimpleFormatter formatter = new SimpleFormatter();

            updateFH.setFormatter(formatter);
            not_updatedFH.setFormatter(formatter);
            uploadedFH.setFormatter(formatter);
            notUploadedFH.setFormatter(formatter);

            loggerUpdate.setUseParentHandlers(false);
            loggerNotUpdated.setUseParentHandlers(false);
            loggerUploaded.setUseParentHandlers(false);
            loggerNotUploaded.setUseParentHandlers(false);

            // the following statement is used to log any messages
            loggerUpdate.info("Files Updated:\n\n"+update);
            loggerNotUpdated.info("Files Not Updated:\n\n"+notUpdated);

            loggerUploaded.info("Files Uploaded:\n\n"+uploaded);
            loggerNotUploaded.info("Files Not Uploaded:\n\n"+notUploaded);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // get all the files from a directory
    private void listFiles(String directoryName) {

        File directory = new File(directoryName);
        File[] fileArray = directory.listFiles((dir,name) -> !name.endsWith(".aspx") && !name.endsWith(".master"));

//        String[] excludeList = {"*.xlsm*","*.docx*","*.doc*","*.xlsx*","*.xls*","*.pdf*","*.jpg*","*.png*","*.csv*"};
//        FileFilter fileFilter = new WildcardFileFilter(excludeList);
//        File[] fileArray = directory.listFiles(fileFilter);

//        File[] fileArray = directory.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File pathname) {
//                String name = pathname.getName().toLowerCase();
//                String mac = pathname.getName();
//                return !name.endsWith(".aspx") || !mac.equals(".DS_Store");
//            }
//        });

        for (int i =0;i<fileArray.length;i++) {
            if (fileArray[i].isFile() && fileArray[i].exists()) {
                if(fileArray[i].canWrite()){
                    filesArrayList.add(fileArray[i]);
                }
                else{
                    fileArray[i].setWritable(true,false);
                    filesArrayList.add(fileArray[i]);
                }
            }else {
                if (fileArray[i].isDirectory()) {
                    listFiles(fileArray[i].getAbsolutePath());
                }
            }
        }
    }
    public int getUpdate(){
        return updated.size();
    }
    public int getUpload(){
        return uploaded.size();
    }

}//End class MultiPartFileUploader
