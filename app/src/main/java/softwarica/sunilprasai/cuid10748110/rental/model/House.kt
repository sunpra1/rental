package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "House"

class House() {
    var id: String? = null
    var address: String? = null
    var location: Location? = null
    var floors: Int? = null
    var images: ArrayList<Image>? = null
    var owner: User? = null
    var tenancies: ArrayList<Tenancy>? = null
    var tenanciesHistory: ArrayList<Tenancy>? = null
    var toLets: ArrayList<ToLet>? = null

    constructor(house: House) : this() {
        id = house.id
        address = house.address
        location = house.location?.let { Location(it) }
        floors = house.floors
        images = house.images
        owner = house.owner?.let { User(it) }
        tenancies = house.tenancies?.let { ArrayList(it) }
        tenanciesHistory = house.tenanciesHistory?.let { ArrayList(it) }
        toLets = house.toLets?.let { ArrayList(it) }
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            id =
                if (jsonObject.has(ID) && !jsonObject.isNull(ID)) {
                    jsonObject.getString(ID)
                } else throw InvalidParameterException(
                    "House must contain id parameter"
                )

            address = if (jsonObject.has(ADDRESS) && !jsonObject.isNull(ADDRESS)) {
                jsonObject.getString(ADDRESS)
            } else throw InvalidParameterException(
                "House must contain address parameter"
            )

            floors =
                if (jsonObject.has(FLOORS) && !jsonObject.isNull(FLOORS))
                    jsonObject.getInt(FLOORS)
                else throw InvalidParameterException(
                    "House must contain floors parameter"
                )

            location =
                if (jsonObject.has(LOCATION) && !jsonObject.isNull(LOCATION)) {
                    Location(jsonObject.getJSONObject(LOCATION))
                } else throw InvalidParameterException(
                    "House must contain location parameter"
                )

            images =
                if (jsonObject.has(IMAGES) && !jsonObject.isNull(IMAGES)) {
                    Image.getImagesListFromJSONArray(jsonObject.getJSONArray(IMAGES))
                } else throw InvalidParameterException(
                    "House must contain images parameter"
                )

            owner =
                if (jsonObject.has(OWNER) && !jsonObject.isNull(OWNER)) {
                    try {
                        User(jsonObject.getJSONObject(OWNER))
                    } catch (e: JSONException) {
                        User().apply {
                            id = jsonObject.getString(OWNER)
                        }
                    }
                } else
                    throw InvalidParameterException("House must contain owner parameter")

            tenancies =
                if (jsonObject.has(TENANCIES) && !jsonObject.isNull(TENANCIES)) {
                    Tenancy.getTenancyArrayListFromJSONArray(
                        jsonObject.getJSONArray(
                            TENANCIES
                        )
                    )
                } else ArrayList()

            tenanciesHistory =
                if (jsonObject.has(TENANCIES_HISTORY) && !jsonObject.isNull(TENANCIES_HISTORY)) {
                    Tenancy.getTenancyArrayListFromJSONArray(
                        jsonObject.getJSONArray(
                            TENANCIES_HISTORY
                        )
                    )
                } else ArrayList()

            toLets =
                if (jsonObject.has(TO_LETS) && !jsonObject.isNull(TO_LETS)) {
                    ToLet.getToLetArrayListFromJSONArray(
                        jsonObject.getJSONArray(
                            TO_LETS
                        )
                    )
                } else ArrayList()
        } catch (e: JSONException) {
            Log.d(TAG, "constructor: ${e.localizedMessage}")
        }
    }

    companion object {
        @JvmStatic
        val ID = "_id"

        @JvmStatic
        val ADDRESS = "address"

        @JvmStatic
        val LOCATION = "location"

        @JvmStatic
        val FLOORS = "floors"

        @JvmStatic
        val IMAGES = "images"

        @JvmStatic
        val OWNER = "owner"

        @JvmStatic
        val TENANCIES = "tenancies"

        @JvmStatic
        val TENANCIES_HISTORY = "tenanciesHistory"

        @JvmStatic
        val TO_LETS = "toLets"

        @JvmStatic
        fun getHousesArrayListFromJSONArray(jsonArray: JSONArray): ArrayList<House> {
            val housesArrayList: ArrayList<House> = ArrayList()
            try {
                for (i in 0 until jsonArray.length()) {
                    try {
                        housesArrayList.add(House(jsonArray.getJSONObject(i)))
                    } catch (e: JSONException) {
                        housesArrayList.add(House().apply {
                            id = jsonArray.getString(i)
                        })
                    }
                }
            } catch (e: JSONException) {
                Log.d(TAG, "getHousesArrayListFromJSONArray: ${e.localizedMessage}")
            }
            return housesArrayList
        }
    }
}