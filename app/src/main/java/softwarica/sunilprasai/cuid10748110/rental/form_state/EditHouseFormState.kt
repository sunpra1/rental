package softwarica.sunilprasai.cuid10748110.rental.form_state

import android.net.Uri

object EditHouseFormState {
    var shouldLoadState: Boolean = false
    var address: String? = null
    var floorCount: Int? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var images: ArrayList<Uri>? = null

    fun clearState() {
        shouldLoadState = false
        address = null
        floorCount = null
        longitude = null
        latitude = null
        images = null
    }

    override fun toString(): String {
        return "{ shouldLoadState: $shouldLoadState, address: $address, floorCount: $floorCount, latitude: $latitude, longitude: $longitude, houseImage: $images }"
    }
}