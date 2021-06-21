package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "Payment"

class Payment() {
    var id: String? = null
    var amountPayable: Int? = null
    var amountReceived: Int? = null
    var paiForMonth: Int? = null
    var paidForYear: Int? = null
    var paymentDetails: ArrayList<PaymentDetail>? = null
    var note: String? = null
    var approved: Boolean? = null

    constructor(payment: Payment) : this() {
        id = payment.id
        amountPayable = payment.amountPayable
        amountReceived = payment.amountReceived
        paiForMonth = payment.paiForMonth
        paidForYear = payment.paidForYear
        paymentDetails = payment.paymentDetails?.let { ArrayList(it) }
        note = payment.note
        approved = payment.approved
    }

    constructor(jsonObject: JSONObject) : this() {
        id =
            if (jsonObject.has(ID) && !jsonObject.isNull(ID)) jsonObject.getString(ID) else throw InvalidParameterException(
                "Payment must contain _id parameter"
            )

        amountPayable =
            if (jsonObject.has(AMOUNT_PAYABLE) && !jsonObject.isNull(AMOUNT_PAYABLE)) jsonObject.getInt(
                AMOUNT_PAYABLE
            ) else throw InvalidParameterException(
                "Payment must contain amountPayable parameter"
            )

        amountReceived =
            if (jsonObject.has(AMOUNT_RECEIVED) && !jsonObject.isNull(AMOUNT_RECEIVED)) jsonObject.getInt(
                AMOUNT_RECEIVED
            ) else throw InvalidParameterException(
                "Payment must contain amountReceived parameter"
            )

        paiForMonth =
            if (jsonObject.has(PAID_FOR_MONTH) && !jsonObject.isNull(PAID_FOR_MONTH)) jsonObject.getInt(
                PAID_FOR_MONTH
            ) else throw InvalidParameterException(
                "Payment must contain paidForMonth parameter"
            )

        paidForYear =
            if (jsonObject.has(PAID_FOR_YEAR) && !jsonObject.isNull(PAID_FOR_YEAR)) jsonObject.getInt(
                PAID_FOR_YEAR
            ) else throw InvalidParameterException(
                "Payment must contain paidForYear parameter"
            )

        paymentDetails =
            if (jsonObject.has(PAYMENT_DETAILS) && !jsonObject.isNull(PAYMENT_DETAILS)) PaymentDetail.getPaymentDetailsFromJSONArray(
                jsonObject.getJSONArray(
                    PAYMENT_DETAILS
                )
            ) else ArrayList()

        note =
            if (jsonObject.has(NOTE) && !jsonObject.isNull(NOTE)) jsonObject.getString(
                NOTE
            ) else null

        approved =
            if (jsonObject.has(APPROVED) && !jsonObject.isNull(APPROVED)) jsonObject.getBoolean(
                APPROVED
            ) else throw InvalidParameterException(
                "Payment must contain approved parameter"
            )
    }

    fun getJSONObject(): JSONObject {
        val jsonObject = JSONObject()

        amountPayable?.let {
            jsonObject.put(AMOUNT_PAYABLE, it)
        }

        amountReceived?.let {
            jsonObject.put(AMOUNT_RECEIVED, it)
        }

        paiForMonth?.let {
            jsonObject.put(PAID_FOR_MONTH, it)
        }

        paidForYear?.let {
            jsonObject.put(PAID_FOR_YEAR, it)
        }

        note?.let {
            jsonObject.put(NOTE, it)
        }

        paymentDetails?.let {
            if (it.size > 0) {
                val paymentDetailsJSONArray = JSONArray()
                it.forEach { paymentDetailsJSONArray.put(it.getJSONObject()) }
                jsonObject.put(PAYMENT_DETAILS, paymentDetailsJSONArray)
            }
        }

        return jsonObject
    }

    companion object {
        @JvmStatic
        val ID = "_id"

        @JvmStatic
        val AMOUNT_PAYABLE = "amountPayable"

        @JvmStatic
        val AMOUNT_RECEIVED = "amountReceived"

        @JvmStatic
        val PAID_FOR_MONTH = "paidForMonth"

        @JvmStatic
        val PAID_FOR_YEAR = "paidForYear"

        @JvmStatic
        val PAYMENT_DETAILS = "paymentDetails"

        @JvmStatic
        val NOTE = "note"

        @JvmStatic
        val APPROVED = "approved"

        @JvmStatic
        fun getPaymentsFromJSONOArray(jsonArray: JSONArray): ArrayList<Payment> {
            val payments: ArrayList<Payment> = ArrayList()

            try {
                if (jsonArray.length() > 0) {
                    for (i in 0 until jsonArray.length()) {
                        payments.add(Payment(jsonArray.getJSONObject(i)))
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "getPaymentsFromJSONOArray: ${e.localizedMessage}")
            }

            return payments
        }
    }
}