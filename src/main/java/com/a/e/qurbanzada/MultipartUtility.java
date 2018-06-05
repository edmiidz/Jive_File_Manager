package com.a.e.qurbanzada;

import com.sun.jersey.core.util.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


/**
 * Created by aqn3130 on 02/06/2017.
 */
public class MultipartUtility {

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;
    private static String username = null;

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    private static String password = null;

    public static void setUsername(String userName) {
        username = userName;
    }
    public static void setPassword(String passWord) {
        password = passWord;
    }

    MultipartUtility( String requestURL , int value ) throws IOException {
        // creates a unique boundary based on time stamp
        boundary = "SNIP";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();

        String iPass = username + ":" + password;
        String iAuth = "Basic " + new String(new Base64().encode(iPass.getBytes()));
        httpConn.addRequestProperty("Authorization", iAuth);

        //httpConn.addRequestProperty("Authorization", "Bearer 63vpca09btfgt1lu9isn0i6b5iedwmxsd2de8om6.t");
        //httpConn.addRequestProperty("X-Jive-Run-As", "email Stephan.Schlichtmann@springer.com");

        if ( value == 101 ) {
            httpConn.setDoOutput( true );
            httpConn.setRequestMethod( "POST" );
        }
        if ( value == 100 ) {
            httpConn.setDoOutput( true );
            httpConn.setRequestMethod( "PUT" );
        }

        httpConn.setDoInput(true);
        httpConn.setRequestProperty( "Content-Type" , "multipart/form-data; boundary=" + boundary );
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter( new OutputStreamWriter( outputStream , "UTF-8"),true );
    }

    public HttpURLConnection getHttpConn() {
        return httpConn;
    }

    /**
     * Adds a upload file section to the request

     * @param uploadFile a File to be uploaded

     */
    //* @param fieldName name attribute in <input type="file" name="..." />
    //* @throws IOException

    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append( LINE_FEED );
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append( LINE_FEED );
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append( LINE_FEED );
        writer.append("Content-Transfer-Encoding: binary").append( LINE_FEED );
        writer.append( LINE_FEED );
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ( ( bytesRead = inputStream.read( buffer ) ) != -1 ) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.

     */
    //* @throws IOException

    public String finish() throws IOException {
        String response;

        writer.append( LINE_FEED ).flush();
        writer.append("--").append(boundary).append("--").append( LINE_FEED );
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        InputStream is;
        try {
            is = httpConn.getInputStream();
        } catch (IOException e) {
            is = httpConn.getErrorStream();
        }
        // DEBUG
        Map<String, List<String>> hf = httpConn.getHeaderFields();

        String isGZIP = httpConn.getHeaderField("Content-Encoding");
        if ( "gzip".equalsIgnoreCase(isGZIP) ) {
            is = new GZIPInputStream(is);
        }

        int len = 1024 * 64;
        int offset = 0;
        int rlen;
        byte[] b = new byte[len];
        while ( ( rlen = is.read ( b , offset , len ) ) >= 0 ) {
            offset = offset + rlen;
            len = len - rlen;
        }
        is.close();
        response = new String(b, "UTF-8");
        httpConn.disconnect();
        return response;
    }
    public void addJSONData( String value ) {
        writer.append("--").append(boundary).append( LINE_FEED );
        writer.append( "Content-Type: application/json" ).append( LINE_FEED );
        writer.append( LINE_FEED );
        writer.append(value).append( LINE_FEED );
        writer.flush();
    }
}
