package com.a.e.qurbanzada;

import com.sun.jersey.core.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Get {
    private List<String> content_ID_list = new ArrayList<>();
    private Map<String,List> content_name_size = new HashMap<>();
    private Map<String,String> parentPlaceName = new HashMap<>();
    private List<JSONObject> jsonObjects = new ArrayList<>();
    private Map<String,String> place_name_id = new HashMap<>();
    private static String username = null;
    private static String password = null;
    private static String user = null;
    private int response_code = 0;
    private HashMap map_from_text_file = new HashMap();
    private List<String> place_Categories = new ArrayList<>();
    private Map<String,String> binary_map = new HashMap<>();
    private  Map<String,String> people_map = new HashMap<>();
    public Map<String, String> getPeople_map() {
        return people_map;
    }
    private static String avatarUrl;

    private static List<String> userDetail = new ArrayList<>();

    public List<String> get(String uri, List<String> filter_list) throws IOException, JSONException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);
        String getPass = username + ":" + password;
        String getAuth = "Basic " + new String(new Base64().encode(getPass.getBytes()));
        httpGet.setHeader("Authorization", getAuth);
        httpGet.setHeader("Content-Type","application/json");

        CloseableHttpResponse closeableHttpResponse = httpclient.execute(httpGet);

        response_code = closeableHttpResponse.getStatusLine().getStatusCode();

        System.out.println(closeableHttpResponse.getStatusLine());

        if( response_code == 200 ){
            try {
                HttpEntity httpEntity = closeableHttpResponse.getEntity();
                InputStream inputStream = httpEntity.getContent();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                StringBuilder stringBuilder = new StringBuilder();
                while ( true ) {
                    int data = bufferedInputStream.read();
                    if( data == -1 ) {
                        break;
                    }
                    else {
                        stringBuilder.append( ( char ) data );
                    }
                }

                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                exploreList(jsonObject,filter_list);

                if ( jsonObject.has("links" ) ){
                    JSONObject links = jsonObject.getJSONObject( "links" );
                    if ( links.has("next" ) ){
                        if ( links.getString("next" ).startsWith( "https" ) ){
                            get( links.getString("next") , filter_list);
                        }
                    }
                }

                cache ( TempFiles.getFile_name_id(), place_name_id );

                if( place_Categories.size() > 0 ){
                    return place_Categories;
                }
                else {
                    return content_ID_list;
                }
            } finally {
                closeableHttpResponse.close();
            }
        }
        else{
            System.out.println( closeableHttpResponse.getStatusLine() );
            return null;
        }
    }

    //Explore json list and collect data
    private void exploreList(JSONObject jsonObject, List<String> filters) throws JSONException {
        if ( jsonObject.has("list") ) {

            JSONArray jsonArray = jsonObject.getJSONArray("list");

            if(filters == null){
                filters = new ArrayList<>();
                filters.add("Filter");
            }
            for (int i = 0; i < jsonArray.length(); i++ ) {

                JSONObject json_key_value = jsonArray.getJSONObject(i);
                List<String> list_for_map = new ArrayList<>();

                for (String filter : filters){
                    switch (filter){

                        case "file":
                            if (json_key_value.has("size")) {
                                getFilteredData(json_key_value, list_for_map);
                            }
                            break;
                        case "document":
                            if (json_key_value.has("type") && json_key_value.getString("type").equals("document")) {
                                getFilteredData(json_key_value, list_for_map);
                            }
                            break;
                        case "blog":
                            if ( json_key_value.has("type") && json_key_value.getString("type").equals("post") ) {
                                getFilteredData(json_key_value, list_for_map);
                            }
                            break;
                        default:
                            if ( json_key_value.has("contentID") ) {
                                getFilteredData(json_key_value, list_for_map);
                            }
                    }
                }

                if ( json_key_value.has("parentPlace" ) ){
                    JSONObject parentPlace = json_key_value.getJSONObject( "parentPlace" );
                    parentPlaceName.put(parentPlace.getString("name" ),parentPlace.getString("type"));
                }

                if ( json_key_value.has("placeID" ) && json_key_value.has("type" ) ){
                    if ( json_key_value.getString("type" ).equals( "group" ) || json_key_value.getString("type" ).equals( "space" ) ||
                            json_key_value.getString("type" ).equals( "project" ) ){

                        String plc_name = json_key_value.getString("name");
                        String plc_ID = json_key_value.getString("placeID");
                        place_name_id.put(plc_name, plc_ID);
                    }
                }
                if ( json_key_value.has("type" ) ){
                    if ( json_key_value.getString("type" ).equals( "category" ) ){
                        place_Categories.add( json_key_value.getString("name"));
                    }
                }

                if( json_key_value.has("type") && json_key_value.getString("type").equals("person")){
                    people_map.put(json_key_value.getString("displayName"),json_key_value.getString("id"));
                }
            }
        }
        else {
            if ( jsonObject.has("displayName" ) ){
                user = jsonObject.getString("displayName");
            }
            if (userDetail.isEmpty()){
                if (jsonObject.has("emails")){
                    JSONArray email = jsonObject.getJSONArray("emails");
                    if (!email.isNull(0)){
                        userDetail.add(email.getJSONObject(0).getString("jive_label"));
                        userDetail.add(email.getJSONObject(0).getString("value"));
                    }

                }
                if (jsonObject.has("jive")){

                    JSONObject profile = jsonObject.getJSONObject("jive");
                    JSONArray profiles = profile.getJSONArray("profile");

                    if (!profiles.isNull(0)){
                        userDetail.add(profiles.getJSONObject(0).getString("jive_label"));
                        userDetail.add(profiles.getJSONObject(0).getString("value"));
                    }
                    if (!profiles.isNull(1)){
                        userDetail.add(profiles.getJSONObject(1).getString("jive_label"));
                        userDetail.add(profiles.getJSONObject(1).getString("value"));
                    }
                    if (!profiles.isNull(3)){
                        userDetail.add(profiles.getJSONObject(3).getString("jive_label"));
                        userDetail.add(profiles.getJSONObject(3).getString("value"));
                    }
                    if (!profiles.isNull(5)){
                        userDetail.add(profiles.getJSONObject(5).getString("jive_label"));
                        userDetail.add(profiles.getJSONObject(5).getString("value"));
                    }
                }
            }
            if (jsonObject.has("thumbnailUrl")){
//                userDetail.add(jsonObject.getString("thumbnailUrl"));
                avatarUrl = jsonObject.getString("thumbnailUrl");
            }
        }
    }

    private void getFilteredData(JSONObject json_key_value, List<String> list_for_map) throws JSONException {
        content_ID_list.add( json_key_value.getString("contentID" ) );
        jsonObjects.add( json_key_value );

        int size = 0;
        if (json_key_value.has("size" )) {

            size = (int) json_key_value.get( "size" );
        }

        if (size != 0){
            list_for_map.add(String.valueOf(size));
        }
        else {
            list_for_map.add("N/A");
        }

        if (json_key_value.has("contentType")){

            list_for_map.add(json_key_value.getString("contentType"));
        }
        else {
            list_for_map.add(json_key_value.getString("type"));
        }

        if ( json_key_value.has("parentPlace") ) {

            JSONObject content_parentPlace = json_key_value.getJSONObject( "parentPlace" );
            list_for_map.add(content_parentPlace.getString("name"));
            list_for_map.add(content_parentPlace.getString("type"));
        }
        else {
            list_for_map.add("Hive");
            list_for_map.add("Hive");
        }

        if ( json_key_value.has("viewCount") ) {
            list_for_map.add(String.valueOf(json_key_value.get("viewCount")));
        }

        if (json_key_value.has("contentID")){
            list_for_map.add(json_key_value.getString("contentID" ));
        }

        if (json_key_value.has("binaryURL")){
            binary_map.put(json_key_value.getString("subject"),json_key_value.getString("binaryURL"));
        }

        content_name_size.put( json_key_value.getString("subject" ), list_for_map);
    }

    //Writing report of deleted items in to a file
    void cache(String fileName,Map map) throws IOException {
        getPlacesFromTextFile();

        for ( Object k : map.keySet() ) {
            if ( map_from_text_file.containsValue( map.get(k) ) ){

            }
            else {
//                try {
//                    FileWriter fileWriter = new FileWriter( fileName,true );
//                    fileWriter.write(( k + "<-::->" + map.get( k ) ) + "\n" );
//
//                    fileWriter.flush();
//                    fileWriter.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                try (FileWriter fileWriter = new FileWriter( fileName,true )){
                    fileWriter.write(( k + "<-::->" + map.get( k ) ) + "\n" );
                    fileWriter.flush();
                }
            }

        }//End for loop
    }

    private void getPlacesFromTextFile() throws IOException {
        String line;
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(TempFiles.getFile_name_id()));
//
//            while ( (line = reader.readLine() ) != null ) {
//                String[] parts = line.split("<-::->", 2 );
//                if ( parts.length >= 2 ) {
//                    String key = parts[0];
//                    String value = parts[1];
//                    map_from_text_file.putIfAbsent(key,value);
//                } else {
//                    System.out.println( "ignoring line: " + line);
//                }
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try (BufferedReader reader = new BufferedReader(new FileReader(TempFiles.getFile_name_id()))){
            while ( (line = reader.readLine() ) != null ) {
                String[] parts = line.split("<-::->", 2 );
                if ( parts.length >= 2 ) {
                    String key = parts[0];
                    String value = parts[1];
                    map_from_text_file.putIfAbsent(key,value);
                } else {
                    System.out.println( "ignoring line: " + line);
                }
            }
        }
    }

    public Map < String, List > getContent_name_size(){
        return content_name_size;
    }
    public Map<String, String> getParentPlaceName(){
        return parentPlaceName;
    }
    protected List < JSONObject > getJsonObjects(){
        return jsonObjects;
    }

    Map <String,String> getPlaceNameId(){
        return place_name_id;
    }

    public static void setUsername(String username) {
        Get.username = username;
    }
    public static void setPassword(String password) {
        Get.password = password;
    }
    public static String getUser() {
        return user;
    }
    public int getResponse_code() {
        return response_code;
    }
    public Map<String,String> getBinary_map() {
        return binary_map;
    }
    public static List<String> getUserDetail() {
        return userDetail;
    }
    public static String getAvatarUrl() {
        return avatarUrl;
    }

}
