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
    private static final String BAIDU_DICT_URL_BASE = "https://dict.baidu.com";

    class Word {
        String pinyin;
        String chineseExplain;
        String englishExplain;
        String baikePreview;
    }

    /**
     * Lookup Baidu dictionary for a word.
     *
     * Should gather the lookups in a set instead.
     *
     * @param word
     */
    private void dictLookup(String word) throws IOException {
        System.out.println();
        System.out.println(word);

        final String initialUrl = BAIDU_DICT_URL_BASE + "/s?wd=" + word;
        Document dictDoc = Jsoup.connect(initialUrl).get();

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
            if (hrefLength - word.length() > 1) {
                System.out.println("GONNA SPLIT");
                Set<String> splitWords = word.chars().mapToObj(c -> (char) c).map(String::valueOf).collect(Collectors.toSet());
                splitWords.stream().forEach(x -> {
                    try {
                        dictLookup(x);
                    } catch (IOException e) {
                        System.exit(1);
                    }
                });
                return;
            }

            String url = BAIDU_DICT_URL_BASE + href;
            System.out.println(url);
            dictDoc = Jsoup.connect(url).get();
            pinyinWrapper = dictDoc.getElementById("pinyin");
        }
        if (pinyinWrapper == null) {
            System.out.println("NOT FOUND. GONNA SPLIT");
            Set<String> splitWords = word.chars().mapToObj(c -> (char) c).map(String::valueOf).collect(Collectors.toSet());
            splitWords.forEach(this::dictLookupHelper);
            return;
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
                System.out.println("Link to Baike article:" + initialUrl);
                System.out.println(baikeWrapper.selectFirst("p").text());
            }
            return;
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

    private void dictLookupHelper(String word) {
        try {
            dictLookup(word);
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private void getWords() throws IOException {
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

        // Filter Chinese strings then do dictionary lookup.
        segments.stream().filter(x -> Character.UnicodeScript.of(x.charAt(0)) ==
                Character.UnicodeScript.HAN)
                .collect(Collectors.toSet())
                .forEach(this::dictLookupHelper);
    }

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.getWords();
    }
}
