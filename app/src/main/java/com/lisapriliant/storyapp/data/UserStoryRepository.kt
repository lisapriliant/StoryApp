package com.lisapriliant.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.retrofit.ApiService

class UserStoryRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
){
    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): LiveData<UserModel> {
        return userPreference.getSession().asLiveData()
    }

    companion object {
        @Volatile
        private var instance: UserStoryRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserStoryRepository =
            instance ?: synchronized(this) {
                instance ?: UserStoryRepository(userPreference, apiService)
            }.also {
                instance = it
            }
    }
}