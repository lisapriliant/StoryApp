package com.lisapriliant.storyapp.ui.add

import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.response.AddNewStoryResponse
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.retrofit.ApiConfig
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import com.lisapriliant.storyapp.ui.getApiServiceWithToken
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class AddStoryViewModel(
    private val userPreference: UserPreference
) : ViewModel() {
    private val _addNewStory = MutableLiveData<AddNewStoryResponse>()
    val addNewStory: LiveData<AddNewStoryResponse> = _addNewStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    var currentImageUri: Uri? = null
    var description: String = ""

    private var currentLocation: Location? = null

    suspend fun addStory(apiService: ApiService, file: MultipartBody.Part, description: RequestBody, currentLocation: Location? = null) {
        _isLoading.value = true
        try {
            val apiServiceWithToken = userPreference.getApiServiceWithToken()
            if (apiServiceWithToken != null) {
                val response = apiService.addStory(file, description, currentLocation?.latitude, currentLocation?.longitude)
                if (response.isSuccessful) {
                    _addNewStory.value = response.body()
                    _toast.value = Event(response.body()?.message.toString())
                } else {
                    val jsonInString = response.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    _toast.value = Event(errorBody.message ?: response.message().toString())
                }
            }
        } catch (e: HttpException) {
            _toast.value = Event(e.message ?: "Error while loading data")
            Log.e(TAG, "addStory: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getApiServiceWithToken(): ApiService? {
        val user = userPreference.getSession().first()
        return if (user.isLogin && user.token.isNotEmpty()) {
            ApiConfig.getApiService(user.token)
        } else {
            null
        }
    }

    fun saveInstanceState(imageUri: Uri?, description: String, currentLocation: Location?) {
        currentImageUri = imageUri
        this.description = description
        this.currentLocation = currentLocation
    }

    companion object {
        const val TAG = "AddStoryViewModel"
    }
}