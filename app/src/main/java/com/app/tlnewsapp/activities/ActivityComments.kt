package com.app.tlnewsapp.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.R
import com.app.tlnewsapp.adapter.AdapterComments
import com.app.tlnewsapp.callbacks.CallbackComments
import com.app.tlnewsapp.models.Comments
import com.app.tlnewsapp.models.Value
import com.app.tlnewsapp.rests.ApiInterface
import com.app.tlnewsapp.rests.RestAdapter
import com.app.tlnewsapp.utils.NetworkCheck
import com.balysv.materialripple.MaterialRippleLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ActivityComments : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var adapterCategory: AdapterComments? = null
    private var callbackCall: Call<CallbackComments>? = null
    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private var nid: Long? = null
    private var commentsCount: Long? = null
    private lateinit var lytParent: RelativeLayout
    private lateinit var btnAddComment: FloatingActionButton
    private lateinit var myApplication: MyApplication
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        view = findViewById(android.R.id.content)

        myApplication = MyApplication.instance!!

        nid = intent.getLongExtra("nid", 0)
        commentsCount = intent.getLongExtra("count", 0)

        setupToolbar()

        lytParent = findViewById(R.id.lyt_parent)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_category)
        swipeRefreshLayout!!.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView!!.setHasFixedSize(true)

        staggeredGridLayoutManager = StaggeredGridLayoutManager(1, 1)
        recyclerView!!.layoutManager = staggeredGridLayoutManager

        //set data and list adapter
        adapterCategory = AdapterComments(this@ActivityComments, ArrayList())
        recyclerView!!.adapter = adapterCategory

        // on item list clicked
        adapterCategory!!.setOnItemClickListener (object: AdapterComments.OnItemClickListener{
            override fun onItemClick(view: View, obj: Comments, position: Int, context: Context) {
                if (myApplication.isLogin && myApplication.userId == obj.user_id) {

                    val layoutInflaterAndroid = LayoutInflater.from(context)
                    val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_edit, null)

                    val alert = AlertDialog.Builder(context)
                    alert.setView(mView)

                    val btn_edit = mView.findViewById<MaterialRippleLayout>(R.id.menu_edit)
                    val btn_delete = mView.findViewById<MaterialRippleLayout>(R.id.menu_delete)

                    val alertDialog = alert.create()

                    btn_edit.setOnClickListener {
                        alertDialog.dismiss()
                        val i = Intent(applicationContext, ActivityUpdateComment::class.java)
                        i.putExtra("id", obj.comment_id)
                        i.putExtra("date_time", obj.date_time)
                        i.putExtra("content", obj.content)
                        startActivity(i)
                    }
                    btn_delete.setOnClickListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(getString(R.string.confirm_delete_comment))
                        builder.setPositiveButton(getString(R.string.dialog_yes)) { dialog, which ->
                            val retrofit = Retrofit.Builder()
                                    .baseUrl(Config.ADMIN_PANEL_URL + "/")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                            val apiInterface = retrofit.create(ApiInterface::class.java)
                            val call = apiInterface.deleteComment(obj.comment_id)
                            call.enqueue(object : Callback<Value> {
                                override fun onResponse(call: Call<Value>, response: Response<Value>) {
                                    val value = response.body()!!.getValue()
                                    val message = response.body()!!.getMessage()
                                    if (value == "1") {
                                        Toast.makeText(this@ActivityComments, message, Toast.LENGTH_SHORT).show()
                                        requestActionOnResume()
                                    } else {
                                        Toast.makeText(this@ActivityComments, message, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Value>, t: Throwable) {
                                    t.printStackTrace()
                                    Toast.makeText(this@ActivityComments, "Network error!", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }

                        builder.setNegativeButton(getString(R.string.dialog_no), null)
                        val alert = builder.create()
                        alert.show()

                        alertDialog.dismiss()
                    }

                    alertDialog.show()

                } else if (myApplication.isLogin) {

                    val layoutInflaterAndroid = LayoutInflater.from(context)
                    val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_reply, null)

                    val alert = AlertDialog.Builder(context)
                    alert.setView(mView)

                    val btn_reply = mView.findViewById<MaterialRippleLayout>(R.id.menu_reply)

                    val alertDialog = alert.create()

                    btn_reply.setOnClickListener {
                        alertDialog.dismiss()

                        val intent = Intent(applicationContext, ActivityReplyComment::class.java)
                        intent.putExtra("user_id", myApplication.userId)
                        intent.putExtra("user_name", obj.name)
                        intent.putExtra("nid", nid!!)
                        startActivity(intent)
                    }
                    alertDialog.show()

                }
            }

        })

        // on swipe list
        swipeRefreshLayout!!.setOnRefreshListener {
            lytParent.visibility = View.VISIBLE
            adapterCategory!!.resetListData()
            requestAction()
        }

        requestAction()
        lytParent.visibility = View.VISIBLE

        btnAddComment = findViewById(R.id.btn_add_comment)
        btnAddComment.setOnClickListener {
            if (myApplication.isLogin) {
                //Toast.makeText(getApplicationContext(), "id : " + myApplication.getUserId() + nid, Toast.LENGTH_SHORT).show();
                val intent = Intent(applicationContext, ActivitySendComment::class.java)
                intent.putExtra("user_id", myApplication.userId)
                intent.putExtra("nid", nid!!)
                startActivity(intent)
            } else {
                val login = Intent(applicationContext, ActivityUserLogin::class.java)
                startActivity(login)

                Toast.makeText(applicationContext, resources.getString(R.string.login_required), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = resources.getString(R.string.title_comments)
        }
    }

    private fun displayApiResult(categories: List<Comments>) {

        swipeProgress(false)
        adapterCategory!!.setListData(categories)
        if (categories.size == 0) {
            showNoItemView(true)
        }
    }

    private fun requestCategoriesApi() {
        val apiInterface = RestAdapter.createAPI()
        callbackCall = apiInterface.getComments(nid)
        callbackCall!!.enqueue(object : Callback<CallbackComments> {
            override fun onResponse(call: Call<CallbackComments>, response: Response<CallbackComments>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    displayApiResult(resp.comments)
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<CallbackComments>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }

        })
    }

    private fun onFailRequest() {
        swipeProgress(false)
        if (NetworkCheck.isConnect(this@ActivityComments)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        showNoItemView(false)
        requestCategoriesApi()
    }

    private fun requestActionOnResume() {
        showFailedView(false, "")
        swipeProgress(false)
        showNoItemView(false)
        requestCategoriesApi()
    }

    public override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted) {
            callbackCall!!.cancel()
        }
    }

    private fun showFailedView(flag: Boolean, message: String) {
        val lyt_failed = findViewById<View>(R.id.lyt_failed_category)
        (findViewById<View>(R.id.failed_message) as TextView).text = message
        if (flag) {
            recyclerView!!.visibility = View.GONE
            lyt_failed.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lyt_failed.visibility = View.GONE
        }
        findViewById<View>(R.id.failed_retry).setOnClickListener { requestAction() }
    }

    private fun showNoItemView(show: Boolean) {
        val lyt_no_item = findViewById<View>(R.id.lyt_no_item_category)
        (findViewById<View>(R.id.txt_no_comment) as TextView).setText(R.string.msg_no_comment)
        if (show) {
            recyclerView!!.visibility = View.GONE
            lyt_no_item.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lyt_no_item.visibility = View.GONE
        }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipeRefreshLayout!!.isRefreshing = show
            return
        }
        swipeRefreshLayout!!.post { swipeRefreshLayout!!.isRefreshing = show }
    }

    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {

            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(menuItem)
        }
    }

    public override fun onResume() {
        super.onResume()
        requestActionOnResume()
        lytParent.visibility = View.VISIBLE
    }

    companion object {
        val EXTRA_OBJC = "key.EXTRA_OBJC"
    }

}
