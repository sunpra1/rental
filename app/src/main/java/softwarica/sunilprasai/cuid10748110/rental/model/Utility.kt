package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "Utility"

open class Utility() {
    var id: String? = null
    var name: String? = null
    var isVariableCost: Boolean? = null
    var price: Int? = null
    var approved: Boolean? = null

    constructor(utility: Utility) : this() {
        id = utility.id
        name = utility.name
        isVariableCost = utility.isVariableCost
        price = utility.price
        approved = utility.approved
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            id = if (jsonObject.has(ID) && !jsonObject.isNull(ID))
                jsonObject.getString(ID)
            else throw InvalidParameterException("Utility must contain _id parameter")

            name = if (jsonObject.has(NAME) && !jsonObject.isNull(NAME))
                jsonObject.getString(NAME)
            else throw InvalidParameterException("Utility must contain name parameter")

            isVariableCost =
                if (jsonObject.has(IS_VARIABLE_COST) && !jsonObject.isNull(IS_VARIABLE_COST))
                    jsonObject.getBoolean(IS_VARIABLE_COST)
                else throw InvalidParameterException("Utility must contain isVariableCost parameter")

            price = if (jsonObject.has(PRICE) && !jsonObject.isNull(PRICE))
                jsonObject.getInt(PRICE)
            else throw InvalidParameterException("Utility must contain price parameter")

            approved =
                if (jsonObject.has(APPROVED) && !jsonObject.isNull(APPROVED)) jsonObject.getBoolean(
                    APPROVED
                ) else throw InvalidParameterException(
                    "Payment must contain approved parameter"
                )

        } catch (e: JSONException) {
            Log.d(TAG, "constructor: ${e.localizedMessage}")
        }
    }

    open fun getJSONObject(): JSONObject {
        return JSONObject().apply {
            name?.let {
                put(NAME, name)
            }
            isVariableCost?.let {
                put(IS_VARIABLE_COST, isVariableCost)
            }
            price?.let {
                put(PRICE, price)
            }
        }
    }

    companion object {
        @JvmStatic
        val ID = "_id"

        @JvmStatic
        val NAME = "name"

        @JvmStatic
        val IS_VARIABLE_COST = "isVariableCost"

        @JvmStatic
        val PRICE = "price"

        @JvmStatic
        val APPROVED = "approved"

        @JvmStatic
        fun getUtilitiesFromJSONArray(jsonArray: JSONArray): ArrayList<Utility> {
            val utilities: ArrayList<Utility> = ArrayList()
            try {
                if (jsonArray.length() > 0) {
                    for (i in 0 until jsonArray.length()) {
                        utilities.add(Utility(jsonArray.getJSONObject(i)))
                    }
                }
            } catch (e: JSONException) {
                Log.d(TAG, "getUtilitiesFromJSONArray: ${e.localizedMessage}")
            }
            return utilities
        }
    }
}