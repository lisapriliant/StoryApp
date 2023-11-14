package com.lisapriliant.storyapp.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.response.DetailStoryResponse
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import com.lisapriliant.storyapp.ui.getApiServiceWithToken
import kotlinx.coroutines.launch

class DetailViewModel(private val userPreference: UserPreference) : ViewModel() {
    private val _detailStory = MutableLiveData<DetailStoryResponse>()
    val detailStory: MutableLiveData<DetailStoryResponse> = _detailStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    suspend fun getDetailStory(apiService: ApiService, id: String = "") {
        _isLoading.value = true
        try {
            val apiServiceWithToken = userPreference.getApiServiceWithToken()
            if (apiServiceWithToken != null) {
                val response = apiService.getDetailStory(id)
                if (response.isSuccessful) {
                    _detailStory.value = response.body()
                } else {
                    val jsonInString = response.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    _toast.value = Event(errorBody.message ?: response.message().toString())
                }
            }
        } catch (e: Exception) {
            _toast.value = Event(e.message ?: "Error while loading data")
            Log.e(TAG, "getDetailStory: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun getApiServiceWithToken(): LiveData<ApiService?> {
        return liveData {
            emit(userPreference.getApiServiceWithToken())
        }
    }

    fun getDetailStoryById(apiService: ApiService, id: String) {
        viewModelScope.launch {
            this@DetailViewModel.getDetailStory(apiService, id)
        }
    }

    companion object {
        const val TAG = "DetailViewModel"
    }
}