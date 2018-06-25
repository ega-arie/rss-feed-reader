package com.ega.rssfeedreader.model;

public class RssSource {
    private String url;
    private String name;

    public RssSource(String name, String url){
        this.name = name;
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
