package com.lisapriliant.storyapp.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.UserStoryRepository
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.response.ListStoryItem
import com.lisapriliant.storyapp.data.response.LoginResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import com.lisapriliant.storyapp.ui.getApiServiceWithToken
import kotlinx.coroutines.launch

class MainViewModel(
    private val userPreference: UserPreference,
    private val repository: UserStoryRepository
) : ViewModel() {
    private val _login = MutableLiveData<LoginResponse?>()

    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> = _listStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    fun getSession(): LiveData<UserModel> {
        return repository.getSession()
    }

    suspend fun getStory(apiService: ApiService) {
        _isLoading.value = true
        try {
            val apiServiceWithToken = userPreference.getApiServiceWithToken()
            if (apiServiceWithToken != null) {
                val response = apiService.getStories()
                if (response.isSuccessful) {
                    _listStory.value = response.body()?.listStory ?: emptyList()
                } else {
                    val jsonInString = response.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    _toast.value = Event(errorBody.message ?: response.message().toString())
                }
            }
        } catch (e: Exception) {
            _toast.value = Event(e.message ?: "Error while loading data")
            Log.e(TAG, "getStory: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun getAllStory(apiService: ApiService) {
        viewModelScope.launch {
            this@MainViewModel.getStory(apiService)
        }
    }

    fun getApiServiceWithToken(): LiveData<ApiService?> {
        return liveData {
            emit(userPreference.getApiServiceWithToken())
        }
    }

    suspend fun logout() {
        userPreference.logout()
        _login.value = null
    }

    fun isLogout() {
        viewModelScope.launch {
            this@MainViewModel.logout()
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}