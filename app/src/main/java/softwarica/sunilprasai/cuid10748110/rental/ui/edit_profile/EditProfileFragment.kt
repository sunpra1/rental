package softwarica.sunilprasai.cuid10748110.rental.ui.edit_profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.AuthViewModel
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.GENDER_TYPE
import softwarica.sunilprasai.cuid10748110.rental.model.User
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "EditProfileFragment"
private const val REQUEST_ACCESS_EXTERNAL_STORAGE: Int = 9001
private const val REQUEST_CODE_SELECT_PHOTO: Int = 9002

class EditProfileFragment : Fragment(), View.OnClickListener,
    OnFocusChangeListener {
    private lateinit var mFullNameTil: TextInputLayout
    private lateinit var mFullNameEt: TextInputEditText
    private lateinit var mGenderTil: TextInputLayout
    private lateinit var mGenderSpn: Spinner
    private lateinit var mDobEt: TextInputEditText
    private lateinit var mDobTil: TextInputLayout
    private lateinit var mAddressTil: TextInputLayout
    private lateinit var mAddressEt: TextInputEditText
    private lateinit var mContactTil: TextInputLayout
    private lateinit var mContactEt: TextInputEditText
    private lateinit var mEmailTil: TextInputLayout
    private lateinit var mEmailEt: TextInputEditText
    private lateinit var mPasswordTil: TextInputLayout
    private lateinit var mPasswordEt: TextInputEditText
    private lateinit var mCPasswordTil: TextInputLayout
    private lateinit var mCPasswordEt: TextInputEditText
    private lateinit var mSelectImageBtn: MaterialButton
    private lateinit var mUpdateProfileBtn: MaterialButton
    private lateinit var mUserImage: ImageView
    private var mUserNewImageUri: Uri? = null
    private var mIsEmailValid = true
    private var mIsPhoneValid = true
    private var mImageSize: Double = 0.0
    private lateinit var mSelectImageTil: TextInputLayout
    private lateinit var mViewModel: AuthViewModel
    private lateinit var mContext: Context
    private lateinit var mUser: User

    private val mGenderItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position > 0) {
                mGenderTil.isErrorEnabled = false
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            mGenderTil.apply {
                isErrorEnabled = false
                error = "Please provide your gender"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        mContext = requireContext()
        mFullNameTil = view.findViewById(R.id.nameTil)
        mFullNameEt = view.findViewById(R.id.nameEt)
        mGenderTil = view.findViewById(R.id.selectGenderTil)
        mGenderSpn = view.findViewById(R.id.genderListSpn)
        val genderSpnAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            GENDER_TYPE
        )
        mGenderSpn.adapter = genderSpnAdapter
        mGenderSpn.onItemSelectedListener = mGenderItemSelectedListener
        mDobTil = view.findViewById(R.id.dobTil)
        mDobEt = view.findViewById(R.id.dobCV)
        mDobEt.onFocusChangeListener = this
        mAddressTil = view.findViewById(R.id.addressTil)
        mAddressEt = view.findViewById(R.id.addressEt)
        mContactTil = view.findViewById(R.id.contactTil)
        mContactEt = view.findViewById(R.id.contactEt)
        mContactEt.onFocusChangeListener = this
        mEmailTil = view.findViewById(R.id.emailTil)
        mEmailEt = view.findViewById(R.id.emailEt)
        mEmailEt.onFocusChangeListener = this
        mPasswordTil = view.findViewById(R.id.passwordTil)
        mPasswordEt = view.findViewById(R.id.passwordEt)
        mPasswordEt.onFocusChangeListener = this
        mCPasswordTil = view.findViewById(R.id.cPasswordTil)
        mCPasswordEt = view.findViewById(R.id.cPasswordEt)
        mCPasswordEt.onFocusChangeListener = this
        mUserImage = view.findViewById(R.id.userImage)
        mSelectImageTil = view.findViewById(R.id.selectImageTil)
        mSelectImageBtn = view.findViewById(R.id.selectImageBtn)
        mSelectImageBtn.setOnClickListener(this)
        mUpdateProfileBtn = view.findViewById(R.id.update_profile_btn)
        mUpdateProfileBtn.setOnClickListener(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        mViewModel.getLoggedInUser().observe(requireActivity())
        {
            if (isAdded && it != null) {
                mUser = it
                updateView()
            }
        }
    }

    private fun updateView() {
        if (mUser.image != null) {
            mUserImage.visibility = View.VISIBLE
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mUserImage.setImageBitmap(imageBitmap)
                }
            }).execute(mUser.image!!.buffer)
        } else {
            mUserImage.visibility = View.GONE
        }
        mFullNameEt.setText(mUser.fullName)
        if (mUser.gender != null) {
            mGenderSpn.setSelection(GENDER_TYPE.indexOf(mUser.gender.toString()))
        }
        if (mUser.dob != null)
            mDobEt.setText(SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(mUser.dob!!))

        if (mUser.address != null)
            mAddressEt.setText(mUser.address)

        if (mUser.email != null)
            mEmailEt.setText(mUser.email)

        mContactEt.setText(mUser.phone.toString())
    }

    private fun validate(): Boolean {
        var isValid = true
        if (mFullNameEt.text.isNullOrEmpty()) {
            mFullNameTil.apply {
                isErrorEnabled = true
                error = "Name filed is left empty"
            }
            isValid = false
        }

        if (mGenderSpn.selectedItemPosition == 0) {
            mGenderTil.apply {
                isErrorEnabled = true
                error = "Please provide your gender"
            }
            isValid = false
        }

        if (mDobEt.text.isNullOrEmpty()) {
            mDobTil.apply {
                isErrorEnabled = true
                error = "Please provide your DOB"
            }
            isValid = false
        } else {
            try {
                val date = SimpleDateFormat(
                    yyyy_MM_dd,
                    Locale.ENGLISH
                ).parse(mDobEt.text!!.toString())
                if (date == null) {
                    mDobTil.apply {
                        isErrorEnabled = true
                        error = "Please provide valid date in format of yyyy-mm-dd"
                    }
                    isValid = false
                }
            } catch (e: ParseException) {
                Log.e(TAG, "validate: ${e.localizedMessage}")
                mDobTil.apply {
                    isErrorEnabled = true
                    error = "Please provide valid date in format of yyyy-mm-dd"
                }
                isValid = false
            }
        }

        if (mContactEt.text.isNullOrEmpty()) {
            mContactTil.apply {
                isErrorEnabled = true
                error = "Contact field is left empty"
            }
            isValid = false
        } else if (!mIsPhoneValid) {
            mContactTil.apply {
                isErrorEnabled = true
                error = "Phone number is already taken"
            }
            isValid = false
        }

        if (mEmailEt.text.isNullOrEmpty()) {
            mEmailTil.apply {
                isErrorEnabled = true
                error = "Please provide your email address"
            }
            isValid = false
        } else if (mEmailEt.text!!.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(mEmailEt.text!!)
                .matches()
        ) {
            mEmailTil.apply {
                isErrorEnabled = true
                error = "Please provide valid email address"
            }
            isValid = false
        } else if (!mIsEmailValid) {
            mEmailTil.apply {
                isErrorEnabled = true
                error = "Email address is already taken"
            }
            isValid = false
        }

        if (mAddressEt.text.isNullOrEmpty()) {
            mAddressTil.apply {
                isErrorEnabled = true
                error = "Please provide your address"
            }
            isValid = false
        }

        if (!mPasswordEt.text.isNullOrEmpty()) {
            if (mPasswordEt.text!!.length < 6) {
                mPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Confirm password must be 6 characters long"
                }
                isValid = false
            } else if (mCPasswordEt.text.isNullOrEmpty()) {
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Confirm password must be filled"
                }
                isValid = false
            }
        }
        if (!mCPasswordEt.text.isNullOrEmpty()) {
            if (mCPasswordEt.text!!.length < 6) {
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Confirm password must be 6 characters long"
                }
                isValid = false
            } else if (mPasswordEt.text.isNullOrEmpty()) {
                mCPasswordTil.apply {
                    isErrorEnabled = true
                    error = "Both password and confirm password must be filled"
                }
                isValid = false
            }
        }
        if (!mPasswordEt.text.isNullOrEmpty() && !mCPasswordEt.text.isNullOrEmpty() && mPasswordEt.text!! != mCPasswordEt.text!!) {
            mCPasswordTil.apply {
                isErrorEnabled = true
                error = "Confirm password doesn't match"
            }
            isValid = false
        }

        if (mUser.image == null) {
            if (mUserNewImageUri == null) {
                mSelectImageTil.apply {
                    isErrorEnabled = true
                    error = "Please provide your image"
                }
                isValid = false
            } else if (mUserNewImageUri != null && mImageSize > 2) {
                mSelectImageTil.apply {
                    isErrorEnabled = true
                    error = "File size exceeded the permitted limit of 2MB"
                }
                isValid = false
            }
        }
        return isValid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            mSelectImageTil.isErrorEnabled = false
            mImageSize = FileHandler.getFileSize(data.data!!, mContext)
            if (mImageSize > VALID_IMAGE_SIZE) {
                mUserImage.visibility = View.GONE
                mUserNewImageUri = null
                mSelectImageTil.apply {
                    isErrorEnabled = true
                    error = "Image highlighted in red has exceeded the prescribed limit of 2MB"
                }
            } else {
                mUserImage.visibility = View.VISIBLE
                mUserNewImageUri = data.data!!
                mUserImage.setImageURI(mUserNewImageUri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View) {
        when (v.id) {
            mSelectImageBtn.id -> {
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                    }
                    startActivityForResult(
                        intent,
                        REQUEST_CODE_SELECT_PHOTO
                    )
                } else {
                    handleStoragePermissions()
                }
            }

            mUpdateProfileBtn.id -> {
                if (validate()) {
                    updateProfile(User().apply {
                        fullName = mFullNameEt.text!!.toString()
                        phone = mContactEt.text!!.toString().toLong()
                        address = mAddressEt.text!!.toString()
                        email = mEmailEt.text!!.toString()
                        gender = User.getGender(GENDER_TYPE[mGenderSpn.selectedItemPosition])
                        dob = SimpleDateFormat(
                            yyyy_MM_dd,
                            Locale.ENGLISH
                        ).parse(mDobEt.text!!.toString())
                        if (!mPasswordEt.text.isNullOrEmpty())
                            password = mPasswordEt.text!!.toString()
                    })
                }
            }
        }
    }

    private fun handleStoragePermissions() {
        Log.d(TAG, "handlePermissions: ")
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("Information")
                setMessage("To be able to upload the image, this app requires permission to access file storage.\nDo you want to provide file storage access?")
                setPositiveButton(R.string.ok) { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri =
                        Uri.fromParts(
                            "package",
                            requireActivity().packageName,
                            this::class.java.simpleName
                        )
                    intent.data = uri
                    startActivity(intent)
                }
                setNegativeButton(getString(R.string.no_thanks)) { _, _ ->
                    //Do nothing
                }
                show()
            }
        } else {
            Log.d(TAG, "handlePermissions: shouldShowRequestPermissionRationale is false")
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_ACCESS_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == REQUEST_ACCESS_EXTERNAL_STORAGE && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(
                TAG,
                "onRequestPermissionsResult: permission to access external storage granted"
            )
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(
                intent,
                REQUEST_CODE_SELECT_PHOTO
            )
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        when (v.id) {
            mFullNameEt.id -> {
                if (!hasFocus && mFullNameEt.text.isNullOrEmpty()) {
                    mFullNameTil.apply {
                        isErrorEnabled = true
                        error = "Name filed is left empty"
                    }
                } else
                    mFullNameTil.apply {
                        isErrorEnabled = false
                    }
            }

            mDobEt.id -> {
                if (!hasFocus && mDobEt.text.isNullOrEmpty()) {
                    mDobTil.apply {
                        isErrorEnabled = true
                        error = "Please provide your DOB"
                    }
                } else if (!hasFocus && !mDobEt.text.isNullOrEmpty()) {
                    try {
                        val date = SimpleDateFormat(
                            yyyy_MM_dd,
                            Locale.ENGLISH
                        ).parse(mDobEt.text!!.toString())
                        if (date == null) {
                            mDobTil.apply {
                                isErrorEnabled = true
                                error = "Please provide valid date in format of yyyy-mm-dd"
                            }
                        }
                    } catch (e: ParseException) {
                        mDobTil.apply {
                            isErrorEnabled = true
                            error = "Please provide valid date in format of yyyy-mm-dd"
                        }
                    }
                } else
                    mDobTil.apply {
                        isErrorEnabled = false
                    }
            }

            mContactEt.id -> {
                if (!hasFocus && mContactEt.text.isNullOrEmpty()) {
                    mContactTil.apply {
                        isErrorEnabled = true
                        error = "Contact number is left empty"
                    }
                } else if (!hasFocus && mContactEt.text!!.isNotEmpty() && mContactEt.text!!.length != 10) {
                    mContactTil.apply {
                        isErrorEnabled = true
                        error = "Contact number must be 10 characters long"
                    }
                } else if (!hasFocus && mContactEt.text!!.isNotEmpty() && mContactEt.text!!.length == 10 && mContactEt.text!!.toString()
                        .toLong() != mViewModel.getLoggedInUser().value!!.phone
                ) {
                    handlePhoneValidation(mContactEt.text!!.toString())
                } else {
                    mContactTil.apply {
                        isErrorEnabled = false
                    }
                }
            }

            mEmailEt.id -> {
                if (!hasFocus && mEmailEt.text.isNullOrEmpty()) {
                    mEmailTil.apply {
                        isErrorEnabled = true
                        error = "Please provide your email address"
                    }
                } else if (!hasFocus && mEmailEt.text!!.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                        mEmailEt.text!!
                    ).matches()
                ) {
                    mContactTil.apply {
                        isErrorEnabled = true
                        error = "Please provide valid email address"
                    }
                } else if (!hasFocus && mEmailEt.text!!.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(
                        mEmailEt.text!!
                    )
                        .matches() && mEmailEt.text!!.toString() != mViewModel.getLoggedInUser().value!!.email
                ) {
                    handleEmailValidation(mEmailEt.text!!.toString())
                } else {
                    mContactTil.apply {
                        isErrorEnabled = false
                    }
                }
            }

            mAddressEt.id -> {
                if (!hasFocus && mAddressEt.text.isNullOrEmpty()) {
                    mAddressTil.apply {
                        isErrorEnabled = true
                        error = "Please provide your address"
                    }
                } else
                    mAddressTil.apply {
                        isErrorEnabled = false
                    }
            }

            mCPasswordEt.id -> {
                if (!mCPasswordEt.text.isNullOrEmpty()) {
                    if (mCPasswordEt.text!!.length < 6) {
                        mCPasswordTil.apply {
                            isErrorEnabled = true
                            error = "Password must be 6 characters long"
                        }
                    } else if (!mPasswordEt.text.isNullOrEmpty() && mCPasswordEt.text!! != mPasswordEt.text!!) {
                        mCPasswordTil.apply {
                            isErrorEnabled = true
                            error = "Confirm password does not match"
                        }
                    }
                }
            }
            mPasswordEt.id -> {
                if (!mPasswordEt.text.isNullOrEmpty()) {
                    if (mPasswordEt.text!!.length < 6) {
                        mPasswordTil.apply {
                            isErrorEnabled = true
                            error = "Password must be 6 characters long"
                        }
                    } else if (!mCPasswordEt.text.isNullOrEmpty() && mCPasswordEt.text!! != mPasswordEt.text!!) {
                        mCPasswordTil.apply {
                            isErrorEnabled = true
                            error = "Confirm password does not match"
                        }
                    }
                }
            }
        }
    }

    private fun updateProfile(user: User) {
        val requestBody = HashMap<String, RequestBody>().apply {
            put(User.FULL_NAME, RequestBody.create(MultipartBody.FORM, user.fullName!!))
            put(User.GENDER, RequestBody.create(MultipartBody.FORM, user.gender!!.toString()))
            put(User.PHONE, RequestBody.create(MultipartBody.FORM, user.phone!!.toString()))
            put(User.EMAIL, RequestBody.create(MultipartBody.FORM, user.email!!))
            put(User.ADDRESS, RequestBody.create(MultipartBody.FORM, user.address!!))
            put(
                User.DOB,
                RequestBody.create(
                    MultipartBody.FORM,
                    SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(user.dob!!)
                )
            )
            user.password?.let {
                put(User.PASSWORD, RequestBody.create(MultipartBody.FORM, user.password!!))
            }
        }

        var imagePart: MultipartBody.Part? = null
        if (mUserNewImageUri != null) {
            val selectedImage = FileHandler.getFile(mUserNewImageUri!!, mContext)
            val imageBody = RequestBody.create(MultipartBody.FORM, selectedImage)
            imagePart =
                MultipartBody.Part.createFormData(User.IMAGE, selectedImage.name, imageBody)
        }

        val consumer = APIService.getService(APIConsumer::class.java)
        val updateProfile = consumer.updateUserProfile(
            UserToken.getInstance(mContext).token!!,
            imagePart,
            requestBody
        )
        updateProfile.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val userResponse = User(JSONObject(response.body()!!.string()))
                    mViewModel.setLoggedInUser(userResponse)
                    AppToast.show(
                        mContext,
                        "${userResponse.fullName} profile updated successfully",
                        Toast.LENGTH_LONG
                    )
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
                        displayAPIErrorResponseDialog("Unable to update your profile details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update your profile details. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update your profile details. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                android.app.AlertDialog.Builder(mContext)
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

    private fun handleEmailValidation(Email: String) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val jsonObject = JSONObject().apply {
            put(User.EMAIL, Email)
        }
        val requestBody = RequestBody.create(MediaType.parse(TYPE_JSON), jsonObject.toString())
        val validateEmail = consumer.validateEmailAddress(requestBody)
        validateEmail.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val jsonResponse = JSONObject(response.body()!!.string())
                        Log.d(
                            TAG,
                            "onResponse: $jsonResponse"
                        )
                        mIsEmailValid =
                            jsonResponse.has("isUnique") && jsonResponse.getBoolean("isUnique")

                        if (!mIsEmailValid) {
                            mEmailTil.apply {
                                isErrorEnabled = true
                                error = "Email address is already taken"
                            }
                        } else {
                            mEmailTil.apply {
                                isErrorEnabled = false
                                endIconMode = TextInputLayout.END_ICON_CUSTOM
                                setEndIconDrawable(R.drawable.check_circle_green)
                            }
                        }
                    } else if (!response.isSuccessful && response.errorBody() != null) {
                        mIsEmailValid = false
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
                            displayAPIErrorResponseDialog("Unable to email address credentials. Please try again later")
                        }
                    } else {
                        mIsEmailValid = false
                        displayAPIErrorResponseDialog("Unable to verify email address. Please try again later")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mIsEmailValid = false
                    displayAPIErrorResponseDialog("Unable to verify email address. Please try again later")
                }

                private fun displayAPIErrorResponseDialog(message: String) {
                    android.app.AlertDialog.Builder(mContext)
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

    private fun handlePhoneValidation(phone: String) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val jsonObject = JSONObject().apply {
            put(User.PHONE, phone)
        }

        val requestBody = RequestBody.create(MediaType.parse(TYPE_JSON), jsonObject.toString())

        val validatePhone = consumer.validatePhoneNumber(requestBody)

        validatePhone.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val jsonResponse = JSONObject(response.body()!!.string())
                        Log.d(
                            TAG,
                            "onResponse: $jsonResponse"
                        )
                        mIsPhoneValid =
                            jsonResponse.has("isUnique") && jsonResponse.getBoolean("isUnique")

                        if (!mIsPhoneValid) {
                            mContactTil.apply {
                                isErrorEnabled = true
                                error = "Phone number is already taken"
                            }
                        } else {
                            mContactTil.apply {
                                isErrorEnabled = false
                                endIconMode = TextInputLayout.END_ICON_CUSTOM
                                setEndIconDrawable(R.drawable.check_circle_green)
                            }
                        }
                    } else if (!response.isSuccessful && response.errorBody() != null) {
                        mIsPhoneValid = false
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
                            displayAPIErrorResponseDialog("Unable to verify contact number. Please try again later")
                        }
                    } else {
                        mIsPhoneValid = false
                        displayAPIErrorResponseDialog("Unable to verify contact number. Please try again later")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mIsPhoneValid = false
                    displayAPIErrorResponseDialog("Unable to verify contact number. Please try again later")
                }

                private fun displayAPIErrorResponseDialog(message: String) {
                    android.app.AlertDialog.Builder(mContext)
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
}