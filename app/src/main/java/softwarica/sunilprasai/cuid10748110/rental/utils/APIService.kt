package softwarica.sunilprasai.cuid10748110.rental.utils

import retrofit2.Retrofit

object APIService {
    private const val BASE_URL = "http://192.168.0.100:5000/"

    fun <T> getService(serviceInterface: Class<T>): T {
        val builder: Retrofit.Builder = Retrofit.Builder().baseUrl(BASE_URL)
        val retrofit: Retrofit = builder.build()
        return retrofit.create(serviceInterface)
    }
}