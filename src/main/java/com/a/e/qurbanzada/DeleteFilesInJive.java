package com.a.e.qurbanzada;

import com.sun.jersey.core.util.Base64;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by aqn3130 on 10/06/2017.
 * This is to delete files before new sharepoint reports gets published
 */

public class DeleteFilesInJive {

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConnDel;
    private OutputStream outputStream;
    private PrintWriter writer;
    private int status;
    private static String username = null;
    private static String password = null;

    public static void setUsername(String username) {
        DeleteFilesInJive.username = username;
    }

    public static void setPassword(String password) {
        DeleteFilesInJive.password = password;
    }

    public int getStatus(){
        return status;
    }
    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    final String CHARSET = "UTF-8";

    public DeleteFilesInJive(String reqUrl, int value, String data) throws IOException
    {
        // creates a unique boundary based on time stamp
        boundary = "SNIP";
        URL url = new URL(reqUrl);
        httpConnDel = (HttpURLConnection) url.openConnection();

        String pass = username + ":" + password;
        String auth = "Basic " + new String(new Base64().encode(pass.getBytes()));
        httpConnDel.addRequestProperty("Authorization", auth);

        if(value == 100){
            httpConnDel.setRequestMethod("DELETE");
        }
        if(value == 101){
            httpConnDel.setDoOutput(true); // indicates POST method
            httpConnDel.setRequestMethod("PUT");
            httpConnDel.setRequestProperty("Content-Type", "application/json; charset=utf8");
            OutputStreamWriter out = new OutputStreamWriter(httpConnDel.getOutputStream());
            out.write(data);
            out.close();
        }
        status=httpConnDel.getResponseCode();
    }
    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.

     */
    //* @throws IOException

    public String finish() throws IOException
    {

        String response = null;

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
//        status = httpConnDel.getResponseCode();
        InputStream is = null;
        try {
            is = httpConnDel.getInputStream();
        } catch (IOException e) {
            is = httpConnDel.getErrorStream();
        }
        // DEBUG
        Map<String, List<String>> hf = httpConnDel.getHeaderFields();
        for (String key : hf.keySet()) {
            //System.out.println(key + ": " + httpConnDel.getHeaderField(key));
        }

        String isGZIP = httpConnDel.getHeaderField("Content-Encoding");
        if ( "gzip".equalsIgnoreCase(isGZIP) ) {
            is = new GZIPInputStream(is);
        }

        int len = 1024 * 64;
        int offset = 0;
        int rlen = -1;
        byte[] b = new byte[len];
        while ((rlen = is.read(b, offset, len)) >= 0) {
            offset = offset + rlen;
            len = len - rlen;
        }
        is.close();
        response = new String(b, "UTF-8");
        // short vs. clean way to get response
        //byte[] b2 = new byte[offset];
        //System.arraycopy(b, 0, b2, 0, offset);
        //response = new String(b2, "UTF-8");

        httpConnDel.disconnect();
        return response;
    }
    public void addJSONData(String value)
    {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Type: application/json").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
}
