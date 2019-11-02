package com.app.tlnewsapp.realm.table;

import com.app.tlnewsapp.models.Category;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CategoryRealm extends RealmObject {

    @PrimaryKey
    public long cid = -1;
    public String category_name = "";
    public String category_image = "";
    public long post_count = -1;

    public Category getOriginal() {
        Category c = new Category();
        c.cid = cid;
        c.category_name = category_name;
        c.category_image = category_image;
        c.post_count = post_count;
        return c;
    }

}
