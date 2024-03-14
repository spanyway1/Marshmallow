package com.marshmallow

import okhttp3.MultipartBody
import retrofit2.Call;
import retrofit2.http.Body
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST;
import retrofit2.http.Part
import retrofit2.http.Query

data class FluppyResult(
    var mData: String
)

data class SendData(
    var uuid:String,
    var data:String
)

data class EasyResult(
    var mData1: String,
    var mData2:String,
    var mData3:String
)
interface APIInterface {
    @GET("/retrofit/getEasy")
    fun getEasy(@Query("uuid")uuid: String, @Query("data")data:String):Call<EasyResult>

    @GET("/retrofit/discriminate")
    fun getDiscrimination(@Query("uuid")uuid: String, @Query("data")data:String):Call<FluppyResult>

    @Multipart
    @POST("/saveManual")
    fun postManual(@Part img:MultipartBody.Part)
            :Call<FluppyResult>

    @GET("/retrofit/convTopic")
    fun getImageUrls(@Query("uuid")uuid: String, @Query("data")data:String)
    :Call<FluppyResult>

}