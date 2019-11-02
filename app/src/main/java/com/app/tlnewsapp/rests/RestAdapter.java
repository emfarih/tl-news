package com.app.tlnewsapp.rests;

import com.app.tlnewsapp.Config;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.realm.BuildConfig;
import io.realm.RealmObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestAdapter {

    public static ApiInterface createAPI() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(Level.BODY);

        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass().equals(RealmObject.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).setLenient().create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        if(BuildConfig.DEBUG){
            builder.addInterceptor(logging);
        }
        builder.cache(null);
        OkHttpClient okHttpClient = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        return retrofit.create(ApiInterface.class);
    }

}
