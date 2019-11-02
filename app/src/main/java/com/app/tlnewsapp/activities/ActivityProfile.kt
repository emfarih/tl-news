package com.app.tlnewsapp.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast

import com.app.tlnewsapp.R
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.utils.ApiConnector
import com.app.tlnewsapp.utils.Constant
import com.app.tlnewsapp.utils.NetworkCheck
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso

import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.util.ArrayList

class ActivityProfile : AppCompatActivity() {

    private lateinit var txt_email: EditText
    private lateinit var txt_name: EditText
    private lateinit var txt_pwd: EditText
    private var myApplication: MyApplication? = null
    private var btn_update: Button? = null
    private lateinit var strMessage: String
    private lateinit var lyt_profile: RelativeLayout
    //ProgressBar progressBar;
    private var img_profile: ImageView? = null
    private var img_change: ImageView? = null
    private var str_user_id: String? = null
    private lateinit var progressDialog: ProgressDialog
    private var str_name: String? = null
    private var str_email: String? = null
    private var str_image: String? = null
    private var str_password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setTitle(R.string.title_menu_profile)
        }

        val intent = intent
        str_name = intent.getStringExtra("name")
        str_email = intent.getStringExtra("email")
        str_image = intent.getStringExtra("user_image")
        str_password = intent.getStringExtra("password")

        progressDialog = ProgressDialog(this@ActivityProfile)
        progressDialog.setTitle(resources.getString(R.string.title_please_wait))
        progressDialog.setMessage(resources.getString(R.string.logout_process))
        progressDialog.setCancelable(false)

        myApplication = MyApplication.instance

        img_profile = findViewById(R.id.profile_image)
        img_change = findViewById(R.id.change_image)

        lyt_profile = findViewById(R.id.lyt_profile)
        //progressBar = (ProgressBar) findViewById(R.id.progressBar);

        txt_email = findViewById(R.id.edt_email)
        txt_name = findViewById(R.id.edt_user)
        txt_pwd = findViewById(R.id.edt_password)

        txt_name.setText(str_name)
        txt_email.setText(str_email)
        txt_pwd.setText(str_password)

        if (NetworkCheck.isNetworkAvailable(this@ActivityProfile)) {
            //new MyTask().execute(Constant.PROFILE_URL + myApplication.getUserId());
            getUserImage().execute(ApiConnector())
        } else {
            showToast("No Network Connection!!")
            SetMessage()
        }

        img_change!!.setOnClickListener { requestStoragePermission() }

    }

    @TargetApi(16)
    private fun requestStoragePermission() {
        Dexter.withActivity(this@ActivityProfile)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent()
                            intent.type = "image/*"
                            intent.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { error -> Toast.makeText(applicationContext, "Error occurred! $error", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@ActivityProfile)
        builder.setTitle("Need Permissions")
        builder.setMessage(R.string.permission_upload)
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.save -> {
                if (NetworkCheck.isNetworkAvailable(this@ActivityProfile)) {
                    MyTaskUp().execute(Constant.PROFILE_UPDATE_URL + myApplication!!.userId +
                            "&name=" + txt_name.text.toString().replace(" ", "%20") +
                            "&email=" + txt_email.text.toString() +
                            "&password=" + txt_pwd.text.toString()
                    )

                } else {
                    showToast("No Network Connection!!")
                    SetMessage()
                }
                return true
            }

            else -> return super.onOptionsItemSelected(menuItem)
        }
    }

    fun setAdapter() {

    }

    fun showToast(msg: String) {
        Toast.makeText(this@ActivityProfile, msg, Toast.LENGTH_LONG).show()
    }

    private inner class MyTaskUp : AsyncTask<String, Void, String>() {
        private var pDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): String? {
            return NetworkCheck.getJSONString(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (null == result || result.length == 0) {
                showToast("No Data Found!!!")


            } else {

                try {
                    val mainJson = JSONObject(result)
                    val jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME)
                    var objJson: JSONObject? = null
                    for (i in 0 until jsonArray.length()) {
                        objJson = jsonArray.getJSONObject(i)
                        strMessage = objJson!!.getString(Constant.MSG)
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                setResult()
            }

        }
    }

    fun setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {

        } else {
            progressDialog = ProgressDialog(this@ActivityProfile)
            progressDialog.setTitle(R.string.updating_profile)
            progressDialog.setMessage(resources.getString(R.string.waiting_message))
            progressDialog.setCancelable(false)
            progressDialog.show()
            Handler().postDelayed({
                progressDialog.dismiss()
                val builder = AlertDialog.Builder(this@ActivityProfile)
                builder.setMessage("Update Successfully")
                builder.setPositiveButton("Ok") { dialogInterface, i -> finish() }
                builder.setCancelable(false)
                builder.show()
            }, Constant.DELAY_PROGRESS_DIALOG.toLong())
        }
    }

    private fun SetMessage(): AlertDialog {

        var alert = AlertDialog.Builder(this@ActivityProfile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = AlertDialog.Builder(this@ActivityProfile, android.R.style.Theme_Material_Light_Dialog_Alert)
        } else {
            alert = AlertDialog.Builder(this@ActivityProfile)
        }
        alert.setTitle("No Network Connection!!")
        alert.setIcon(R.mipmap.ic_launcher)
        alert.setMessage("Please connect to working Internet connection")

        alert.setPositiveButton("OK") { dialog, whichButton -> finish() }
        alert.show()
        return alert.create()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri = data!!.data
                if (Build.VERSION.SDK_INT < 19) {
                    val selectedImagePath = getPath(selectedImageUri)
                    val bitmap = BitmapFactory.decodeFile(selectedImagePath)
                    SetImage(bitmap)
                } else {
                    val parcelFileDescriptor: ParcelFileDescriptor?
                    try {
                        parcelFileDescriptor = contentResolver.openFileDescriptor(selectedImageUri!!, "r")
                        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                        parcelFileDescriptor.close()
                        SetImage(image)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun SetImage(image: Bitmap) {
        this.img_profile!!.setImageBitmap(image)

        // upload
        val imageData = encodeTobase64(image)
        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("image", imageData))
        params.add(BasicNameValuePair("user_id", myApplication!!.userId))

        object : AsyncTask<ApiConnector, Long, Boolean>() {

            override fun onPreExecute() {
                super.onPreExecute()
                progressDialog = ProgressDialog(this@ActivityProfile)
                progressDialog.setTitle(R.string.updating_profile)
                progressDialog.setMessage(resources.getString(R.string.waiting_message))
                progressDialog.setCancelable(false)
                progressDialog.show()
            }

            override fun doInBackground(vararg apiConnectors: ApiConnector): Boolean? {
                return apiConnectors[0].uploadImageToserver(params)
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                Handler().postDelayed({ progressDialog.dismiss() }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }

        }.execute(ApiConnector())

    }

    fun getPath(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        }
        return uri.path
    }

    private inner class getUserImage : AsyncTask<ApiConnector, Long, JSONArray>() {
        override fun doInBackground(vararg params: ApiConnector): JSONArray? {
            return params[0].GetCustomerDetails(myApplication!!.userId)
        }

        override fun onPostExecute(jsonArray: JSONArray) {

            try {

                var objJson: JSONObject? = null
                objJson = jsonArray.getJSONObject(0)
                val user_id = objJson!!.getString("id")
                val user_image = objJson.getString("imageName")

                if (user_image == "") {
                    img_profile!!.setImageResource(R.drawable.ic_user_account_white)
                } else {

                    Picasso.with(this@ActivityProfile)
                            .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + user_image.replace(" ", "%20"))
                            .resize(300, 300)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_account_white)
                            .into(img_profile)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        private val SELECT_PICTURE = 1

        fun encodeTobase64(image: Bitmap?): String? {
            System.gc()

            if (image == null) return null

            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val b = baos.toByteArray()

            return Base64.encodeToString(b, Base64.DEFAULT)
        }
    }

}
