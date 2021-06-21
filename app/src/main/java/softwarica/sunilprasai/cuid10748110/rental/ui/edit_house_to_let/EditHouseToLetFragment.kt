package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_to_let

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.TENANCY_ROOM_TYPE
import softwarica.sunilprasai.cuid10748110.rental.model.TO_LET_ROOM_TYPE
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.CurrentImagesRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.SelectedImagesRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val TAG = "EditToLetFragment"
private const val REQUEST_ACCESS_EXTERNAL_STORAGE: Int = 3001
private const val REQUEST_CODE_SELECT_PHOTOS: Int = 3002
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val ARG_PARAM_TO_LET_POSITION = "toLetPosition"
private const val ARG_PARAM_TO_LET_ID = "toLetId"

class EditHouseToLetFragment : Fragment(), View.OnClickListener,
    View.OnFocusChangeListener, SelectedImagesRVA.OnSelectedImageDeleteClickListener {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null
    private var mParamToLetPosition: Int = -1
    private var mParamToLetID: String? = null
    private lateinit var mViewModel: HouseViewModel

    private lateinit var mRoomTypeTil: TextInputLayout
    private lateinit var mRoomTypeSpn: Spinner

    private lateinit var mRoomCountTil: TextInputLayout
    private lateinit var mRoomCountEt: TextInputEditText

    private lateinit var mRentAmountTil: TextInputLayout
    private lateinit var mRentAmountEt: TextInputEditText

    private lateinit var mRoomFacilitiesTil: TextInputLayout
    private lateinit var mRoomFacilitiesEt: TextInputEditText

    private lateinit var mSelectImagesTil: TextInputLayout
    private lateinit var mSelectImagesBtn: MaterialButton

    private lateinit var mSelectedImagesRVTil: TextInputLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: SelectedImagesRVA
    private lateinit var mRecyclerViewAdapterOfCurrentImages: CurrentImagesRVA

    private lateinit var mUpdateToLetBtn: MaterialButton

    private var mSelectedImagesList: ArrayList<Uri> = ArrayList()

    private val mRoomTypeSpnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position > 0) {
                mRoomTypeTil.isErrorEnabled = false
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            mRoomTypeTil.apply {
                isErrorEnabled = false
                error = "Tenancy room type is required"
            }
        }
    }
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamToLetPosition = it.getInt(ARG_PARAM_TO_LET_POSITION)
            mParamToLetID = it.getString(ARG_PARAM_TO_LET_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_house_to_let, container, false)
        mContext = requireContext()
        mRoomTypeTil = view.findViewById(R.id.roomTypeTil)
        mRoomTypeSpn = view.findViewById(R.id.roomTypeSpn)
        mRoomTypeSpn.onItemSelectedListener = mRoomTypeSpnItemSelectedListener

        mRoomCountTil = view.findViewById(R.id.roomCountTil)
        mRoomCountEt = view.findViewById(R.id.roomCountEt)
        mRoomCountEt.onFocusChangeListener = this

        mRentAmountTil = view.findViewById(R.id.rentAmountTil)
        mRentAmountEt = view.findViewById(R.id.rentAmountEt)
        mRentAmountEt.onFocusChangeListener = this

        mRoomFacilitiesTil = view.findViewById(R.id.roomFacilitiesTil)
        mRoomFacilitiesEt = view.findViewById(R.id.roomFacilitiesEt)
        mRoomFacilitiesEt.onFocusChangeListener = this

        mSelectImagesTil = view.findViewById(R.id.selectImagesTil)
        mSelectImagesBtn = view.findViewById(R.id.selectImagesBtn)
        mSelectImagesBtn.setOnClickListener(this)

        mSelectedImagesRVTil = view.findViewById(R.id.selectedImagesRVTil)

        mSelectedImagesRVTil = view.findViewById(R.id.selectedImagesRVTil)
        mRecyclerView = view.findViewById(R.id.selectedImagesRV)
        mRecyclerViewAdapter = SelectedImagesRVA()
        mRecyclerViewAdapterOfCurrentImages = CurrentImagesRVA()
        mRecyclerViewAdapter.setOnSelectedImageDeleteClickListener(this)
        mRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mRecyclerView.adapter = mRecyclerViewAdapterOfCurrentImages

        mUpdateToLetBtn = view.findViewById(R.id.updateToLetBtn)
        mUpdateToLetBtn.setOnClickListener(this)

        val mRoomTypeSpnItemsAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            mContext, R.layout.support_simple_spinner_dropdown_item, TENANCY_ROOM_TYPE
        ) {
            override fun isEnabled(position: Int): Boolean {
                return if (position == 0) {
                    false
                } else {
                    super.isEnabled(position)
                }
            }

            override fun areAllItemsEnabled(): Boolean = false
        }
        mRoomTypeSpn.adapter = mRoomTypeSpnItemsAdapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)
        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (mParamHousePosition != -1 && mParamToLetPosition != -1 && mParamHousePosition < it.size && mParamToLetPosition < it[mParamHousePosition].toLets!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].toLets!![mParamToLetPosition].id == mParamToLetID) {
                    val toLet = it[mParamHousePosition].toLets!![mParamToLetPosition]
                    updateView(toLet)
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateView(toLet: ToLet) {
        mRentAmountEt.setText(toLet.amount.toString())
        mRoomCountEt.setText(toLet.roomCount.toString())

        TO_LET_ROOM_TYPE.mapIndexed { index, rt ->
            if (toLet.roomType.toString() == rt) {
                Log.d(TAG, "updateView: ${index}")
                mRoomTypeSpn.setSelection(index)
            }
        }

        val facilities = StringBuilder()
        toLet.facilities!!.mapIndexed { index, facility ->
            if (index < toLet.facilities!!.size - 1)
                facilities.append(facility).append(", ")
            else
                facilities.append(facility)
        }
        mRoomFacilitiesEt.setText(facilities.toString())
        mRecyclerViewAdapterOfCurrentImages.setSelectedImages(toLet.images!!)
    }

    companion object {
        @JvmStatic
        fun newInstance(housePosition: Int, houseID: String, toLetPosition: Int, toLetID: String) =
            EditHouseToLetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseID)
                    putInt(ARG_PARAM_TO_LET_POSITION, toLetPosition)
                    putString(ARG_PARAM_TO_LET_ID, toLetID)
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
                mRecyclerView.adapter = mRecyclerViewAdapter
                mSelectedImagesRVTil.isErrorEnabled = false
                mSelectImagesTil.isErrorEnabled = false
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
                mSelectedImagesRVTil.isErrorEnabled = false
                mSelectImagesTil.isErrorEnabled = false
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
                mSelectImagesTil.apply {
                    isErrorEnabled = true
                    error = "No image(s) selected"
                }
            }

            if (!isAllImagesValid) {
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"

                VibrateView.vibrate(
                    mContext,
                    R.anim.shake,
                    mSelectedImagesRVTil
                )
            } else {
                mSelectedImagesRVTil.isErrorEnabled = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
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

                mUpdateToLetBtn.id -> {
                    if (validate()) {
                        val toLet = ToLet().apply {
                            roomType =
                                ToLet.getRoomType(TO_LET_ROOM_TYPE[mRoomTypeSpn.selectedItemPosition])
                            roomCount = mRoomCountEt.text!!.toString().toInt()
                            amount = mRentAmountEt.text!!.toString().toInt()
                            facilities = mRoomFacilitiesEt.text.toString().split(",")
                                .map { facility -> facility.trim() } as ArrayList<String>
                            house = mViewModel.getHouse(mParamHousePosition)
                        }
                        updateToLet(toLet)
                    }
                }
            }
        }
    }

    private fun updateToLet(toLet: ToLet) {
        val requestBody = HashMap<String, RequestBody>().apply {
            put(ToLet.AMOUNT, RequestBody.create(MultipartBody.FORM, toLet.amount.toString()))
            put(
                ToLet.ROOM_COUNT,
                RequestBody.create(MultipartBody.FORM, toLet.roomCount.toString())
            )
            put(ToLet.ROOM_TYPE, RequestBody.create(MultipartBody.FORM, toLet.roomType.toString()))
            toLet.facilities!!.let {
                val jsonArray = JSONArray()
                for (i in 0 until it.size) {
                    jsonArray.put(it[i])
                }
                put(
                    ToLet.FACILITIES,
                    RequestBody.create(MultipartBody.FORM, jsonArray.toString())
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
        val updateToLet = consumer.updateToLet(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamToLetID!!,
            images,
            requestBody
        )
        updateToLet.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val toLetResponse = ToLet(JSONObject(response.body()!!.string()))
                    mViewModel.replaceToLet(mParamHousePosition, mParamToLetPosition, toLetResponse)
                    AppToast.show(
                        mContext,
                        "To-let has been updated successfully",
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
                        displayAPIErrorResponseDialog("Unable to update to-let. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update to-let. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update to-let. Please try again later")
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

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                mRoomCountEt.id -> {
                    if (!hasFocus) {
                        mRoomCountEt.text?.let {
                            when {
                                it.trim().isEmpty() -> {
                                    mRoomCountTil.apply {
                                        isErrorEnabled = true
                                        error = "Total rooms being offered for tenancy is required"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mRoomCountTil
                                    )
                                }
                                !it.trim().isDigitsOnly() -> {
                                    mRoomCountTil.apply {
                                        isErrorEnabled = true
                                        error = "Room count takes numeric value only"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mRoomCountTil
                                    )
                                }
                            }
                        }
                    } else {
                        mRoomCountTil.isErrorEnabled = false
                    }
                }

                mRentAmountEt.id -> {
                    if (!hasFocus) {
                        mRentAmountEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mRentAmountTil.apply {
                                    isErrorEnabled = true
                                    error =
                                        "Rent amount of rooms being offered for tenancy is required"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mRentAmountTil
                                )
                            } else if (!it.trim().isDigitsOnly()) {
                                mRentAmountTil.apply {
                                    isErrorEnabled = true
                                    error = "Rent amount takes numeric value only"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mRentAmountTil
                                )
                            }
                        }
                    } else {
                        mRentAmountTil.isErrorEnabled = false
                    }
                }

                mRoomFacilitiesEt.id -> {
                    if (!hasFocus) {
                        mRoomFacilitiesEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mRoomFacilitiesTil.apply {
                                    isErrorEnabled = true
                                    error =
                                        "Facilities available along with tenancy offered is required"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mRoomFacilitiesTil
                                )
                            }
                        }
                    } else {
                        mRoomFacilitiesTil.isErrorEnabled = false
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {

        var isValid = true

        mRoomCountEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mRoomCountTil.apply {
                        isErrorEnabled = true
                        error = "Total rooms being offered for tenancy is required"
                    }
                }
                !it.trim().isDigitsOnly() -> {
                    isValid = false
                    mRoomCountTil.apply {
                        isErrorEnabled = true
                        error = "Room count takes numeric value only"
                    }
                }
                else -> {
                    mRoomCountTil.isErrorEnabled = false
                }
            }
        }

        mRentAmountEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mRentAmountTil.apply {
                        isErrorEnabled = true
                        error = "Rent amount of rooms being offered for tenancy is required"
                    }
                }
                !it.trim().isDigitsOnly() -> {
                    isValid = false
                    mRentAmountTil.apply {
                        isErrorEnabled = true
                        error = "Rent amount takes numeric value only"
                    }
                }
                else -> {
                    mRentAmountTil.isErrorEnabled = false
                }
            }
        }

        mRoomTypeSpn.let {
            when (it.selectedItemPosition) {
                0 -> {
                    isValid = false
                    mRoomTypeTil.apply {
                        isErrorEnabled = true
                        error =
                            "Weather tenancy being offered is for whole flat or for sharing with others is required"
                    }
                }
                else -> {
                    mRoomTypeTil.isErrorEnabled = false
                }
            }
        }

        mRoomFacilitiesEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mRoomFacilitiesTil.apply {
                        isErrorEnabled = true
                        error = "Facilities available along with tenancy offered is required"
                    }
                }
                else -> {
                    mRoomFacilitiesTil.isErrorEnabled = false
                }
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
        } else {
            mSelectImagesTil.apply {
                isErrorEnabled = true
                error = "No image(s) selected"
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
            mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
        }
    }
}