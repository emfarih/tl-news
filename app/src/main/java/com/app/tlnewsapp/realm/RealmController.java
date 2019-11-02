package com.app.tlnewsapp.realm;

import android.app.Activity;
import android.app.Application;
import androidx.fragment.app.Fragment;

import com.app.tlnewsapp.models.News;
import com.app.tlnewsapp.realm.table.NewsRealm;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {
        if (instance == null) instance = new RealmController(fragment.getActivity().getApplication());
        return instance;
    }
    public static RealmController with(Activity activity) {
        if (instance == null) instance = new RealmController(activity.getApplication());
        return instance;
    }
    public static RealmController with(Application application) {
        if (instance == null) instance = new RealmController(application);
        return instance;
    }

    public static RealmController getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    //find all objects
    public List<News> getNews() {
        RealmResults<NewsRealm> realmResults = realm.where(NewsRealm.class).findAll();
        realmResults = realmResults.sort("added_date", Sort.DESCENDING);
        List<News> newList = new ArrayList<>();
        for (NewsRealm c : realmResults){
            newList.add(c.getOriginal());
        }
        return newList;
    }

    public News saveNews(News obj) {
        realm.beginTransaction();
        NewsRealm newObj = obj.getObjectRealm();
        newObj.added_date = System.currentTimeMillis();
        newObj = realm.copyToRealmOrUpdate(newObj);
        realm.commitTransaction();
        return newObj != null ? newObj.getOriginal() : null;
    }

    public News getNews(long nid) {
        NewsRealm postRealm = realm.where(NewsRealm.class).equalTo("nid", nid).findFirst();
        return postRealm != null ? postRealm.getOriginal() : null;
    }

    public void deleteNews(long nid) {
        realm.beginTransaction();
        realm.where(NewsRealm.class).equalTo("nid", nid).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    public int getNewsSize() {
        return realm.where(NewsRealm.class).findAll().size();
    }


}
