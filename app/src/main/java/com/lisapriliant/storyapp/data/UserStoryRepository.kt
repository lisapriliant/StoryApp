package com.lisapriliant.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.lisapriliant.storyapp.data.database.StoryDatabase
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.remote.StoryRemoteMediator
import com.lisapriliant.storyapp.data.response.ListStoryItem
import com.lisapriliant.storyapp.data.retrofit.ApiService

class UserStoryRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
){
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getSession(): LiveData<UserModel> {
        return userPreference.getSession().asLiveData()
    }

    companion object {
        @Volatile
        private var instance: UserStoryRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): UserStoryRepository =
            instance ?: synchronized(this) {
                instance ?: UserStoryRepository(userPreference, apiService, storyDatabase)
            }.also {
                instance = it
            }

        fun resetInstance() {
            instance = null
        }
    }
}