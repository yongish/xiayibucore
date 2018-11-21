package com.zhiyong;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    /**
     * Lookup Baidu dictionary for a word.
     * @param word
     */
    public static void dictLookup(String word) {

    }

    public static void main(String[] args) throws IOException {
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
        Set<String> filtered = segments.stream()
                .filter(x -> Character.UnicodeScript.of(x.charAt(0)) ==
                        Character.UnicodeScript.HAN)
                .collect(Collectors.toSet());


        for (String segment : filtered) {
            System.out.println();
            System.out.println(segment);

            // Dictionary lookup and store to DB.
            final String BAIDU_DICT_URL_BASE = "https://dict.baidu.com";
            final String INITIAL_URL = BAIDU_DICT_URL_BASE + "/s?wd=" + segment;
            Document dictDoc = Jsoup.connect(INITIAL_URL).get();

            // May be a multiple-choice page. If so, select 1st choice.
            Element pinyinWrapper = dictDoc.getElementById("pinyin");
            if (pinyinWrapper == null && !dictDoc.text().contains("百度汉语中没有收录")) {
                String href = dictDoc.getElementById("data-container").selectFirst("a").attr("href");
                // Regard 1st choice as irrelevant if it is >1 character longer than the segment.
                int start = href.indexOf("=");
                int end = href.indexOf("&");
                if (end == -1) {
                    end = href.length();
                }
                System.out.println(href);
                int hrefLength = href.substring(start + 1, end).length();
                System.out.println("LENGTH: " + hrefLength);

                // If length is more than 1 character greater, look up individual words.
                if (hrefLength - segment.length() > 1) {
//                    finalSet.addAll(segment.chars().mapToObj(c -> (char) c).map(String::valueOf).collect(Collectors.toSet()));
                    Set<String> words = segment.chars().mapToObj(c -> (char) c).map(String::valueOf).collect(Collectors.toSet());



                }



                String url = BAIDU_DICT_URL_BASE + href;
                System.out.println(url);
                dictDoc = Jsoup.connect(url).get();
                pinyinWrapper = dictDoc.getElementById("pinyin");
            }
            if (pinyinWrapper == null) {
                System.out.println("NOT FOUND.");
                continue;
            }

            // Pinyin
            String pinyin = pinyinWrapper.selectFirst("b").text().replace("[", "").replace("]", "").trim();
            System.out.println(pinyin);

            // Chinese explanation.
            Element basicWrapper = dictDoc.getElementById("basicmean-wrapper");
            if (basicWrapper == null) {
                Element baikeWrapper = dictDoc.getElementById("baike-wrapper");
                if (baikeWrapper == null) {
                    System.out.println("MEANING NOT FOUND.");
                } else {
                    System.out.println("Link to Baike article:" + INITIAL_URL);
                    System.out.println(baikeWrapper.selectFirst("p").text());
                }
                continue;
            }
            String chineseExplain = basicWrapper.child(1).text().trim();
            int start = chineseExplain.indexOf("]");
            if (start != -1) {
                chineseExplain = chineseExplain.substring(start + 1).trim();
            }
            System.out.println(chineseExplain);

            // English explanation.
            Element engWrapper = dictDoc.getElementById("fanyi-wrapper").child(1);
            String eng = engWrapper.text().trim();
            System.out.println(eng);
        }
    }
}
