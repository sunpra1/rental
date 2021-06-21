package softwarica.sunilprasai.cuid10748110.rental.ui.select_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.form_state.AddHouseFormState
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast


private const val TAG = "SelectLocationFragment"

private const val REQUEST_CHECK_SETTINGS: Int = 1000
private const val REQUEST_ACCESS_FINE_LOCATION: Int = 1001

class SelectLocationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMapView: MapView
    private var mMap: GoogleMap? = null
    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private var mUserLocation: LatLng? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            mMap?.let { googleMap ->
                p0?.lastLocation?.let { lastLocation ->
                    Log.d(TAG, "onLocationResult: $lastLocation")

                    val currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude)

                    if (mUserLocation != currentLocation) {
                        googleMap.clear()
                        googleMap.addMarker(
                            MarkerOptions().position(currentLocation).title("You'r here")
                        )
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation,
                                18.0f
                            )
                        )
                        mUserLocation = currentLocation
                        Log.d(TAG, "onLocationResult: user location updated")
                    }
                }
            }
        }
    }
    private lateinit var mContext: Context

    @SuppressLint("VisibleForTests")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_select_location, container, false)
        mContext = requireContext()
        mMapView = view.findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)

        mLocationClient = FusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        mMap = googleMap.apply {
            setOnMapClickListener(this@SelectLocationFragment)
            setOnMapLongClickListener(this@SelectLocationFragment)
            mapType = GoogleMap.MAP_TYPE_HYBRID
            isBuildingsEnabled = true
            setOnMyLocationButtonClickListener(this@SelectLocationFragment)
            if (ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isMyLocationEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            menu.removeItem(R.id.getCurrentLocationOptionMenu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.getCurrentLocationOptionMenu -> {
                handlePermissions()
                true
            }

            R.id.setCurrentLocationOptionMenu -> {
                mUserLocation?.let {
                    AddHouseFormState.latitude = it.latitude
                    AddHouseFormState.longitude = it.longitude
                    requireActivity().supportFragmentManager.popBackStack()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handlePermissions() {
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocationSettings()
        }
    }

    private fun getCurrentLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                it.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
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
        }
    }

    private fun stopLocationUpdates() {
        mLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapClick(p0: LatLng?) {
        Log.d(TAG, "onMapClick: ${p0.toString()}")
        p0?.let { latLang ->
            mUserLocation?.let {
                val results = FloatArray(1)
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    latLang.latitude,
                    latLang.longitude,
                    results
                )
                Log.d(TAG, "onMapClick: ${results[0]}")
            }

            mMap?.let {
                it.clear()
                it.addMarker(MarkerOptions().position(latLang).title("You'r here"))
                it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLang, 18.0f))
                mUserLocation = p0
                stopLocationUpdates()
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        requireActivity().invalidateOptionsMenu()
        mMapView.onResume()
        mMapView.getMapAsync(this)
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.d(TAG, "onMyLocationButtonClick: ")
        startLocationUpdates()
        return true
    }

    override fun onMapLongClick(p0: LatLng?) {
        mMap?.let {
            if (isAdded)
                AppToast.show(
                    requireContext(),
                    "Please long press on map to toggle the map view from Hybrid and Terran",
                    Toast.LENGTH_LONG
                )
            it.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }
}