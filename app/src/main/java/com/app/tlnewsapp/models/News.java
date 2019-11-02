package com.app.tlnewsapp.models;

import com.app.tlnewsapp.realm.table.NewsRealm;

import java.io.Serializable;

public class News implements Serializable {

    public long nid = -1;
    public String news_title = "";
    public String category_name = "";
    public String news_date = "";
    public String news_image = "";
    public String news_description = "";
    public String content_type = "";
    public String video_url = "";
    public String video_id = "";
    public long comments_count = -1;

    public NewsRealm getObjectRealm() {
        NewsRealm p = new NewsRealm();
        p.nid = nid;
        p.news_title = news_title;
        p.category_name = category_name;
        p.news_date = news_date;
        p.news_image = news_image;
        p.news_description = news_description;
        p.content_type = content_type;
        p.video_url = video_url;
        p.video_id = video_id;
        p.comments_count = comments_count;

        return p;
    }

    public News() {
    }

    public boolean isDraft(){
        return !(news_description != null && !news_description.trim().equals(""));
    }

}
