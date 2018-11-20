package com.zhiyong;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws IOException {
        // Get text of article from sina.com.cn.
        Document doc = Jsoup.connect("https://news.sina.com.cn/c/2018-11-19/doc-ihnyuqhi3254063.shtml").get();
        Element textElement = doc.getElementById("artibody");
        if (textElement == null) {
            textElement = doc.getElementById("article");
        }
        if (textElement == null) {
            // todo: Log error.
        }

        String text = textElement.text();


        // Segmentation and deduplication.
        JiebaSegmenter segmenter = new JiebaSegmenter();
        Set<String> segments = new HashSet<>(segmenter.sentenceProcess(text));

        // Filter Chinese strings.
        Set<String> filtered = segments.stream().filter(x -> Character.UnicodeScript.of(x.charAt(0)) == Character.UnicodeScript.HAN).collect(Collectors.toSet());
        for (String segment : filtered) {
            System.out.print(segment);
            System.out.println(Character.UnicodeScript.of(segment.charAt(0)) == Character.UnicodeScript.HAN);
        }

        // Dictionary lookup and store to DB.
        
    }
}
