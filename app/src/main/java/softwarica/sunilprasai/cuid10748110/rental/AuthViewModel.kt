package softwarica.sunilprasai.cuid10748110.rental

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.model.User
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.AppState
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "AuthViewModel"

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val loggedInUser = MutableLiveData<User>().also {
        if (it.value == null)
            when {
                AppState.loggedInUser != null -> it.value = AppState.loggedInUser
                UserToken.getInstance(application.applicationContext).token != null -> fetchLoggedInUserDetails(
                    application.applicationContext
                )
                else -> it.value = null
            }
    }

    fun getLoggedInUser(): LiveData<User> = loggedInUser
    fun getLoggedInUserValue(): User? = loggedInUser.value
    fun setLoggedInUser(user: User?) {
        loggedInUser.value = user
    }

    fun fetchLoggedInUserDetails(context: Context) {
        val token: String = UserToken.getInstance(context).token!!
        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val getLoggedInUserProfile: Call<ResponseBody> = consumer.getUserProfile(token)
        getLoggedInUserProfile.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        val jsonResponse = JSONObject(it.string())
                        Log.d(TAG, "onResponse of fetchLoggedInUserDetails: $jsonResponse")
                        val user = User(jsonResponse)
                        loggedInUser.value = user
                        AppState.loggedInUser = user
                    }
                } else {
                    loggedInUser.value = null
                    AppState.loggedInUser = null
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onResponse of fetchLoggedInUserDetails: ${t.localizedMessage}")
                loggedInUser.value = null
                AppState.loggedInUser = null
            }
        })
    }
}