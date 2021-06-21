package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house

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
import softwarica.sunilprasai.cuid10748110.rental.form_state.EditHouseFormState
import softwarica.sunilprasai.cuid10748110.rental.model.House
import softwarica.sunilprasai.cuid10748110.rental.model.Location
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.select_location.SelectLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val ARG_PARAM_HOUSE_POSITION = "position"
private const val ARG_PARAM_HOUSE_ID = "id"
private const val TAG = "EditHouseFragment"
private const val REQUEST_CODE_CHECK_SETTINGS: Int = 2000
private const val REQUEST_ACCESS_FINE_LOCATION: Int = 2001
private const val REQUEST_ACCESS_EXTERNAL_STORAGE: Int = 2002
private const val REQUEST_CODE_SELECT_PHOTOS: Int = 2003

class EditHouseFragment private constructor() : Fragment(), View.OnClickListener,
    View.OnFocusChangeListener, SelectedImagesRVA.OnSelectedImageDeleteClickListener {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

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

    private lateinit var mUpdateImagesBtn: MaterialButton
    private lateinit var mSelectedImagesRVTil: TextInputLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapterOfSelectedImages: SelectedImagesRVA
    private lateinit var mRecyclerViewAdapterOfCurrentImages: CurrentImagesRVA

    private lateinit var mUpdateBtn: MaterialButton

    private lateinit var mViewModel: HouseViewModel

    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private var mUserLocation: LatLng? = null
    private var mSelectedImagesList: ArrayList<Uri> = ArrayList()
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
                    EditHouseFormState.latitude = mUserLocation?.latitude
                    EditHouseFormState.longitude = mUserLocation?.longitude
                } else {
                    stopLocationUpdates()
                }
            }
        }
    }
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
        }
    }

    @SuppressLint("VisibleForTests")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_house, container, false)
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

        mUpdateImagesBtn = view.findViewById(R.id.updateImagesBtn)
        mUpdateImagesBtn.setOnClickListener(this)

        mSelectedImagesRVTil = view.findViewById(R.id.selectedImagesRVTil)
        mRecyclerView = view.findViewById(R.id.selectedImagesRV)
        mRecyclerViewAdapterOfSelectedImages = SelectedImagesRVA()
        mRecyclerViewAdapterOfCurrentImages = CurrentImagesRVA()
        mRecyclerViewAdapterOfSelectedImages.setOnSelectedImageDeleteClickListener(this)
        mRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mRecyclerView.adapter = mRecyclerViewAdapterOfCurrentImages

        mUpdateBtn = view.findViewById(R.id.updateHouseBtn)
        mUpdateBtn.setOnClickListener(this)

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

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamHousePosition < it.size && it[mParamHousePosition].id == mParamHouseID) {
                    updateView(it[mParamHousePosition])
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateView(house: House) {
        house.address?.let {
            mAddressEt.setText(it)
        }

        house.floors?.let {
            mFloorCountEt.setText(it.toString())
        }

        house.location?.let {
            mLatitudeEt.setText(it.latitude.toString())
            mLongitudeEt.setText(it.longitude.toString())
        }

        mRecyclerViewAdapterOfCurrentImages.setSelectedImages(house.images!!)
    }

    private fun loadFormState() {
        Log.d(TAG, "loadOldState: $EditHouseFormState")
        if (EditHouseFormState.shouldLoadState) {
            EditHouseFormState.address?.let {
                mAddressEt.setText(it)
            }

            EditHouseFormState.floorCount?.let {
                mFloorCountEt.setText(it.toString())
            }

            EditHouseFormState.longitude?.let {
                mLongitudeEt.setText(it.toString())
            }

            EditHouseFormState.latitude?.let {
                mLatitudeEt.setText(it.toString())
            }

            EditHouseFormState.images?.let {
                mRecyclerViewAdapterOfSelectedImages.setSelectedImages(it)
            }
            EditHouseFormState.clearState()
        }
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

                mUpdateImagesBtn.id -> {
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

                mUpdateBtn.id -> {
                    if (validate()) {
                        val oldHouse = mViewModel.getHouses().value!![mParamHousePosition]

                        val updatedHouse = House().apply {
                            address = mAddressEt.text?.trim().toString()
                            floors = mFloorCountEt.text?.trim().toString().toInt()
                            location = Location().apply {
                                latitude = mLatitudeEt.text?.trim().toString().toDouble()
                                longitude = mLongitudeEt.text?.trim().toString().toDouble()
                            }
                        }

                        updateHouse(oldHouse, updatedHouse, mParamHousePosition)
                    }
                }
            }
        }
    }

    private fun updateHouse(oldHouse: House, updatedHouse: House, position: Int) {
        if (AppState.loggedInUser != null) {
            val requestBody = HashMap<String, RequestBody>().apply {
                updatedHouse.address?.let {
                    put(
                        House.ADDRESS,
                        RequestBody.create(MultipartBody.FORM, updatedHouse.address!!)
                    )
                }

                updatedHouse.floors?.let {
                    put(
                        House.FLOORS,
                        RequestBody.create(
                            MultipartBody.FORM,
                            updatedHouse.floors!!.toString()
                        )
                    )
                }

                updatedHouse.location?.let {
                    put(
                        House.LOCATION,
                        RequestBody.create(
                            MultipartBody.FORM,
                            updatedHouse.location!!.getJSONObject().toString()
                        )
                    )
                }
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

            val consumer = APIService.getService(APIConsumer::class.java)
            val updateHouse = consumer.updateHouse(
                UserToken.getInstance(mContext).token!!,
                oldHouse.id!!,
                images,
                requestBody
            )
            updateHouse.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        clearFields()
                        val updatedHouseResponse = House(JSONObject(response.body()!!.string()))
                        mViewModel.replaceHouse(position, updatedHouseResponse)
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
                                    displayAPIErrorResponseDialog(errorMessage.toString())
                            }
                        } else {
                            displayAPIErrorResponseDialog("Unable to update house details. Please try again later")
                        }
                    } else {
                        displayAPIErrorResponseDialog("Unable to update house details. Please try again later")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    displayAPIErrorResponseDialog("Unable to update house details. Please try again later")
                }

                private fun displayAPIErrorResponseDialog(message: String) {
                    AlertDialog.Builder(mContext)
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


    private fun clearFields() {
        mParamHousePosition = -1
        mParamHouseID = null
        mAddressEt.setText("")
        mFloorCountEt.setText("")
        mLatitudeEt.setText("")
        mLongitudeEt.setText("")
        mRecyclerViewAdapterOfSelectedImages.setSelectedImages(ArrayList())
        mRecyclerViewAdapterOfCurrentImages.setSelectedImages(ArrayList())
        EditHouseFormState.clearState()
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
                mRecyclerView.adapter = mRecyclerViewAdapterOfSelectedImages
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
                    mRecyclerViewAdapterOfSelectedImages.setSelectedImages(mSelectedImagesList)
                }
            } else if (data.data != null) {
                mSelectedImagesList.clear()
                mSelectedImagesList.add(data.data!!)

                if (FileHandler.getFileSize(data.data!!, mContext) > VALID_IMAGE_SIZE) {
                    isAllImagesValid = false
                }

                mSelectedImagesRVTil.visibility = View.VISIBLE
                mRecyclerViewAdapterOfSelectedImages.setSelectedImages(mSelectedImagesList)
            } else {
                mSelectedImagesRVTil.visibility = View.GONE
                mRecyclerViewAdapterOfSelectedImages.setSelectedImages(ArrayList())
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
                mAddressTil.isErrorEnabled = true
                mAddressTil.error = "Address is required"
            }
        }

        mFloorCountEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mFloorCountTil.isErrorEnabled = true
                mFloorCountTil.error = "Floor count is required"
            }
        }

        mLatitudeEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mLatitudeTil.isErrorEnabled = true
                mLatitudeTil.error = "Latitude of your house is required"
            }
        }

        mLongitudeEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mLongitudeTil.isErrorEnabled = true
                mLongitudeTil.error = "Longitude of your house is required"
            }
        }

        if (mSelectedImagesList.size > 0) {
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
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        loadFormState()
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
                                mAddressTil.isErrorEnabled = true
                                mAddressTil.error = "Address is required"
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
                                mFloorCountTil.isErrorEnabled = true
                                mFloorCountTil.error = "Floor count is required"
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
                                //remembering the state
                                EditHouseFormState.longitude = value.trim().toString().toDouble()
                                mLongitudeTil.isErrorEnabled = false
                            } else {
                                mLongitudeTil.isErrorEnabled = true
                                mLongitudeTil.error = "Longitude of your house is required"
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

            Log.d(TAG, "onFocusChange: ${EditHouseFormState.address}")
        }

    }

    private fun saveFormState() {
        EditHouseFormState.shouldLoadState = true
        mAddressEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                EditHouseFormState.address = value.trim().toString()
            }
        }

        mFloorCountEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                EditHouseFormState.floorCount = value.trim().toString().toInt()
            }
        }

        mLatitudeEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                EditHouseFormState.latitude = value.trim().toString().toDouble()
            }
        }

        mLongitudeEt.text?.let { value ->
            if (value.trim().isNotEmpty()) {
                EditHouseFormState.longitude = value.trim().toString().toDouble()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int, id: String) =
            EditHouseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, position)
                    putString(ARG_PARAM_HOUSE_ID, id)
                }
            }
    }

    override fun onDeleteClick(position: Int) {
        mSelectedImagesList.removeAt(position)
        mRecyclerViewAdapterOfSelectedImages.notifyItemRemoved(position)

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
    }
}