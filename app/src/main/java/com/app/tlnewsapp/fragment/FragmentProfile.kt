package com.app.tlnewsapp.fragment

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.app.tlnewsapp.R
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.activities.ActivityPrivacyPolicy
import com.app.tlnewsapp.activities.ActivityProfile
import com.app.tlnewsapp.activities.ActivityUserLogin
import com.app.tlnewsapp.activities.ActivityUserRegister
import com.app.tlnewsapp.activities.MyApplication
import com.app.tlnewsapp.adapter.AdapterAbout
import com.app.tlnewsapp.utils.ApiConnector
import com.app.tlnewsapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

import org.json.JSONArray
import org.json.JSONObject

import java.util.ArrayList

class FragmentProfile : Fragment() {

    private var root_view: View? = null
    private var parent_view: View? = null
    private lateinit var myApplication: MyApplication
    private lateinit var lyt_is_login: RelativeLayout
    private lateinit var lyt_login_register: RelativeLayout
    private lateinit var txt_edit: TextView
    private lateinit var txt_login: TextView
    private lateinit var txt_logout: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var txt_register: TextView
    private lateinit var txt_username: TextView
    private lateinit var txt_email: TextView
    private lateinit var img_profile: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterAbout: AdapterAbout
    private lateinit var lyt_root: LinearLayout

    private val dataInformation: List<Data>
        get() {

            val data = ArrayList<Data>()

            data.add(Data(
                    R.drawable.ic_drawer_privacy,
                    resources.getString(R.string.title_about_privacy),
                    resources.getString(R.string.sub_title_about_privacy)
            ))

            data.add(Data(
                    R.drawable.ic_drawer_rate,
                    resources.getString(R.string.title_about_rate),
                    resources.getString(R.string.sub_title_about_rate)
            ))

            data.add(Data(
                    R.drawable.ic_drawer_more,
                    resources.getString(R.string.title_about_more),
                    resources.getString(R.string.sub_title_about_more)
            ))

            data.add(Data(
                    R.drawable.ic_drawer_info,
                    resources.getString(R.string.title_about_info),
                    ""
            ))

            return data
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root_view = inflater.inflate(R.layout.fragment_profile, null)
        parent_view = activity!!.findViewById(R.id.main_content)
        lyt_root = root_view!!.findViewById(R.id.root_layout)

        myApplication = MyApplication.instance!!

        lyt_is_login = root_view!!.findViewById(R.id.lyt_is_login)
        lyt_login_register = root_view!!.findViewById(R.id.lyt_login_register)
        txt_login = root_view!!.findViewById(R.id.btn_login)
        txt_logout = root_view!!.findViewById(R.id.txt_logout)
        txt_edit = root_view!!.findViewById(R.id.btn_logout)
        txt_register = root_view!!.findViewById(R.id.txt_register)
        txt_username = root_view!!.findViewById(R.id.txt_username)
        txt_email = root_view!!.findViewById(R.id.txt_email)
        img_profile = root_view!!.findViewById(R.id.img_profile)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle(resources.getString(R.string.title_please_wait))
        progressDialog.setMessage(resources.getString(R.string.logout_process))
        progressDialog.setCancelable(false)

        recyclerView = root_view!!.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapterAbout = AdapterAbout(dataInformation, activity!!)
        recyclerView.adapter = adapterAbout

        adapterAbout.setOnItemClickListener(object : AdapterAbout.OnItemClickListener {
            override fun onItemClick(v: View, obj: Data, position: Int) {
                if (position == 0) {
                    startActivity(Intent(activity, ActivityPrivacyPolicy::class.java))
                }
                if (position == 1) {
                    val appName = activity!!.packageName
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$appName")))
                    }

                } else if (position == 2) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))))
                } else if (position == 3) {
                    aboutDialog()
                }
            }
        })

        return root_view
    }

    override fun onResume() {

        if (myApplication.isLogin) {
            lyt_is_login.visibility = View.VISIBLE
            lyt_login_register.visibility = View.GONE
            GetUserImage().execute(ApiConnector())

            val user = myApplication.getUser()
            if(user.name!=""){
                txt_username.text = user.name
                txt_email.text = user.email

                if (user.imageUrl == "") {
                    img_profile.setImageResource(R.drawable.ic_user_account_white)
                } else {
                    Picasso.with(activity)
                            .load(user.imageUrl)
                            .resize(300, 300)
                            .centerCrop()

                            .into(img_profile)
                }
            }

            txt_logout.visibility = View.VISIBLE
            txt_logout.setOnClickListener { logoutDialog() }

        } else {
            lyt_is_login.visibility = View.GONE
            lyt_login_register.visibility = View.VISIBLE
            txt_login.setOnClickListener { startActivity(Intent(activity, ActivityUserLogin::class.java)) }

            txt_register.setOnClickListener { startActivity(Intent(activity, ActivityUserRegister::class.java)) }
            txt_logout.visibility = View.GONE
        }

        super.onResume()
    }

    fun logoutDialog() {

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(R.string.logout_title)
        builder.setMessage(R.string.logout_message)
        builder.setPositiveButton(R.string.dialog_yes) { di, i ->
            MyApplication.instance?.saveIsLogin(false)
            FirebaseAuth.getInstance().signOut()
            progressDialog.show()
            Handler().postDelayed({
                progressDialog.dismiss()
                val builder = AlertDialog.Builder(activity!!)
                builder.setMessage(R.string.logout_success)
                builder.setPositiveButton(R.string.dialog_ok) { dialogInterface, i -> refreshFragment() }
                builder.setCancelable(false)
                builder.show()
            }, Constant.DELAY_PROGRESS_DIALOG.toLong())
        }
        builder.setNegativeButton(R.string.dialog_cancel, null)
        builder.show()

    }

    fun refreshFragment() {
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.detach(this).attach(this).commit()
    }

    private inner class GetUserImage : AsyncTask<ApiConnector, Long, JSONArray?>() {
        override fun doInBackground(vararg params: ApiConnector): JSONArray? {
            return params[0].GetCustomerDetails(myApplication.userId)
        }

        override fun onPostExecute(jsonArray: JSONArray?) {

            try {
                val objJson: JSONObject? = jsonArray?.getJSONObject(0)
                val user_id = objJson!!.getString("id")
                val name = objJson.getString("name")
                val email = objJson.getString("email")
                val user_image = objJson.getString("imageName")
                val password = objJson.getString("password")

                txt_username.text = name
                txt_email.text = email

                if (user_image == "") {
                    img_profile.setImageResource(R.drawable.ic_user_account_white)
                } else {
                    Picasso.with(activity)
                            .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + user_image.replace(" ", "%20"))
                            .resize(300, 300)
                            .centerCrop()

                            .into(img_profile)
                }

                txt_edit.setOnClickListener {
                    val intent = Intent(activity, ActivityProfile::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("email", email)
                    intent.putExtra("user_image", user_image)
                    intent.putExtra("password", password)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    inner class Data(val image: Int, val title: String, val sub_title: String)

    fun aboutDialog() {
        val layoutInflaterAndroid = LayoutInflater.from(activity)
        val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null)

        val alert = AlertDialog.Builder(activity!!)
        alert.setView(mView)
        alert.setCancelable(false)
        alert.setPositiveButton(R.string.dialog_ok) { dialog, which -> dialog.dismiss() }
        alert.show()
    }

}