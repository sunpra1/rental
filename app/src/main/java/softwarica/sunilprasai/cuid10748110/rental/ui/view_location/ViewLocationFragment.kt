package softwarica.sunilprasai.cuid10748110.rental.ui.view_location

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast

private const val TAG = "ViewLocationFragment"
private const val ARG_PARAM_LATITUDE = "latitude"
private const val ARG_PARAM_LONGITUDE = "longitude"

class ViewLocationFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMapLongClickListener {
    private var mParamLatitude: Double? = null
    private var mParamLongitude: Double? = null

    private lateinit var mMapView: MapView
    private var mMap: GoogleMap? = null
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamLatitude = it.getDouble(ARG_PARAM_LATITUDE)
            mParamLongitude = it.getDouble(ARG_PARAM_LONGITUDE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_view_location, container, false)
        mContext = requireContext()
        mMapView = view.findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        mMap = googleMap.apply {
            setOnMapLongClickListener(this@ViewLocationFragment)
            mapType = GoogleMap.MAP_TYPE_HYBRID
            isBuildingsEnabled = true
        }
        locateHouse()
    }

    private fun locateHouse() {
        if (mParamLatitude != null && mParamLongitude != null) {
            mMap?.let {
                val currentLocation = LatLng(mParamLatitude!!, mParamLongitude!!)
                it.addMarker(
                    MarkerOptions().position(currentLocation).title("House is here")
                )
                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLocation,
                        18.0f
                    )
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.getHouseLocationOptionMenu) {
            locateHouse()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(latitude: Double, longitude: Double) =
            ViewLocationFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_PARAM_LATITUDE, latitude)
                    putDouble(ARG_PARAM_LONGITUDE, longitude)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        mMapView.getMapAsync(this)
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onMapLongClick(p0: LatLng?) {
        mMap?.let {
            if (isAdded)
                AppToast.show(
                    mContext,
                    "Please long press on map to toggle the map view from Hybrid and Terran",
                    Toast.LENGTH_LONG
                )
            it.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }
}