package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException


private const val TAG = "Review"

class Review() {
    var context: Context? = null
    var rating: Int? = null
    var comment: String? = null
    var user: User? = null

    constructor(jsonObject: JSONObject) : this() {
        try {
            context = if (jsonObject.has(CONTEXT) && !jsonObject.isNull(CONTEXT))
                getReviewContext(jsonObject.getString(CONTEXT))
            else throw InvalidParameterException("Review must contain context parameter")

            rating = if (jsonObject.has(RATING) && !jsonObject.isNull(RATING))
                jsonObject.getInt(RATING)
            else throw InvalidParameterException("Review must contain rating parameter")

            comment = if (jsonObject.has(COMMENT) && !jsonObject.isNull(COMMENT))
                jsonObject.getString(COMMENT)
            else throw InvalidParameterException("Review must contain comment parameter")

            user = if (jsonObject.has(USER) && !jsonObject.isNull(USER)) {
                try {
                    User(jsonObject.getJSONObject(USER))
                } catch (e: JSONException) {
                    User().apply {
                        id = jsonObject.getString(USER)
                    }
                }
            } else
                throw InvalidParameterException("House must contain user parameter")

        } catch (e: JSONException) {
            Log.d(TAG, "constructor: ${e.localizedMessage}")
        }
    }

    fun getJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            context?.let { jsonObject.put(CONTEXT, context) }
            rating?.let { jsonObject.put(RATING, rating) }
            comment?.let { jsonObject.put(COMMENT, comment) }
        } catch (e: JSONException) {
            Log.d(TAG, "getJSONObject: ${e.localizedMessage}")
        }

        return jsonObject
    }


    companion object {
        @JvmStatic
        val CONTEXT = "context"

        @JvmStatic
        val COMMENT = "comment"

        @JvmStatic
        val RATING = "rating"

        @JvmStatic
        val USER = "user"

        @JvmStatic
        fun getReviewContext(context: String): Context {
            return when (context) {
                "OWNER" -> Context.OWNER
                "TENANT" -> Context.TENANT
                else -> throw InvalidParameterException("Invalid review context provided")
            }
        }

        @JvmStatic
        fun getReviewArrayListFromJSONArray(jsonArray: JSONArray): ArrayList<Review> {
            val reviews = ArrayList<Review>()
            for (i in 0 until jsonArray.length()) {
                try {
                    reviews.add(Review(jsonArray.getJSONObject(i)))
                } catch (e: JSONException) {
                    Log.e(TAG, "getReviewArrayListFromJSONArray: ${e.localizedMessage}")
                }
            }
            return reviews
        }
    }

    enum class Context {
        OWNER, TENANT
    }
}