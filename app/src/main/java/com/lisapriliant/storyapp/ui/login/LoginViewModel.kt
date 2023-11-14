package com.lisapriliant.storyapp.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.UserStoryRepository
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.response.LoginResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class LoginViewModel(
    private val repository: UserStoryRepository,
    private val apiService: ApiService
) : ViewModel() {
    private val _login = MutableLiveData<LoginResponse?>()
    val login: MutableLiveData<LoginResponse?> = _login

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    fun isLogin(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val client = apiService.login(email, password)
            client.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    _isLoading.value = false
                    try {
                        if (response.body() != null) {
                            _login.value = response.body()
                            _toast.value = Event(response.body()?.message.toString())
                        } else {
                            val jsonInString = response.errorBody()?.string()
                            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                            _toast.value = Event(errorBody.message ?: response.message().toString())
                        }
                    } catch (e: HttpException) {
                        val jsonInString = e.response()?.errorBody()?.string()
                        val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                        _toast.value = Event(errorBody.message ?: response.message().toString())
                        Log.e(TAG, "onFailure: ${response.message()}, ${response.body()?.message.toString()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    _isLoading.value = false
                    _toast.value = Event(t.message.toString())
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    companion object {
        const val TAG = "LoginViewModel"
    }
}