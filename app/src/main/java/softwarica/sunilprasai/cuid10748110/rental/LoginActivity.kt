package softwarica.sunilprasai.cuid10748110.rental

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.model.User
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val RC_SIGN_IN = 1001
private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var mLoginError: TextView
    private lateinit var mPhoneOrEmailTil: TextInputLayout
    private lateinit var mPhoneOrEmailEt: TextInputEditText

    private lateinit var mPasswordTil: TextInputLayout
    private lateinit var mPasswordEt: TextInputEditText

    private lateinit var registerBtn: MaterialButton
    private lateinit var loginBtn: MaterialButton

    private lateinit var mViewModel: AuthViewModel
    private var mShouldFetchUserDetails: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "LOGIN"

        mLoginError = findViewById(R.id.loginError)
        mPhoneOrEmailTil = findViewById(R.id.phoneEmailTil)
        mPhoneOrEmailEt = findViewById(R.id.phoneEmailEt)
        mPhoneOrEmailEt.onFocusChangeListener = this

        mPasswordTil = findViewById(R.id.passwordTil)
        mPasswordEt = findViewById(R.id.passwordEt)
        mPasswordEt.onFocusChangeListener = this

        loginBtn = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener(this)

        registerBtn = findViewById(R.id.registerBtn)
        registerBtn.setOnClickListener(this)

        mViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewModel.getLoggedInUser().observe(this) {
            it?.let {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mShouldFetchUserDetails && mViewModel.getLoggedInUserValue() == null && UserToken.getInstance(
                this
            ).token != null
        ) {
            mViewModel.fetchLoggedInUserDetails(this)
        }
        mShouldFetchUserDetails = true
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                registerBtn.id -> startActivity(Intent(baseContext, RegisterActivity::class.java))
                loginBtn.id -> {
                    if (validate()) {
                        val user: User = User().apply {
                            password = mPasswordEt.text?.trim().toString()
                            try {
                                phone = mPhoneOrEmailEt.text?.trim().toString().toLong()
                            } catch (e: Exception) {
                                Log.d(TAG, "Converting input to long failed: ${e.localizedMessage}")
                                email = mPhoneOrEmailEt.text?.trim().toString()
                            }
                        }
                        loginUser(user)
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        mPhoneOrEmailEt.text?.let {
            if (it.trim().toString().isEmpty()) {
                isValid = false
                mPhoneOrEmailTil.apply {
                    isErrorEnabled = true
                    error = "Phone number or email address is required"
                }
            } else {
                val message: String? = if (it.trim().isDigitsOnly() && it.trim().length != 10) {
                    "Phone number must be 10 characters long"
                } else if (!it.trim().isDigitsOnly() && !Patterns.EMAIL_ADDRESS.matcher(
                        it.trim().toString()
                    ).matches()
                ) {
                    "Please provide valid email address"
                } else null

                if (message != null) {
                    isValid = false
                    mPhoneOrEmailTil.apply {
                        isErrorEnabled = true
                        error = message
                    }
                } else {
                    mPhoneOrEmailTil.isErrorEnabled = false
                }

            }
        }

        mPasswordEt.text?.let {
            if (it.trim().toString().isEmpty()) {
                isValid = false
                mPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Password is required"
                }
            }
        }
        if (!isValid) VibrateView.vibrate(
            applicationContext,
            R.anim.shake,
            findViewById(R.id.loginCardView)
        )
        return isValid
    }

    private fun loginUser(user: User) {
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), user.getJSONObject().toString())
        val consumer = APIService.getService(APIConsumer::class.java)
        val loginUser = consumer.loginUser(requestBody)
        loginUser.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        val jsonResponse = JSONObject(it.string())
                        val token = jsonResponse.getString("token")
                        val loggedInUser = User(jsonResponse.getJSONObject("user"))
                        AppState.loggedInUser = loggedInUser
                        UserToken.getInstance(baseContext).token = token
                        clearInputFields()
                        Keyboard.hide(this@LoginActivity)
                        mViewModel.setLoggedInUser(user)
                    }
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    val errorResponse = JSONObject(response.errorBody()!!.string())
                    if (errorResponse.has("message")) {
                        val errorMessage = StringBuilder()
                        try {
                            val errorResponseMessage = errorResponse.getJSONObject("message")
                            errorResponseMessage.keys().forEach {
                                errorMessage.append(errorResponseMessage[it]).append("\n")
                            }
                        } catch (e: JSONException) {
                            errorMessage.append(errorResponse.getString("message"))
                        } finally {
                            if (errorMessage.isNotEmpty())
                                displayAPIErrorResponseDialog(errorMessage.toString())
                        }
                    } else {
                        displayAPIErrorResponseDialog("Unable to verify credentials. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to verify credentials. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to verify credentials. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                AlertDialog.Builder(this@LoginActivity)
                    .setIcon(R.drawable.information)
                    .setTitle("INFORMATION")
                    .setMessage(message)
                    .setPositiveButton(
                        "OK"
                    ) { dialog, _ -> dialog!!.dismiss() }
                    .show()
            }
        })
    }

    private fun clearInputFields() {
        mPhoneOrEmailEt.setText("")
        mPasswordEt.setText("")
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        v?.let {
            if (v.id == mPhoneOrEmailEt.id) {
                if (!hasFocus) {
                    mPhoneOrEmailEt.text?.let {
                        when {
                            it.trim().isEmpty() -> {
                                mPhoneOrEmailTil.apply {
                                    isErrorEnabled = true
                                    error = "Email or phone is required"
                                }
                            }
                            else -> {
                                val message: String? =
                                    if (it.trim().isDigitsOnly() && it.trim().length != 10) {
                                        "Phone number must be 10 characters long"
                                    } else if (!it.trim()
                                            .isDigitsOnly() && !Patterns.EMAIL_ADDRESS.matcher(
                                            it.trim().toString()
                                        )
                                            .matches()
                                    ) {
                                        "Please provide valid email address"
                                    } else null

                                if (message != null) {
                                    mPhoneOrEmailTil.apply {
                                        isErrorEnabled = true
                                        error = message
                                    }
                                } else {
                                    mPhoneOrEmailTil.isErrorEnabled = false
                                }
                            }
                        }
                    }
                } else {
                    mPhoneOrEmailTil.isErrorEnabled = false
                }
            } else if (v.id == mPasswordEt.id) {
                if (!hasFocus) {
                    mPasswordEt.text?.let {
                        when {
                            it.trim().toString().isBlank() -> {
                                mPasswordTil.apply {
                                    isErrorEnabled = true
                                    error = "Password is required"
                                }
                            }
                            it.trim().length < 6 -> {
                                mPasswordTil.apply {
                                    isErrorEnabled = true
                                    error = "Password must be 6 characters long"
                                }
                            }
                            else -> {
                                //Do nothing
                            }
                        }
                    }
                } else {
                    mPasswordTil.isErrorEnabled = false
                }
            } else null
        }
    }
}