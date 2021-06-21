package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM_dd
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "User"


val GENDER_TYPE = arrayListOf(
    "SELECT GENDER",
    "MALE",
    "FEMALE",
    "OTHER"
)

class User() {
    var id: String? = null
    var fullName: String? = null
    var phone: Long? = null
    var address: String? = null
    var password: String? = null
    var gender: Gender? = null
    var email: String? = null
    var image: Image? = null
    var dob: Date? = null
    var tenancies: ArrayList<Tenancy>? = null
    var houses: ArrayList<House>? = null
    var reviews: ArrayList<Review>? = null

    constructor(user: User) : this() {
        id = user.id
        fullName = user.fullName
        phone = user.phone
        address = user.address
        password = user.password
        gender = user.gender
        email = user.email
        image = user.image
        dob = user.dob
        tenancies = user.tenancies?.let { ArrayList(it) }
        houses = user.houses?.let { ArrayList(it) }
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            id =
                if (jsonObject.has(ID) && !jsonObject.isNull(ID)) jsonObject.getString(ID)
                else throw InvalidParameterException(
                    "User must contain id parameter"
                )

            fullName =
                if (jsonObject.has(FULL_NAME) && !jsonObject.isNull(FULL_NAME)) jsonObject.getString(
                    FULL_NAME
                )
                else
                    throw InvalidParameterException("User must contain full name parameter")

            phone =
                if (jsonObject.has(PHONE) && !jsonObject.isNull(PHONE)) jsonObject.getString(PHONE)
                    .toLong()
                else
                    throw InvalidParameterException("User must contain phone parameter")

            address =
                if (jsonObject.has(ADDRESS) && !jsonObject.isNull(ADDRESS)) jsonObject.getString(
                    ADDRESS
                ) else null

            image =
                if (jsonObject.has(IMAGE) && !jsonObject.isNull(IMAGE)) Image(
                    jsonObject.getJSONObject(
                        IMAGE
                    )
                )
                else null

            email =
                if (jsonObject.has(EMAIL) && !jsonObject.isNull(EMAIL)) jsonObject.getString(EMAIL) else null

            gender = if (jsonObject.has(GENDER) && !jsonObject.isNull(GENDER)) getGender(
                jsonObject.getString(
                    GENDER
                )
            ) else null

            dob = if (jsonObject.has(DOB) && !jsonObject.isNull(DOB)) SimpleDateFormat(
                yyyy_MM_dd,
                Locale.ENGLISH
            ).parse(jsonObject.getString(DOB)) else null

            tenancies = if (jsonObject.has(TENANCIES) && !jsonObject.isNull(TENANCIES)) {
                try {
                    Tenancy.getTenancyArrayListFromJSONArray(
                        jsonObject.getJSONArray(TENANCIES)
                    )
                } catch (e: JSONException) {
                    ArrayList()
                }
            } else ArrayList()

            houses = if (jsonObject.has(HOUSES) && !jsonObject.isNull(HOUSES)) {
                try {
                    House.getHousesArrayListFromJSONArray(
                        jsonObject.getJSONArray(HOUSES)
                    )
                } catch (e: JSONException) {
                    ArrayList()
                }
            } else ArrayList()

            reviews = if (jsonObject.has(REVIEWS) && !jsonObject.isNull(REVIEWS)) {
                try {
                    Review.getReviewArrayListFromJSONArray(
                        jsonObject.getJSONArray(REVIEWS)
                    )
                } catch (e: JSONException) {
                    ArrayList()
                }
            } else ArrayList()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "User constructor: ${e.localizedMessage}")
        }
    }

    private fun getRole(role: String): Role {
        return when (role) {
            "TENANT" -> Role.TENANT
            "ADMIN" -> Role.ADMIN
            "OWNER" -> Role.OWNER
            else -> Role.TENANT
        }
    }

    enum class Role {
        TENANT, ADMIN, OWNER
    }

    enum class Gender {
        MALE, FEMALE, OTHER
    }

    fun getJSONObject(): JSONObject {
        return JSONObject().apply {
            id?.let {
                put(ID, it)
            }
            fullName?.let {
                put(FULL_NAME, it)
            }
            phone?.let {
                put(PHONE, it)
            }
            address?.let {
                put(ADDRESS, it)
            }
            password?.let {
                put(PASSWORD, it)
            }
            email?.let {
                put(EMAIL, it)
            }
            gender?.let {
                put(GENDER, it)
            }
            dob?.let {
                put(DOB, it.toString())
            }
        }
    }

    companion object {
        @JvmStatic
        val IMAGE: String = "image"

        @JvmStatic
        val ID: String = "_id"

        @JvmStatic
        val FULL_NAME: String = "fullName"

        @JvmStatic
        val PHONE: String = "phone"

        @JvmStatic
        val EMAIL: String = "email"

        @JvmStatic
        val ADDRESS: String = "address"

        @JvmStatic
        val GENDER: String = "gender"

        @JvmStatic
        val DOB: String = "dob"

        @JvmStatic
        val PASSWORD: String = "password"

        @JvmStatic
        val TENANCIES: String = "tenancies"

        @JvmStatic
        val HOUSES: String = "houses"

        @JvmStatic
        val PHONE_OR_EMAIL: String = "phone/email"

        @JvmStatic
        val REVIEWS: String = "reviews"

        @JvmStatic
        fun getGender(gender: String): Gender? {
            return when (gender) {
                "MALE" -> Gender.MALE
                "FEMALE" -> Gender.FEMALE
                "OTHER" -> Gender.OTHER
                else -> null
            }
        }
    }
}