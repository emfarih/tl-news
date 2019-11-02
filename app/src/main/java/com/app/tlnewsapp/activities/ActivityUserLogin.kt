@file:Suppress("DEPRECATION")

package com.app.tlnewsapp.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.app.tlnewsapp.R
import com.app.tlnewsapp.utils.Constant
import com.app.tlnewsapp.utils.NetworkCheck
import com.app.tlnewsapp.utils.validation.Rule
import com.app.tlnewsapp.utils.validation.Validator
import com.app.tlnewsapp.utils.validation.annotation.Email
import com.app.tlnewsapp.utils.validation.annotation.Password
import com.app.tlnewsapp.utils.validation.annotation.Required
import com.app.tlnewsapp.utils.validation.annotation.TextRule
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.*
import org.json.JSONException
import org.json.JSONObject

@Suppress("ControlFlowWithEmptyBody", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ActivityUserLogin : AppCompatActivity(), Validator.ValidationListener {

    private lateinit var strEmail: String
    private lateinit var strPassword: String
    private lateinit var strMessage: String
    private lateinit var strName: String
    private lateinit var strPassengerId: String
    private lateinit var strImage: String
    @Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address")
    private lateinit var edtEmail: EditText

    @Required(order = 3)
    @Password(order = 4, message = "Enter a Valid Password")
    @TextRule(order = 5, minLength = 6, message = "Enter a Password Correctly")
    private lateinit var edtPassword: EditText
    private var validator: Validator? = null
    private lateinit var btnSingIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var myApp: MyApplication
    private lateinit var txtForgot: TextView

    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth
    private var credentialWantToMerge: AuthCredential? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        callbackManager = CallbackManager.Factory.create()

        setupToolbar()

        myApp = MyApplication.instance!!
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnSingIn = findViewById(R.id.btn_update)
        btnSignUp = findViewById(R.id.btn_create)
        txtForgot = findViewById(R.id.txt_forgot)

        btnSingIn.setOnClickListener {
            validator!!.validateAsync()
            myApp.saveType("normal")
        }

        txtForgot.setOnClickListener {
            val intent = Intent(this@ActivityUserLogin, ActivityForgotPassword::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(applicationContext, ActivityUserRegister::class.java))
            finish()
        }

        validator = Validator(this)
        validator!!.validationListener = this

        initFirebase()
        setupFacebookLogin()
        setupGoogleLogin()
    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_g_web_client_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val buttonGoogleLogin = findViewById<Button>(R.id.btn_google)
        buttonGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
    }

    private fun linkAccount() {
        val tag = "LINK"
        credentialWantToMerge?.let {
            auth.currentUser?.linkWithCredential(it)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "linkWithCredential:success")
                        Toast.makeText(baseContext, "Great!, now you can sign in from any providers",
                                Toast.LENGTH_LONG).show()
                        val user = task.result?.user
                        updateUI(user)
                    } else {
                        Log.w(tag, "linkWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }
        credentialWantToMerge = null
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val tag = "FB TOKEN"
        Log.d(tag, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInWithCredential:success")
                        val user = auth.currentUser
                        Toast.makeText(applicationContext, user?.email,Toast.LENGTH_LONG).show()
                        if(credentialWantToMerge!=null) linkAccount()
                        else updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInWithCredential:failure", task.exception)
                        try {
                            throw task.exception!!
                        } catch(e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(applicationContext, "User already exists, please login using the initial provider and we will merge it!",Toast.LENGTH_LONG).show()
                            credentialWantToMerge = credential
                        } catch(e: Exception) {
                            Log.e(tag, e.message)
                        }
                    }
                }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            myApp.setUser(user)
            Constant.GET_SUCCESS_MSG = 1
            strEmail = user.email.toString()
            strName = user.displayName.toString()
            strPassengerId = user.uid
            strImage = user.photoUrl.toString()
            setResult()
        }
    }

    private fun setupFacebookLogin() {
        val tag = "FB"
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(tag, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(tag, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(tag, "facebook:onError", error)
                // ...
            }
        })

        val buttonFacebookLogin = findViewById<Button>(R.id.btn_fb)
        buttonFacebookLogin.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email","public_profile"))
        }

    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val tag = "GOOGLE"
        Log.d(tag, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInWithCredential:success")
                        val user = auth.currentUser
                        Toast.makeText(applicationContext, user?.email,Toast.LENGTH_LONG).show()
                        if(credentialWantToMerge!=null) linkAccount()
                        else updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
//                        Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        Log.w(tag, "signInWithCredential:failure", task.exception)
                        try {
                            throw task.exception!!
                        } catch(e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(applicationContext, "User already exists, please login using initial provider and merge it!",Toast.LENGTH_LONG).show()
                            credentialWantToMerge = credential
                        } catch(e: Exception) {
                            Log.e(tag, e.message)
                        }
                    }
//                    hideProgressDialog()
                }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            supportActionBar!!.setHomeButtonEnabled(false)
            supportActionBar!!.title = ""
        }

        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        if (appBarLayout.layoutParams != null) {
            val layoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val appBarLayoutBehaviour = AppBarLayout.Behavior()
            appBarLayoutBehaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
            layoutParams.behavior = appBarLayoutBehaviour
        }
    }

    override fun onValidationSucceeded() {
        strEmail = edtEmail.text.toString()
        strPassword = edtPassword.text.toString()
        if (NetworkCheck.isNetworkAvailable(this@ActivityUserLogin)) {
            MyTaskLoginNormal().execute(Constant.NORMAL_LOGIN_URL + strEmail + "&password=" + strPassword)
        }
    }

    override fun onValidationFailed(failedView: View, failedRule: Rule<*>) {
        val message = failedRule.failureMessage
        if (failedView is EditText) {
            failedView.requestFocus()
            failedView.error = message
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show()
        }
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val tag = "GOOGLE"
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(tag, "Google sign in failed", e)
                // [START_EXCLUDE]
//                updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {

    }

    @SuppressLint("StaticFieldLeak")
    private inner class MyTaskLoginNormal : AsyncTask<String, Void, String>() {

        internal var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(this@ActivityUserLogin)
            progressDialog!!.setTitle(resources.getString(R.string.title_please_wait))
            progressDialog!!.setMessage(resources.getString(R.string.login_process))
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }

        override fun doInBackground(vararg params: String): String? {
            return NetworkCheck.getJSONString(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (null == result || result.isEmpty()) {

            } else {

                try {
                    val mainJson = JSONObject(result)
                    val jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME)
                    var objJson: JSONObject?
                    for (i in 0 until jsonArray.length()) {
                        objJson = jsonArray.getJSONObject(i)
                        if (objJson!!.has(Constant.MSG)) {
                            strMessage = objJson.getString(Constant.MSG)
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
                        } else {
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
                            strName = objJson.getString(Constant.USER_NAME)
                            strPassengerId = objJson.getString(Constant.USER_ID)
                            strImage = objJson.getString("normal")

                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


                Handler().postDelayed({
                    if (null != progressDialog && progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                    setResult()
                }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }

        }
    }

    fun setResult() {

        when {
            Constant.GET_SUCCESS_MSG == 0 -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.whops)
                dialog.setMessage(R.string.login_failed)
                dialog.setPositiveButton(R.string.dialog_ok, null)
                dialog.setCancelable(false)
                dialog.show()

            }
            Constant.GET_SUCCESS_MSG == 2 -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.whops)
                dialog.setMessage(R.string.login_disabled)
                dialog.setPositiveButton(R.string.dialog_ok, null)
                dialog.setCancelable(false)
                dialog.show()

            }
            else -> {
                myApp.saveIsLogin(true)
                myApp.saveLogin(strPassengerId, strName, strEmail)

                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.login_title)
                dialog.setMessage(R.string.login_success)
                dialog.setPositiveButton(R.string.dialog_ok) { _, _ -> finish() }
                dialog.setCancelable(false)
                dialog.show()

            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {

            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    companion object{
        private const val RC_SIGN_IN = 1
    }
}
