package com.lisapriliant.storyapp.data.di

import android.content.Context
import com.lisapriliant.storyapp.data.UserStoryRepository
import com.lisapriliant.storyapp.data.database.StoryDatabase
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.pref.dataStore
import com.lisapriliant.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserStoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val storyDatabase = StoryDatabase.getDatabase(context)
        return UserStoryRepository.getInstance(pref, apiService, storyDatabase)
    }

    fun resetInstance() {
        UserStoryRepository.resetInstance()
    }
}