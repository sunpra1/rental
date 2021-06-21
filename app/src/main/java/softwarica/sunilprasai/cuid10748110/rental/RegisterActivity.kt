package softwarica.sunilprasai.cuid10748110.rental

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
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

private const val TAG = "RegisterActivity"

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {
    private lateinit var mNameTil: TextInputLayout
    private lateinit var mNameEt: TextInputEditText
    private lateinit var mPhoneTil: TextInputLayout
    private lateinit var mPhoneEt: TextInputEditText
    private lateinit var mPasswordTil: TextInputLayout
    private lateinit var mPasswordEt: TextInputEditText
    private lateinit var mCPasswordTil: TextInputLayout
    private lateinit var mCPasswordEt: TextInputEditText
    private lateinit var mRegisterBtn: MaterialButton

    private lateinit var mViewModel: AuthViewModel
    private var isPhoneNumberValid = false
    private var mShouldFetchUserDetails: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "REGISTER"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mNameTil = findViewById(R.id.nameTil)
        mNameEt = findViewById(R.id.nameEt)
        mNameEt.onFocusChangeListener = this

        mPhoneTil = findViewById(R.id.phoneTil)
        mPhoneEt = findViewById(R.id.phoneEt)
        mPhoneEt.onFocusChangeListener = this
        mPhoneEt.setOnKeyListener(this)

        mPasswordTil = findViewById(R.id.passwordTil)
        mPasswordEt = findViewById(R.id.passwordEt)
        mPasswordEt.onFocusChangeListener = this

        mCPasswordTil = findViewById(R.id.cPasswordTil)
        mCPasswordEt = findViewById(R.id.cPasswordEt)
        mCPasswordEt.onFocusChangeListener = this

        mRegisterBtn = findViewById(R.id.registerBtn)
        mRegisterBtn.setOnClickListener(this)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun validate(): Boolean {
        var isValid = true
        mNameEt.text?.let {
            if (it.trim().toString().isEmpty()) {
                isValid = false
                mNameTil.apply {
                    isErrorEnabled = true
                    error = "Full name is required"
                }
            }
        }

        mPhoneEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mPhoneTil.apply {
                        isErrorEnabled = true
                        error = "Phone number is required"
                    }
                }
                it.trim().length != 10 -> {
                    isValid = false
                    mPhoneTil.apply {
                        isErrorEnabled = true
                        error = "Phone number must be 10 characters long"
                    }
                }
                !it.trim().isDigitsOnly() -> {
                    isValid = false
                    mPhoneTil.apply {
                        isErrorEnabled = true
                        error = "Phone number must contain numeric value"
                    }
                }

                !isPhoneNumberValid -> {
                    isValid = false
                    mPhoneTil.apply {
                        isErrorEnabled = true
                        error = "Phone number is already taken"
                    }
                }
                else -> null
            }
        }

        val password: String = mPasswordEt.text?.trim().toString()
        val cPassword: String = mCPasswordEt.text?.trim().toString()

        when {
            password.isEmpty() -> {
                isValid = false
                mPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Password is required"
                }
            }
            password.length < 6 -> {
                mPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Password must be six characters long"
                }
            }
        }

        when {
            cPassword.isEmpty() -> {
                isValid = false
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Password is required"
                }
            }
            cPassword.length < 6 -> {
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Password must be six characters long"
                }
            }
            password != cPassword -> {
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "confirm password doesn't match"
                }
            }
        }

        if (!isValid) VibrateView.vibrate(
            applicationContext,
            R.anim.shake,
            findViewById(R.id.formCardView)
        )

        return isValid
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                mRegisterBtn.id -> {
                    if (validate()) {
                        val user = User()
                            .apply {
                                fullName = mNameEt.text?.trim().toString()
                                phone = mPhoneEt.text?.trim().toString().toLong()
                                password = mPasswordEt.text?.trim().toString()
                            }

                        registerUser(user)
                    }
                }
                else -> return
            }
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                mNameEt.id -> {
                    mNameEt.text?.let {
                        if (!hasFocus && it.trim().toString()
                                .isEmpty()
                        ) {
                            mNameTil.apply {
                                isErrorEnabled = true
                                error = "Full name is required"
                                VibrateView.vibrate(baseContext, R.anim.shake, this)
                            }
                        } else {
                            mNameTil.isErrorEnabled = false
                        }
                    }
                }

                mPhoneEt.id -> {
                    mPhoneEt.text?.let {
                        val phone = it.trim().toString()
                        if (!hasFocus) {
                            when {
                                phone.isEmpty() -> {
                                    mPhoneTil.apply {
                                        isErrorEnabled = true
                                        error = "Phone is required"
                                        VibrateView.vibrate(baseContext, R.anim.shake, this)
                                    }
                                }
                                phone.length != 10 -> {
                                    mPhoneTil.apply {
                                        isErrorEnabled = true
                                        error = "Phone must be 10 characters long"
                                    }
                                }
                                else -> {
                                    validatePhoneNumber(phone)
                                }
                            }
                        } else {
                            mPhoneTil.isErrorEnabled = false
                        }
                    }
                }

                mPasswordEt.id -> {
                    mPasswordEt.text?.let {
                        val password = it.trim().toString()
                        if (!hasFocus) {
                            when {
                                password.isEmpty() -> {
                                    mPasswordTil.apply {
                                        isErrorEnabled = true
                                        error = "Password is required"
                                        VibrateView.vibrate(baseContext, R.anim.shake, this)
                                    }
                                }
                                password.length < 6 -> {
                                    mPasswordTil.apply {
                                        isErrorEnabled = true
                                        error = "Password must be 6 characters long"
                                        VibrateView.vibrate(baseContext, R.anim.shake, this)
                                    }
                                }
                                else -> {
                                    val cPassword = mCPasswordEt.text?.trim().toString()
                                    if (cPassword.length > 5 && password != cPassword) {
                                        mCPasswordTil.apply {
                                            isErrorEnabled = true
                                            error = "Confirm password doesn't match"
                                            VibrateView.vibrate(baseContext, R.anim.shake, this)
                                        }
                                    } else null
                                }
                            }
                        } else {
                            mPasswordTil.isErrorEnabled = false
                        }
                    }
                }

                mCPasswordEt.id -> {
                    mCPasswordEt.text?.let {
                        if (!hasFocus) {
                            val cPassword = it.trim().toString()
                            when {
                                cPassword.isEmpty() -> {
                                    mCPasswordTil.apply {
                                        isErrorEnabled = true
                                        error = "Confirm password is required"
                                        VibrateView.vibrate(baseContext, R.anim.shake, this)
                                    }
                                }
                                cPassword.length < 6 -> {
                                    mCPasswordTil.apply {
                                        isErrorEnabled = true
                                        error = "Confirm password must be 6 characters long"
                                        VibrateView.vibrate(baseContext, R.anim.shake, this)
                                    }
                                }
                                else -> {
                                    val password = mPasswordEt.text?.trim().toString()
                                    if (password.length > 5 && password != cPassword) {
                                        mCPasswordTil.apply {
                                            isErrorEnabled = true
                                            error = "Confirm password doesn't match"
                                            VibrateView.vibrate(baseContext, R.anim.shake, this)
                                        }
                                    } else null
                                }
                            }
                        } else {
                            mCPasswordTil.isErrorEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse(TYPE_JSON),
            JSONObject().put("phone", phoneNumber).toString()
        )
        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val validatePhoneNumber: Call<ResponseBody> = consumer.validatePhoneNumber(requestBody)

        validatePhoneNumber.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        val jsonResponse = JSONObject(it.string())
                        val isUnique =
                            jsonResponse.has("isUnique") && jsonResponse.getBoolean("isUnique")
                        Log.d(TAG, "onResponse: isPhoneUnique -> $isUnique")
                        if (!isUnique) {
                            isPhoneNumberValid = false
                            mPhoneTil.apply {
                                isErrorEnabled = true
                                error = "Phone number is already taken"
                            }
                        } else {
                            isPhoneNumberValid = true
                            mPhoneTil.apply {
                                isErrorEnabled = false
                                endIconMode = TextInputLayout.END_ICON_CUSTOM
                                setEndIconDrawable(R.drawable.check_circle_green)
                            }
                        }
                    }
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    isPhoneNumberValid = false
                    val errorResponseMessage = JSONObject(response.errorBody()!!.string())
                    if (errorResponseMessage.has("message")) {
                        try {
                            val message = errorResponseMessage.getJSONObject("message")
                            if (message.has(User.PHONE)) {
                                mPhoneTil.apply {
                                    isErrorEnabled = false
                                    error = message.getString(User.PHONE)
                                }
                            }
                        } catch (e: Exception) {
                            val message = errorResponseMessage.getString("message")
                            mPhoneTil.apply {
                                isErrorEnabled = false
                                error = message
                            }
                        }
                    } else {
                        mPhoneTil.apply {
                            isErrorEnabled = false
                            error = "Unique phone number validation failed"
                        }
                    }
                } else {
                    isPhoneNumberValid = false
                    mPhoneTil.apply {
                        isErrorEnabled = false
                        error = "Unique phone number validation failed"
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mPhoneTil.apply {
                    isErrorEnabled = false
                    error = "Unique phone number validation failed"
                }
            }
        })
    }

    private fun clearInputFields() {
        mNameEt.setText("")
        mPhoneEt.setText("")
        mPasswordEt.setText("")
        mCPasswordEt.setText("")
    }

    private fun registerUser(user: User) {
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), user.getJSONObject().toString())

        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val registerUser: Call<ResponseBody> = consumer.registerUser(requestBody)
        registerUser.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        val jsonResponse = JSONObject(it.string())
                        val token = jsonResponse.getString("token")
                        val newUser = User(jsonResponse.getJSONObject("user"))

                        clearInputFields()
                        Keyboard.hide(this@RegisterActivity)

                        UserToken.getInstance(baseContext).token = token
                        AppState.loggedInUser = newUser

                        mViewModel.setLoggedInUser(newUser)
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
                        displayAPIErrorResponseDialog("Unable to get you registered. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to get you registered. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to get you registered. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                AlertDialog.Builder(this@RegisterActivity)
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

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        v?.let {
            if (v.id == mPhoneEt.id) {
                mPhoneEt.text?.let { phone ->
                    if (phone.trim().length == 10 && phone.trim().isDigitsOnly()) {
                        validatePhoneNumber(phone.trim().toString())
                    } else {
                        isPhoneNumberValid = false
                    }
                }
            }
        }
        return false
    }
}