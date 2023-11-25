package com.lisapriliant.storyapp.data.retrofit

import com.lisapriliant.storyapp.data.response.AddNewStoryResponse
import com.lisapriliant.storyapp.data.response.DetailStoryResponse
import com.lisapriliant.storyapp.data.response.LoginResponse
import com.lisapriliant.storyapp.data.response.RegisterResponse
import com.lisapriliant.storyapp.data.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<StoriesResponse>

    @GET("stories/{id}")
    suspend fun getDetailStory(
        @Path("id") id: String
    ): Response<DetailStoryResponse>

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double? = null,
        @Part("lon") lon: Double? = null
    ): Response<AddNewStoryResponse>

    @GET("stories")
    suspend fun getStoriesLocation(
        @Query("location") location: Int = 1
    ): Response<StoriesResponse>
}