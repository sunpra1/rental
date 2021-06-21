package softwarica.sunilprasai.cuid10748110.rental.ui.add_house

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.form_state.AddHouseFormState
import softwarica.sunilprasai.cuid10748110.rental.model.House
import softwarica.sunilprasai.cuid10748110.rental.model.Location
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.SelectedImagesRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.select_location.SelectLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val TAG = "AddHouseFragment"
private const val REQUEST_CODE_CHECK_SETTINGS: Int = 1000
private const val REQUEST_ACCESS_FINE_LOCATION: Int = 1001
private const val REQUEST_ACCESS_EXTERNAL_STORAGE: Int = 1002
private const val REQUEST_CODE_SELECT_PHOTOS: Int = 1003


class AddHouseFragment : Fragment(), View.OnClickListener, View.OnFocusChangeListener,
    SelectedImagesRVA.OnSelectedImageDeleteClickListener {
    private lateinit var mAddressTil: TextInputLayout
    private lateinit var mAddressEt: TextInputEditText

    private lateinit var mFloorCountTil: TextInputLayout
    private lateinit var mFloorCountEt: TextInputEditText

    private lateinit var mLongitudeTil: TextInputLayout
    private lateinit var mLongitudeEt: TextInputEditText

    private lateinit var mLatitudeTil: TextInputLayout
    private lateinit var mLatitudeEt: TextInputEditText

    private lateinit var mSelectLocationFromMapBtn: MaterialButton
    private lateinit var mGetCurrentLocationBtn: MaterialButton

    private lateinit var mSelectImagesBtn: MaterialButton
    private lateinit var mSelectImagesBtnTil: TextInputLayout
    private lateinit var mSelectedImagesRVTil: TextInputLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: SelectedImagesRVA

    private lateinit var mAddBtn: MaterialButton

    private lateinit var mViewModel: HouseViewModel

    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private var mUserLocation: LatLng? = null
    private var mSelectedImagesList: ArrayList<Uri> = ArrayList()
    private lateinit var mContext: Context

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            p0?.lastLocation?.let {
                Log.d(TAG, "onLocationResult: $it")

                val currentLocation = LatLng(it.latitude, it.longitude)
                if (mUserLocation != currentLocation) {
                    mLatitudeTil.isErrorEnabled = false
                    mLongitudeTil.isErrorEnabled = false

                    mLatitudeEt.setText(currentLocation.latitude.toString())
                    mLongitudeEt.setText(currentLocation.longitude.toString())
                    mUserLocation = currentLocation

                    //Saving state
                    AddHouseFormState.latitude = mUserLocation?.latitude
                    AddHouseFormState.longitude = mUserLocation?.longitude
                } else {
                    stopLocationUpdates()
                }
            }
        }
    }

    @SuppressLint("VisibleForTests")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_house, container, false)
        mContext = requireContext()

        mAddressTil = view.findViewById(R.id.addressTil)
        mAddressEt = view.findViewById(R.id.addressEt)
        mAddressEt.onFocusChangeListener = this

        mFloorCountTil = view.findViewById(R.id.floorsCountTil)
        mFloorCountEt = view.findViewById(R.id.floorsCountEt)
        mFloorCountEt.onFocusChangeListener = this

        mLongitudeTil = view.findViewById(R.id.locationLongitudeTil)
        mLongitudeEt = view.findViewById(R.id.locationLongitudeEt)
        mLongitudeEt.onFocusChangeListener = this

        mLatitudeTil = view.findViewById(R.id.locationLatitudeTil)
        mLatitudeEt = view.findViewById(R.id.locationLatitudeEt)
        mLatitudeEt.onFocusChangeListener = this

        mGetCurrentLocationBtn = view.findViewById(R.id.getCurrentLocationBtn)
        mGetCurrentLocationBtn.setOnClickListener(this)

        mSelectLocationFromMapBtn = view.findViewById(R.id.selectLocationFromMapBtn)
        mSelectLocationFromMapBtn.setOnClickListener(this)

        mSelectImagesBtn = view.findViewById(R.id.selectImagesBtn)
        mSelectImagesBtn.setOnClickListener(this)
        mSelectImagesBtnTil = view.findViewById(R.id.selectImagesBtnTil)

        mSelectedImagesRVTil = view.findViewById(R.id.selectedImagesRVTil)
        mRecyclerView = view.findViewById(R.id.selectedImagesRV)
        mRecyclerViewAdapter = SelectedImagesRVA()
        mRecyclerViewAdapter.setOnSelectedImageDeleteClickListener(this)
        mRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mRecyclerView.adapter = mRecyclerViewAdapter

        mAddBtn = view.findViewById(R.id.addHouseBtn)
        mAddBtn.setOnClickListener(this)

        mLocationClient = FusedLocationProviderClient(mContext)
        mLocationRequest = LocationRequest.create().apply {
            interval = 20000
            fastestInterval = 20000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            Keyboard.hide(requireActivity())
            when (v.id) {
                mGetCurrentLocationBtn.id -> {
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mUserLocation = null
                        getCurrentLocationSettingsAndStartLocationUpdates()
                    } else {
                        handleLocationPermissions()
                    }
                }

                mSelectLocationFromMapBtn.id -> {
                    saveFormState()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                        .addToBackStack(SelectLocationFragment::class.java.simpleName)
                        .replace(R.id.fragment_holder_two, SelectLocationFragment()).commit()
                }

                mSelectImagesBtn.id -> {
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        startActivityForResult(
                            intent,
                            REQUEST_CODE_SELECT_PHOTOS
                        )
                    } else {
                        handleStoragePermissions()
                    }
                }

                mAddBtn.id -> {
                    if (validate()) {
                        val house = House().apply {
                            address = mAddressEt.text!!.trim().toString()
                            floors = mFloorCountEt.text!!.trim().toString().toInt()
                            location = Location().apply {
                                latitude = mLatitudeEt.text!!.trim().toString().toDouble()
                                longitude = mLongitudeEt.text!!.trim().toString().toDouble()
                            }
                        }

                        addHouse(house)
                    }
                }
            }
        }
    }

    private fun addHouse(house: House) {
        if (AppState.loggedInUser != null) {
            val requestBody = HashMap<String, RequestBody>().apply {
                put(House.ADDRESS, RequestBody.create(MultipartBody.FORM, house.address!!))
                put(House.FLOORS, RequestBody.create(MultipartBody.FORM, house.floors.toString()))
                put(
                    House.LOCATION,
                    RequestBody.create(
                        MultipartBody.FORM,
                        house.location?.getJSONObject().toString()
                    )
                )
            }

            val images: ArrayList<MultipartBody.Part> = ArrayList()
            if (mSelectedImagesList.size > 0) {
                mSelectedImagesList.forEach {
                    val selectedFile = FileHandler.getFile(it, mContext)
                    val filePart = RequestBody.create(MultipartBody.FORM, selectedFile)
                    images.add(
                        MultipartBody.Part.createFormData(
                            Tenancy.IMAGES,
                            selectedFile.name,
                            filePart
                        )
                    )
                }
            }

            val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
            val addHouseRequest = consumer.addHouse(
                UserToken.getInstance(mContext).token!!,
                images,
                requestBody
            )
            addHouseRequest.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        clearFields()
                        val newHouse = House(JSONObject(response.body()!!.string()))
                        mViewModel.addHouse(newHouse)
                        requireActivity().supportFragmentManager.popBackStack()

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
                                    apiErrorResponseDialog(errorMessage.toString())
                            }
                        } else {
                            apiErrorResponseDialog("Unable to add house. Please try again later")
                        }
                    } else {
                        apiErrorResponseDialog("Unable to add house. Please try again later")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.localizedMessage}`")
                    apiErrorResponseDialog("Unable to add house. Please try again later")
                }
            })
        }
    }

    private fun apiErrorResponseDialog(message: String) {
        if (isAdded)
            AlertDialog.Builder(mContext)
                .setIcon(R.drawable.information)
                .setTitle("INFORMATION")
                .setMessage(message)
                .setPositiveButton(
                    "OK"
                ) { dialog, _ -> dialog!!.dismiss() }
                .show()
    }

    private fun clearFields() {
        mAddressEt.setText("")
        mFloorCountEt.setText("")
        mLatitudeEt.setText("")
        mLongitudeEt.setText("")
        mRecyclerViewAdapter.setSelectedImages(ArrayList())
        AddHouseFormState.clearState()
        Keyboard.hide(requireActivity())
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

    private fun handleLocationPermissions() {
        Log.d(TAG, "handlePermissions: ")
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("Information")
                setMessage("To determine your current location you must provide this app to access your current location.\nDo you want to provide location access?")
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: permission to access location is granted")
            getCurrentLocationSettingsAndStartLocationUpdates()
        } else if (requestCode == REQUEST_ACCESS_EXTERNAL_STORAGE && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: permission to access external storage granted")
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(
                intent,
                REQUEST_CODE_SELECT_PHOTOS
            )
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_PHOTOS && resultCode == Activity.RESULT_OK && data != null) {
            var isAllImagesValid = true
            if (data.clipData != null && data.clipData!!.itemCount > 0) {
                mSelectedImagesRVTil.isErrorEnabled = false
                mSelectedImagesList.clear()

                data.clipData?.let {
                    for (i in 0 until it.itemCount) {
                        val roomImageUri = it.getItemAt(i).uri
                        roomImageUri?.let { uri ->
                            if (isAllImagesValid && FileHandler.getFileSize(
                                    uri,
                                    mContext
                                ) > VALID_IMAGE_SIZE
                            ) {
                                isAllImagesValid = false
                            }
                            mSelectedImagesList.add(uri)
                        }
                    }
                    mSelectedImagesRVTil.visibility = View.VISIBLE
                    mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
                }
            } else if (data.data != null) {
                mSelectedImagesList.clear()
                mSelectedImagesList.add(data.data!!)
                if (FileHandler.getFileSize(data.data!!, mContext) > VALID_IMAGE_SIZE) {
                    isAllImagesValid = false
                }

                mSelectedImagesRVTil.visibility = View.VISIBLE
                mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
            } else {
                mSelectedImagesRVTil.visibility = View.GONE
                mRecyclerViewAdapter.setSelectedImages(ArrayList())
            }

            if (!isAllImagesValid) {
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"
                VibrateView.vibrate(mContext, R.anim.shake, mSelectedImagesRVTil)
            } else {
                mSelectedImagesRVTil.isErrorEnabled = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getCurrentLocationSettingsAndStartLocationUpdates() {
        val builder = LocationSettingsRequest.Builder()
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                it.startResolutionForResult(requireActivity(), REQUEST_CODE_CHECK_SETTINGS)
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            handleLocationPermissions()
        }
    }

    private fun stopLocationUpdates() {
        mLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun validate(): Boolean {
        var isValid = true

        mAddressEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mAddressTil.apply {
                    isErrorEnabled = true
                    error = "Address is required"
                }
            }
        }

        mFloorCountEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mFloorCountTil.apply {
                    isErrorEnabled = true
                    error = "Floor count is required"
                }
            }
        }

        mLatitudeEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mLatitudeTil.apply {
                    isErrorEnabled = true
                    error = "Latitude of your house is required"
                }
            }
        }

        mLongitudeEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mLongitudeTil.apply {
                    isErrorEnabled = true
                    error = "Longitude of your house is required"
                }
            }
        }

        if (mSelectedImagesList.size == 0) {
            isValid = false
            mSelectImagesBtnTil.isErrorEnabled = true
            mSelectImagesBtnTil.error = "House image(s) is required"
        } else if (mSelectedImagesList.size > 0) {
            var isAllImagesValid = true
            mSelectedImagesList.forEach {
                if (isAllImagesValid && FileHandler.getFileSize(
                        it,
                        mContext
                    ) > VALID_IMAGE_SIZE
                ) {
                    isAllImagesValid = false
                }
            }
            if (!isAllImagesValid) {
                isValid = false
                mSelectedImagesRVTil.apply {
                    isErrorEnabled = true
                    error = "Image highlighted in red has exceeded the prescribed limit of 2MB"
                }
            }
        }

        if (!isValid) {
            VibrateView.vibrate(
                mContext,
                R.anim.shake,
                requireView().findViewById(R.id.formCardView)
            )
        }

        return isValid
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Log.d(TAG, "onFocusChange: ")
        if (v != null) {
            when (v.id) {
                mAddressEt.id -> {
                    if (!hasFocus) {
                        mAddressEt.text?.let { value ->
                            if (value.trim().isNotEmpty()) {
                                mAddressTil.isErrorEnabled = false
                            } else {
                                mAddressTil.apply {
                                    isErrorEnabled = true
                                    error = "Address is required"
                                }
                                VibrateView.vibrate(mContext, R.anim.shake, mAddressTil)
                            }
                        }
                    } else {
                        mAddressTil.isErrorEnabled = false
                    }
                }

                mFloorCountEt.id -> {
                    if (!hasFocus) {
                        mFloorCountEt.text?.let { value ->
                            if (value.trim().isNotEmpty()) {
                                mFloorCountTil.isErrorEnabled = false
                            } else {
                                mFloorCountTil.apply {
                                    isErrorEnabled = true
                                    error = "Floor count is required"
                                }
                                VibrateView.vibrate(mContext, R.anim.shake, mFloorCountTil)
                            }
                        }
                    } else {
                        mFloorCountTil.isErrorEnabled = false
                    }
                }

                mLatitudeEt.id -> {
                    if (!hasFocus) {
                        mLatitudeEt.text?.let { value ->
                            if (value.trim().isNotEmpty()) {
                                mLatitudeTil.isErrorEnabled = false
                            } else {
                                mLatitudeTil.isErrorEnabled = true
                                mLatitudeTil.error = "Latitude of your house is required"
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mLatitudeTil
                                )
                            }
                        }
                    } else {
                        mLatitudeTil.isErrorEnabled = false
                    }
                }

                mLongitudeEt.id -> {
                    if (!hasFocus) {
                        mLongitudeEt.text?.let { value ->
                            if (value.trim().isNotEmpty()) {
                                mLongitudeTil.isErrorEnabled = false
                            } else {
                                mLongitudeTil.apply {
                                    isErrorEnabled = true
                                    error = "Longitude of your house is required"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mLongitudeTil
                                )

                            }
                        }
                    } else {
                        mLongitudeTil.isErrorEnabled = false
                    }
                }
            }

            Log.d(TAG, "onFocusChange: ${AddHouseFormState.address}")
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        loadFormState()
    }

    private fun loadFormState() {
        Log.d(TAG, "loadOldState: $AddHouseFormState")
        if (AddHouseFormState.shouldLoadState) {
            AddHouseFormState.address?.let {
                mAddressEt.setText(it)
            }

            AddHouseFormState.floorCount?.let {
                mFloorCountEt.setText(it.toString())
            }

            AddHouseFormState.longitude?.let {
                mLongitudeEt.setText(it.toString())
            }

            AddHouseFormState.latitude?.let {
                mLatitudeEt.setText(it.toString())
            }

            AddHouseFormState.houseImages?.let {
                mSelectedImagesList = it
                mSelectedImagesRVTil.visibility = View.VISIBLE
                mRecyclerViewAdapter.setSelectedImages(it)
            }
            AddHouseFormState.clearState()
        }
    }

    private fun saveFormState() {
        AddHouseFormState.shouldLoadState = true
        mAddressEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                AddHouseFormState.address = value.trim().toString()
            }
        }

        mFloorCountEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                AddHouseFormState.floorCount = value.trim().toString().toInt()
            }
        }

        mLatitudeEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                AddHouseFormState.latitude = value.trim().toString().toDouble()
            }
        }

        mLongitudeEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                AddHouseFormState.longitude = value.trim().toString().toDouble()
            }
        }

        if (mSelectedImagesList.size > 0) {
            AddHouseFormState.houseImages = mSelectedImagesList
        }
    }

    override fun onDeleteClick(position: Int) {
        mSelectedImagesList.removeAt(position)
        mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)

        if (mSelectedImagesList.size > 0) {
            var isAllImagesValid = true

            mSelectedImagesList.forEach {
                if (isAllImagesValid && FileHandler.getFileSize(
                        it,
                        mContext
                    ) > VALID_IMAGE_SIZE
                ) {
                    isAllImagesValid = false
                    return
                }
            }

            if (!isAllImagesValid) {
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"
            } else {
                mSelectedImagesRVTil.isErrorEnabled = false
            }
        } else {
            mSelectedImagesRVTil.visibility = View.GONE
        }
    }
}