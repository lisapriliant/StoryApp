package com.lisapriliant.storyapp.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lisapriliant.storyapp.data.response.ErrorResponse
import com.lisapriliant.storyapp.data.response.RegisterResponse
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.Event
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class RegisterViewModel(private val apiService: ApiService) : ViewModel() {
    private val _register = MutableLiveData<RegisterResponse>()
    val register: LiveData<RegisterResponse> = _register

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    fun isRegister(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val client = apiService.register(name, email, password)
            client.enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    _isLoading.value = false
                    try {
                        if (response.body() != null) {
                            _register.value = response.body()
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

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    _isLoading.value = false
                    _toast.value = Event(t.message.toString())
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        }
    }

    companion object {
        const val TAG = "RegisterViewModel"
    }
}