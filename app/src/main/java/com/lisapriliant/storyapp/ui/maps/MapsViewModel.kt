package com.lisapriliant.storyapp.ui.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.response.StoriesResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import com.lisapriliant.storyapp.ui.getApiServiceWithToken

class MapsViewModel(private val userPreference: UserPreference): ViewModel() {
    private val _storiesLocation = MutableLiveData<StoriesResponse>()
    val storiesLocation: MutableLiveData<StoriesResponse> = _storiesLocation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    suspend fun getStoriesLocation(apiService: ApiService) {
        _isLoading.value = true
        try {
            val apiServiceWithToken = userPreference.getApiServiceWithToken()
            if (apiServiceWithToken != null) {
                val response = apiService.getStoriesLocation(1)
                if (response.isSuccessful) {
                    _storiesLocation.value = response.body()
                    _toast.value = Event(response.body()?.message.toString())
                } else {
                    val jsonInString = response.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    _toast.value = Event(errorBody.message ?: response.message().toString())
                }
            }
        } catch (e: Exception) {
            _toast.value = Event(e.message ?: "Error while loading data")
            Log.e(TAG, "getStoriesLocation: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun getApiServiceWithToken(): LiveData<ApiService?> {
        return liveData {
            emit(userPreference.getApiServiceWithToken())
        }
    }

    companion object {
        const val TAG = "MapsViewModel"
    }
}