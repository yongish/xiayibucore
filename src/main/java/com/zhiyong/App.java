package com.zhiyong;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws IOException {
        // Get text of article from sina.com.cn.
//        Document doc = Jsoup.connect("https://news.sina.com.cn/c/2018-11-19/doc-ihnyuqhi3254063.shtml").get();
//        Element textElement = doc.getElementById("artibody");
//        if (textElement == null) {
//            textElement = doc.getElementById("article");
//        }
//        if (textElement == null) {
//            // todo: Log error.
//        }
//
//        String text = textElement.text();
//
//
//        // Segmentation and deduplication.
//        JiebaSegmenter segmenter = new JiebaSegmenter();
//        Set<String> segments = new HashSet<>(segmenter.sentenceProcess(text));
//
//        // Filter Chinese strings.
//        Set<String> filtered = segments.stream().filter(x -> Character.UnicodeScript.of(x.charAt(0)) == Character.UnicodeScript.HAN).collect(Collectors.toSet());
//        for (String segment : filtered) {
//            System.out.print(segment);
//            System.out.println(Character.UnicodeScript.of(segment.charAt(0)) == Character.UnicodeScript.HAN);
//        }

        // Dictionary lookup and store to DB.
        final String ZDICT_BASE_URL = "http://www.zdic.net";
        String urlParameters  = "q=%E4%BC%B8%E6%89%8B%E4%B8%8D%E8%A7%81%E4%BA%94%E6%8C%87";
        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int    postDataLength = postData.length;
        String request        = ZDICT_BASE_URL + "/sousuo/";
        URL    url            = new URL( request );
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        conn.setUseCaches( false );
        conn.getOutputStream().write(postData);

        int status = conn.getResponseCode();

        boolean redirect = false;
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }

        System.out.println("Response Code ... " + status);

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");

            // get the cookie if need, for login
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connnection again
            conn = (HttpURLConnection) new URL(ZDICT_BASE_URL + newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            System.out.println("Redirect to URL : " + newUrl);

        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer html = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            html.append(inputLine);
        }
        in.close();

        System.out.println("URL Content... \n" + html.toString());
        System.out.println("Done");



    }
}
