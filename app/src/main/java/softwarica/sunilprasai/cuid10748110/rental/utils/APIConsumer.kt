package softwarica.sunilprasai.cuid10748110.rental.utils

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIConsumer {
    @POST("users/register")
    fun registerUser(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("users/validate-unique-phone")
    fun validatePhoneNumber(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("users/validate-unique-email")
    fun validateEmailAddress(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("users/login")
    fun loginUser(@Body requestBody: RequestBody): Call<ResponseBody>

    @GET("users/profile")
    fun getUserProfile(@Header("authorization") authorization: String): Call<ResponseBody>

    @POST("users/logout")
    fun logOutUser(@Header("authorization") authorization: String): Call<ResponseBody>

    @Multipart
    @PUT("users/profile")
    fun updateUserProfile(
        @Header("authorization") authorization: String,
        @Part image: MultipartBody.Part?,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @Multipart
    @POST("houses")
    fun addHouse(
        @Header("authorization") authorization: String,
        @Part images: ArrayList<MultipartBody.Part>,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @GET("houses")
    fun getHouses(@Header("authorization") header: String): Call<ResponseBody>

    @GET("houses/{houseID}")
    fun getHouse(
        @Header("authorization") header: String,
        @Path("houseID") houseID: String
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}")
    fun deleteHouse(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String
    ): Call<ResponseBody>

    @Multipart
    @PUT("houses/{houseID}")
    fun updateHouse(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Part images: ArrayList<MultipartBody.Part>?,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @Multipart
    @POST("houses/{houseID}/tenancies")
    fun addTenancy(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Part images: ArrayList<MultipartBody.Part>,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @GET("houses/{houseID}/tenancies/{tenancyID}")
    fun getTenancy(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @GET("houses/{houseID}/tenanciesHistory/{tenancyID}")
    fun getTenancyHistory(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @Multipart
    @PUT("houses/{houseID}/tenancies/{tenancyID}")
    fun updateTenancy(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Part images: ArrayList<MultipartBody.Part>?,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}/tenancies/{tenancyID}")
    fun deleteTenancy(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @POST("houses/{houseID}/tenancies/{tenancyID}/utilities")
    fun addUtility(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @POST("houses/{houseID}/tenancies/{tenancyID}/payments")
    fun addPayment(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @PUT("houses/{houseID}/tenancies/{tenancyID}/utilities/{utilityID}")
    fun updateUtility(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Path("utilityID") utilityID: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}/tenancies/{tenancyID}/utilities/{utilityID}")
    fun deleteUtility(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Path("utilityID") utilityID: String
    ): Call<ResponseBody>

    @PUT("houses/{houseID}/tenancies/{tenancyID}/payments/{paymentID}")
    fun updatePayment(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Path("paymentID") utilityID: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}/tenancies/{tenancyID}/payments/{paymentID}")
    fun deletePayment(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String,
        @Path("paymentID") utilityID: String
    ): Call<ResponseBody>

    @Multipart
    @POST("houses/{houseID}/toLets")
    fun addToLet(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Part images: ArrayList<MultipartBody.Part>,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}/toLets/{toLetID}")
    fun deleteToLet(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("toLetID") toLetID: String
    ): Call<ResponseBody>

    @Multipart
    @PUT("houses/{houseID}/toLets/{toLetID}")
    fun updateToLet(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("toLetID") toLetID: String,
        @Part images: ArrayList<MultipartBody.Part>?,
        @PartMap requestBody: HashMap<String, RequestBody>
    ): Call<ResponseBody>

    @GET("houses/{houseID}/toLets/{toLetID}")
    fun getToLet(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("toLetID") toLetID: String
    ): Call<ResponseBody>

    @GET("tenancies")
    fun getTenantTenancies(
        @Header("authorization") token: String
    ): Call<ResponseBody>

    @GET("tenancies/{tenancyID}")
    fun getTenantTenancy(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @PUT("tenancies/{tenancyID}/approve")
    fun approveTenantTenancy(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @DELETE("tenancies/{tenancyID}")
    fun deleteTenantTenancy(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @PUT("tenancies/{tenancyID}/utilities/{utilityID}/toggleApprove")
    fun toggleTenantTenancyUtilityApproveStatus(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String,
        @Path("utilityID") utilityID: String
    ): Call<ResponseBody>


    @PUT("tenancies/{tenancyID}/payments/{paymentID}/toggleApprove")
    fun toggleTenantTenancyPaymentApproveStatus(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String,
        @Path("paymentID") paymentID: String
    ): Call<ResponseBody>

    @PUT("houses/{houseID}/tenancies/{tenancyID}/endTenancy")
    fun endTenancy(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @PUT("houses/{houseID}/tenanciesHistory/{tenancyID}/makeActive")
    fun makeTenancyActive(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @DELETE("houses/{houseID}/tenanciesHistory/{tenancyID}")
    fun deleteTenancyHistory(
        @Header("authorization") token: String,
        @Path("houseID") houseID: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @GET("tenanciesHistory")
    fun getTenantTenanciesHistory(
        @Header("authorization") token: String
    ): Call<ResponseBody>

    @DELETE("tenanciesHistory/{tenancyID}")
    fun deleteTenantTenanciesHistory(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @GET("tenanciesHistory/{tenancyID}")
    fun getTenantTenancyHistory(
        @Header("authorization") token: String,
        @Path("tenancyID") tenancyID: String
    ): Call<ResponseBody>

    @GET("toLets/{page}/{limit}/{roomType}/{roomCount}/{minAmount}/{maxAmount}/{address}")
    fun searchToLets(
        @Header("authorization") token: String,
        @Path("page") page: Int,
        @Path("limit") limit: Int,
        @Path("roomType") roomType: String,
        @Path("roomCount") roomCount: String,
        @Path("minAmount") minRentAmount: String,
        @Path("maxAmount") maxRentAmount: String,
        @Path("address") address: String
    ): Call<ResponseBody>

    @GET("toLets/{toLetID}")
    fun getToLetsToLet(
        @Header("authorization") token: String,
        @Path("toLetID") toLetID: String
    ): Call<ResponseBody>

    @POST("users/review/{userID}")
    fun postReview(
        @Header("authorization") token: String,
        @Path("userID") userID: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>
}