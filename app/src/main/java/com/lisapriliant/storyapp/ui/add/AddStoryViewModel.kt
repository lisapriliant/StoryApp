package com.lisapriliant.storyapp.ui.add

import android.net.Uri
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
import com.lisapriliant.storyapp.data.response.AddNewStoryResponse
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import com.lisapriliant.storyapp.ui.getApiServiceWithToken
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class AddStoryViewModel(
    private val userPreference: UserPreference,
    private val repository: UserStoryRepository
) : ViewModel() {
    private val _addNewStory = MutableLiveData<AddNewStoryResponse>()
    val addNewStory: LiveData<AddNewStoryResponse> = _addNewStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    var currentImageUri: Uri? = null
    var description: String = ""

    suspend fun addStory(apiService: ApiService, file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        try {
            val response = apiService.addStory(file, description)
            if (response.isSuccessful) {
                _addNewStory.value = response.body()
                _toast.value = Event(response.body()?.message.toString())
            } else {
                val jsonInString = response.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                _toast.value = Event(errorBody.message ?: response.message().toString())
            }
        } catch (e: HttpException) {
            _toast.value = Event(e.message ?: "Error while loading data")
            Log.e(TAG, "addStory: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun addNewStory(apiService: ApiService, file: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            this@AddStoryViewModel.addStory(apiService, file, description)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession()
    }

    fun getApiServiceWithToken(): LiveData<ApiService?> {
        return liveData {
            emit(userPreference.getApiServiceWithToken())
        }
    }

    fun saveInstanceState(imageUri: Uri?, description: String) {
        currentImageUri = imageUri
        this.description = description
    }

    companion object {
        const val TAG = "AddStoryViewModel"
    }
}