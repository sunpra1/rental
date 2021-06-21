package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM_dd
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ToLet"

val TO_LET_ROOM_TYPE = arrayListOf(
    "SELECT ROOM TYPE",
    "FLAT",
    "SHARING"
)

class ToLet() {
    var id: String? = null
    var roomType: RoomType? = null
    var roomCount: Int? = null
    var amount: Int? = null
    var images: ArrayList<Image>? = null
    var facilities: ArrayList<String>? = null
    var house: House? = null
    var createdAt: Date? = null

    constructor(jsonObject: JSONObject) : this() {
        try {
            id =
                if (jsonObject.has(Tenancy.ID) && !jsonObject.isNull(ID)) jsonObject.getString(
                    ID
                ) else throw InvalidParameterException("ToLet must contain id")

            roomType =
                if (jsonObject.has(ROOM_TYPE) && !jsonObject.isNull(ROOM_TYPE)) getRoomType(
                    jsonObject.getString(ROOM_TYPE)
                ) else throw InvalidParameterException("ToLet must contain room type")

            roomCount =
                if (jsonObject.has(ROOM_COUNT) && !jsonObject.isNull(ROOM_COUNT)) jsonObject.getInt(
                    ROOM_COUNT
                ) else throw InvalidParameterException("ToLet must contain roomCount parameter")

            amount =
                if (jsonObject.has(AMOUNT) && !jsonObject.isNull(AMOUNT)) jsonObject.getInt(
                    AMOUNT
                ) else throw InvalidParameterException("ToLet must contain amount parameter")

            facilities = if (jsonObject.has(FACILITIES) && !jsonObject.isNull(FACILITIES)) {
                val jsonArray = jsonObject.getJSONArray(FACILITIES)
                val facilitiesArray: ArrayList<String> = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    facilitiesArray.add(jsonArray.getString(i))
                }
                facilitiesArray
            } else throw InvalidParameterException("ToLet must contain facilities parameter")

            images =
                if (jsonObject.has(IMAGES) && !jsonObject.isNull(IMAGES)) {
                    Image.getImagesListFromJSONArray(jsonObject.getJSONArray(IMAGES))
                } else throw InvalidParameterException(
                    "To-let must contain images parameter"
                )

            house = if (jsonObject.has(HOUSE) && !jsonObject.isNull(HOUSE)) {
                try {
                    House(
                        jsonObject.getJSONObject(
                            HOUSE
                        )
                    )
                } catch (e: JSONException) {
                    House().apply {
                        id = jsonObject.getString(HOUSE)
                    }
                }
            } else throw InvalidParameterException("ToLet must contain house parameter")

            createdAt = if (jsonObject.has(CREATED_AT) && !jsonObject.isNull(CREATED_AT)) {
                SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).parse(
                    jsonObject.getString(
                        CREATED_AT
                    )
                )
            } else throw InvalidParameterException("ToLet must contain createdAt parameter")

        } catch (e: JSONException) {
            Log.e(TAG, "ToLet Constructor: ${e.localizedMessage}")
        }
    }

    fun getJSONObject(): JSONObject {
        return JSONObject().apply {
            roomType?.let {
                put(ROOM_TYPE, it)
            }
            roomCount?.let {
                put(ROOM_COUNT, it)
            }
            amount?.let {
                put(AMOUNT, it)
            }
            facilities?.let {
                val jsonArray = JSONArray()
                for (i in 0 until it.size) {
                    jsonArray.put(it[i])
                }
                put(FACILITIES, jsonArray)
            }
        }
    }

    companion object {
        @JvmStatic
        val ID: String = "_id"

        @JvmStatic
        val ROOM_TYPE = "roomType"

        @JvmStatic
        val ROOM_COUNT = "roomCount"

        @JvmStatic
        val AMOUNT = "amount"

        @JvmStatic
        val IMAGES = "images"

        @JvmStatic
        val FACILITIES = "facilities"

        @JvmStatic
        val HOUSE = "house"

        @JvmStatic
        val CREATED_AT = "createdAt"

        @JvmStatic
        fun getRoomType(roomType: String): RoomType {
            return when (roomType) {
                "FLAT" -> RoomType.FLAT
                "END" -> RoomType.SHARING
                else -> RoomType.SHARING
            }
        }

        @JvmStatic
        fun getToLetArrayListFromJSONArray(jsonArray: JSONArray): ArrayList<ToLet> {
            val toLet = ArrayList<ToLet>()
            try {
                for (i in 0 until jsonArray.length()) {
                    try {
                        toLet.add(ToLet(jsonArray.getJSONObject(i)))
                    } catch (e: JSONException) {
                        toLet.add(ToLet().apply {
                            id = jsonArray.getString(i)
                        })
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, "getToLetArrayListFromJSONArray: ${e.localizedMessage}")
            }
            return toLet
        }
    }

    enum class RoomType {
        FLAT, SHARING
    }
}