package com.a.e.qurbanzada;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aqn3130 on 10/06/2017.
 */
public class Helper {
    private static String communityURL;
    String url = System.getProperty("url", "https://" + communityURL + "/api/core/v3/contents/");
    List li;
    private Map<String,List> contentMetaData_list;
    List<Integer> statusCode = new ArrayList<>();
    int DELETE = 100;
    int MOVE = 101;
    List<JSONObject> jsonObjects;
    private final int CONTENT_PEOPLE = 100;
    private final int CONTENT_PLACE = 101;

    private Map<String,String> binaryData_map;

    Helper(int selector, String plcID, List<String> filters) throws IOException, JSONException {

        String url_content = null;
        if (selector == CONTENT_PEOPLE){

            url_content = "https://" + communityURL + "/api/core/v3/contents?filter=author(https://" + communityURL + "/api/core/v3/people/" + plcID + ")&count=100&includeBlogs=true";
        }
        else if (selector == CONTENT_PLACE){
            url_content = "https://" + communityURL + "/api/core/v3/contents?filter=place(https://" + communityURL + "/api/core/v3/places/" + plcID + ")&count=100&includeBlogs=true";
        }

        Get get = new Get();
        li = get.get(url_content,filters);
        contentMetaData_list = get.getContent_name_size();
        jsonObjects = get.getJsonObjects();
        binaryData_map = get.getBinary_map();
    }
    public static void setCommunityURL(String communityURL) {
        Helper.communityURL = communityURL;
    }
    public Map<String, List> getList(){
        return contentMetaData_list;
    }
    public static String getCommunityURL(){
        return communityURL;
    }
    public Map<String,String> getBinaryData_map() {
        return binaryData_map;
    }

}
