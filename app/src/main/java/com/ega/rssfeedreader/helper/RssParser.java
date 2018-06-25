package com.ega.rssfeedreader.helper;

import com.ega.rssfeedreader.model.RssItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssParser {
    private String xml;

    public RssParser(String xml){
        this.xml = xml;
    }

    public ArrayList<RssItem> getList() throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(false);
        XmlPullParser xmlPullParser = factory.newPullParser();

        xmlPullParser.setInput(new StringReader(xml));
        boolean insideItem = false;
        int eventType = xmlPullParser.getEventType();

        ArrayList<RssItem> listArticle = new ArrayList<RssItem>();
        RssItem article = new RssItem();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xmlPullParser.getName().equalsIgnoreCase("item")) {
                    insideItem = true;

                } else if (xmlPullParser.getName().equalsIgnoreCase("title")) {
                    if (insideItem) {
                        String title = xmlPullParser.nextText();
                        article.setTitle(title);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("link")) {
                    if (insideItem) {
                        String link = xmlPullParser.nextText();
                        article.setLink(link);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("dc:creator")) {
                    if (insideItem) {
                        String author = xmlPullParser.nextText();
                        article.setAuthor(author);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("category")) {
                    if (insideItem) {
                        String category = xmlPullParser.nextText();
                        article.addCategory(category);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("media:thumbnail")) {
                    if (insideItem) {
                        String img = xmlPullParser.getAttributeValue(null, "url");
                        article.setImage(img);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("description")) {
                    if (insideItem) {
                        String description = xmlPullParser.nextText();
                        if (article.getImage() == null) {
                            article.setImage(getImageUrl(description));
                        }
                        article.setDescription(description);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("content:encoded")) {
                    if (insideItem) {
                        String content = xmlPullParser.nextText();
                        if (article.getImage() == null) {
                            article.setImage(getImageUrl(content));
                        }
                        article.setContent(content);
                    }

                } else if (xmlPullParser.getName().equalsIgnoreCase("pubDate")) {
                    Date pubDate = new Date(xmlPullParser.nextText());
                    article.setPubDate(pubDate);
                }

            } else if (eventType == XmlPullParser.END_TAG && xmlPullParser.getName().equalsIgnoreCase("item")) {
                insideItem = false;
                listArticle.add(article);
                article = new RssItem();
            }
            eventType = xmlPullParser.next();
        }

        return listArticle;
    }

    private String getImageUrl(String input) {

        String url = null;
        Pattern patternImg = Pattern.compile("(<img .*?>)");
        Matcher matcherImg = patternImg.matcher(input);
        if (matcherImg.find()) {
            String imgTag = matcherImg.group(1);
            Pattern patternLink = Pattern.compile("src\\s*=\\s*\"(.+?)\"");
            Matcher matcherLink = patternLink.matcher(imgTag);
            if (matcherLink.find()) {
                url = matcherLink.group(1);
            }
        }
        return url;
    }
}
