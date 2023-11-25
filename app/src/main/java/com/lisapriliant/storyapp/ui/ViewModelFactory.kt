package com.lisapriliant.storyapp.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lisapriliant.storyapp.data.UserStoryRepository
import com.lisapriliant.storyapp.data.di.Injection
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.pref.dataStore
import com.lisapriliant.storyapp.data.retrofit.ApiConfig
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.add.AddStoryViewModel
import com.lisapriliant.storyapp.ui.detail.DetailViewModel
import com.lisapriliant.storyapp.ui.login.LoginViewModel
import com.lisapriliant.storyapp.ui.main.MainViewModel
import com.lisapriliant.storyapp.ui.maps.MapsViewModel
import com.lisapriliant.storyapp.ui.register.RegisterViewModel

class ViewModelFactory(
    private val repository: UserStoryRepository,
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(apiService) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userPreference, apiService) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userPreference, repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(userPreference) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(userPreference) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(userPreference) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            val repository = Injection.provideRepository(context)
            val dataStore: DataStore<Preferences> = context.dataStore
            val userPreference = UserPreference.getInstance(dataStore)
            val apiService = ApiConfig.getApiService(token = "")
            INSTANCE ?: ViewModelFactory(repository, userPreference, apiService)
        }.also {
            INSTANCE = it
        }

        fun resetInstance() {
            INSTANCE = null
            Injection.resetInstance()
        }
    }
}