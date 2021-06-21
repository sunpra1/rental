package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

private const val TAG = "UserToken"
private const val TOKEN: String = "TOKEN"
private const val TOKEN_VALUE: String = "TOKEN_VALUE"

class UserToken private constructor(context: Context) {
    companion object {
        @JvmStatic
        fun getInstance(context: Context): UserToken = UserToken(context)
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(TOKEN, Context.MODE_PRIVATE)

    var token: String? = null
        set(value) {
            sharedPreferences.edit().putString(TOKEN_VALUE, value).apply()
            field = value
        }


    fun deleteToken() {
        sharedPreferences.edit().remove(TOKEN_VALUE).apply()
    }

    init {
        token = sharedPreferences.getString(TOKEN_VALUE, null)
        Log.d(TAG, "User token is : $token")
    }
}