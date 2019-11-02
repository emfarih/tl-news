package com.app.tlnewsapp.models;

public class PushNotif {

    public long nid;
    public String news_title;
    public String category_name;
    public String news_date;
    public String news_image;
    public String news_description;
    public String content_type;
    public String video_url;
    public String video_id;
    public long comments_count;

    public long getNid() {
        return nid;
    }

    public String getNews_title() {
        return news_title;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getNews_date() {
        return news_date;
    }

    public String getNews_image() {
        return news_image;
    }

    public String getNews_description() {
        return news_description;
    }

    public String getContent_type() {
        return content_type;
    }

    public String getVideo_url() {
        return video_url;
    }

    public String getVideo_id() {
        return video_id;
    }

    public long getComments_count() {
        return comments_count;
    }

}
