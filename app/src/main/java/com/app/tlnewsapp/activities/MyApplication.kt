package com.app.tlnewsapp.activities

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

import com.google.firebase.auth.FirebaseUser

import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : Application() {
    var preferences: SharedPreferences? = null
    internal var activity: Activity? = null
    var prefName = "news"
    private val user: FirebaseUser? = null

    val isLogin: Boolean
        get() {
            preferences = this.getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getBoolean(
                        "IsLoggedIn", false)
            } else false
        }

    val userId: String?
        get() {
            preferences = this.getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "user_id", "")
            } else ""
        }

    val userName: String?
        get() {
            preferences = this.getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "user_name", "")
            } else ""
        }

    val userEmail: String?
        get() {
            preferences = this.getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "email", "")
            } else ""
        }

    val type: String?
        get() {
            preferences = this.getSharedPreferences(prefName, 0)
            return if (preferences != null) {
                preferences!!.getString(
                        "type", "")
            } else ""
        }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Realm.init(applicationContext)
        // init realm database
        val realmConfiguration = RealmConfiguration.Builder()
                .name("news.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(realmConfiguration)

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    fun saveIsLogin(flag: Boolean) {
        preferences = this.getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putBoolean("IsLoggedIn", flag)
        editor.apply()
    }

    fun getUser(): User {
        preferences = this.getSharedPreferences(prefName, 0)
        val user=User()
        if (preferences != null) {
            user.email = preferences!!.getString("sm_email","")!!
            user.name = preferences!!.getString("sm_name","")!!
            user.imageUrl = preferences!!.getString("sm_image_url","")!!
        }
        return user
    }

    fun setUser(user: FirebaseUser?) {
        preferences = this.getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putString("sm_name", user?.displayName)
        editor.putString("sm_image_url", user?.photoUrl.toString())
        editor.putString("sm_email", user?.email)
        editor.apply()
    }

    fun saveLogin(user_id: String, user_name: String, email: String) {
        preferences = this.getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putString("user_id", user_id)
        editor.putString("user_name", user_name)
        editor.putString("email", email)
        editor.apply()
    }

    fun saveType(type: String) {
        preferences = this.getSharedPreferences(prefName, 0)
        val editor = preferences!!.edit()
        editor.putString("type", type)
        editor.apply()
    }

    data class User(
            var name:String="",
            var email:String="",
            var imageUrl:String=""
    )

    companion object {
        @get:Synchronized
        var instance: MyApplication? = null
    }
}
