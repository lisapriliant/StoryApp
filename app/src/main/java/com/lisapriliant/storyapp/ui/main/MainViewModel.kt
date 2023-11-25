package com.lisapriliant.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lisapriliant.storyapp.data.UserStoryRepository
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.response.ListStoryItem
import com.lisapriliant.storyapp.data.response.LoginResponse
import com.lisapriliant.storyapp.ui.Event

class MainViewModel(
    private val userPreference: UserPreference,
    private val repository: UserStoryRepository
) : ViewModel() {
    private val _login = MutableLiveData<LoginResponse?>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<Event<String>>()
    val toast: LiveData<Event<String>> = _toast

    val storyItem: LiveData<PagingData<ListStoryItem>> = repository.getStories().cachedIn(viewModelScope)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
        _login.value = null
    }
}