package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "PaymentDetails"

class PaymentDetail() {
    var id: String? = null
    var name: String? = null
    var isVariableCost: Boolean? = null
    var price: Int? = null
    var units: Int? = null

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

            units =
                if (jsonObject.has(UNITS) && !jsonObject.isNull(UNITS)) jsonObject.getInt(UNITS) else null
        } catch (e: JSONException) {
            Log.e(TAG, "Constructor: ${e.localizedMessage}")
        }
    }

    fun getJSONObject(): JSONObject {
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
            units?.let {
                put(UNITS, it)
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
        val UNITS = "units"

        @JvmStatic
        fun getPaymentDetailsFromJSONArray(jsonArray: JSONArray): ArrayList<PaymentDetail> {
            val paymentDetails: ArrayList<PaymentDetail> = ArrayList()
            try {
                if (jsonArray.length() > 0) {
                    for (i in 0 until jsonArray.length()) {
                        paymentDetails.add(PaymentDetail(jsonArray.getJSONObject(i)))
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "getPaymentDetailsFromJSONArray: ${e.localizedMessage}")
            }

            return paymentDetails
        }
    }
}