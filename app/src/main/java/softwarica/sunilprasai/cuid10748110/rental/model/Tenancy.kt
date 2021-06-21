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

private const val TAG = "Tenancy"
val TENANCY_RENT_ACCRUES_AT: ArrayList<String> = arrayListOf(
    "SELECT WHEN RENT ACCRUES",
    "BEGINNING",
    "END"
)
val TENANCY_ROOM_TYPE = arrayListOf(
    "SELECT ROOM TYPE",
    "FLAT",
    "SHARING"
)

class Tenancy() {
    var id: String? = null
    var startDate: Date? = null
    var endDate: Date? = null
    var roomType: RoomType? = null
    var roomCount: Int? = null
    var images: ArrayList<Image>? = null
    var accrue: RentAccrueAt? = null
    var amount: Int? = null
    var tenant: User? = null
    var house: House? = null
    var advanceAmount: Int? = null
    var dueAmount: Int? = null
    var utilities: ArrayList<Utility>? = null
    var payments: ArrayList<Payment>? = null
    var approved: Boolean? = null

    constructor(tenancy: Tenancy) : this() {
        id = tenancy.id
        startDate = tenancy.startDate
        endDate = tenancy.endDate
        roomType = tenancy.roomType
        roomCount = tenancy.roomCount
        images = tenancy.images?.let { ArrayList(it) }
        accrue = tenancy.accrue
        amount = tenancy.amount
        tenant = tenancy.tenant?.let { User(it) }
        house = tenancy.house?.let { House(it) }
        advanceAmount = tenancy.advanceAmount
        dueAmount = tenancy.dueAmount
        utilities = tenancy.utilities?.let { ArrayList(it) }
        payments = tenancy.payments?.let { ArrayList(it) }
        approved = tenancy.approved
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            id =
                if (jsonObject.has(ID) && !jsonObject.isNull(ID)) jsonObject.getString(
                    ID
                ) else throw InvalidParameterException("Tenancy must contain id")

            startDate =
                if (jsonObject.has(START_DATE) && !jsonObject.isNull(START_DATE)) SimpleDateFormat(
                    yyyy_MM_dd,
                    Locale.ENGLISH
                ).parse(jsonObject.getString(START_DATE)) else throw InvalidParameterException("Tenancy must contain start date")

            endDate =
                if (jsonObject.has(END_DATE) && !jsonObject.isNull(END_DATE)) SimpleDateFormat(
                    yyyy_MM_dd,
                    Locale.ENGLISH
                ).parse(jsonObject.getString(END_DATE)) else null

            roomType =
                if (jsonObject.has(ROOM_TYPE) && !jsonObject.isNull(ROOM_TYPE)) getRoomType(
                    jsonObject.getString(ROOM_TYPE)
                ) else throw InvalidParameterException("Tenancy must contain room type")

            accrue =
                if (jsonObject.has(ACCRUE) && !jsonObject.isNull(ACCRUE)) getRentAccruedAt(
                    jsonObject.getString(ACCRUE)
                ) else throw InvalidParameterException("Tenancy must contain accrue parameter")

            roomCount =
                if (jsonObject.has(ROOM_COUNT) && !jsonObject.isNull(ROOM_COUNT)) jsonObject.getInt(
                    ROOM_COUNT
                ) else throw InvalidParameterException("Tenancy must contain roomCount parameter")

            amount =
                if (jsonObject.has(AMOUNT) && !jsonObject.isNull(AMOUNT)) jsonObject.getInt(
                    AMOUNT
                ) else throw InvalidParameterException("Tenancy must contain amount parameter")

            advanceAmount =
                if (jsonObject.has(ADVANCE_AMOUNT) && !jsonObject.isNull(ADVANCE_AMOUNT)) jsonObject.getInt(
                    ADVANCE_AMOUNT
                ) else throw InvalidParameterException("Tenancy must contain advanceAmount parameter")

            dueAmount =
                if (jsonObject.has(DUE_AMOUNT) && !jsonObject.isNull(DUE_AMOUNT)) jsonObject.getInt(
                    DUE_AMOUNT
                ) else throw InvalidParameterException("Tenancy must contain dueAmount parameter")

            images =
                if (jsonObject.has(IMAGES) && !jsonObject.isNull(IMAGES)) {
                    val jsonImagesArray = jsonObject.getJSONArray(IMAGES)
                    val arrayOfImages = ArrayList<Image>()
                    for (i in 0 until jsonImagesArray.length()) {
                        arrayOfImages.add(Image(jsonImagesArray.getJSONObject(i)))
                    }
                    arrayOfImages
                } else ArrayList()

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
            } else throw InvalidParameterException("Tenancy must contain house parameter")

            tenant =
                if (jsonObject.has(TENANT) && !jsonObject.isNull(TENANT)) {
                    try {
                        User(
                            jsonObject.getJSONObject(
                                TENANT
                            )
                        )
                    } catch (e: JSONException) {
                        User().apply {
                            id = jsonObject.getString(TENANT)
                        }
                    }
                } else throw InvalidParameterException("Tenancy must contain tenant parameter")

            utilities = if (jsonObject.has(UTILITIES) && !jsonObject.isNull(UTILITIES)) {
                Utility.getUtilitiesFromJSONArray(jsonObject.getJSONArray(UTILITIES))
            } else ArrayList()

            payments = if (jsonObject.has(PAYMENTS) && !jsonObject.isNull(PAYMENTS)) {
                Payment.getPaymentsFromJSONOArray(jsonObject.getJSONArray(PAYMENTS))
            } else ArrayList()

            approved =
                if (jsonObject.has(APPROVED) && !jsonObject.isNull(APPROVED)) jsonObject.getBoolean(
                    APPROVED
                ) else throw InvalidParameterException("Tenancy must contain approved parameter")

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Tenancy constructor: ${e.localizedMessage}")
        }
    }

    fun getJSONObject(): JSONObject {
        return JSONObject().apply {
            startDate?.let {
                put(START_DATE, it)
            }
            roomType?.let {
                put(ROOM_TYPE, it)
            }
            roomCount?.let {
                put(ROOM_COUNT, it)
            }
            accrue?.let {
                put(ACCRUE, it)
            }
            amount?.let {
                put(AMOUNT, it)
            }
            tenant?.let {
                it.phone?.let { phone ->
                    put(User.PHONE, phone)
                }
                it.email?.let { email ->
                    put(User.EMAIL, email)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        val ID: String = "_id"

        @JvmStatic
        val START_DATE = "startDate"

        @JvmStatic
        val END_DATE = "endDate"

        @JvmStatic
        val ROOM_TYPE = "roomType"

        @JvmStatic
        val ROOM_COUNT = "roomCount"

        @JvmStatic
        val IMAGES = "images"

        @JvmStatic
        val ACCRUE = "accrue"

        @JvmStatic
        val AMOUNT = "amount"

        @JvmStatic
        val UTILITIES = "utilities"

        @JvmStatic
        val PAYMENTS = "payments"

        @JvmStatic
        val TENANT = "tenant"

        @JvmStatic
        val HOUSE = "house"

        @JvmStatic
        val ADVANCE_AMOUNT = "advanceAmount"

        @JvmStatic
        val DUE_AMOUNT = "dueAmount"

        @JvmStatic
        val APPROVED = "approved"

        @JvmStatic
        fun getTenancyArrayListFromJSONArray(jsonArray: JSONArray): ArrayList<Tenancy> {
            val tenancies = ArrayList<Tenancy>()
            try {
                for (i in 0 until jsonArray.length()) {
                    try {
                        tenancies.add(Tenancy(jsonArray.getJSONObject(i)))
                    } catch (e: JSONException) {
                        tenancies.add(Tenancy().apply {
                            id = jsonArray.getString(i)
                        })
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, "getTenancyArrayListFromJSONArray: ${e.localizedMessage}")
            }
            return tenancies
        }

        @JvmStatic
        fun getRoomType(roomType: String): RoomType {
            return when (roomType) {
                "FLAT" -> RoomType.FLAT
                "END" -> RoomType.SHARING
                else -> RoomType.SHARING
            }
        }

        @JvmStatic
        fun getRentAccruedAt(rentAccruedAt: String): RentAccrueAt {
            return when (rentAccruedAt) {
                "BEGINNING" -> RentAccrueAt.BEGINNING
                "END" -> RentAccrueAt.END
                else -> RentAccrueAt.BEGINNING
            }
        }
    }


    enum class RoomType {
        FLAT, SHARING
    }

    enum class RentAccrueAt {
        BEGINNING, END
    }
}