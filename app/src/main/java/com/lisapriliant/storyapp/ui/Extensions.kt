package com.lisapriliant.storyapp.ui

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.lisapriliant.storyapp.data.pref.UserPreference
import com.lisapriliant.storyapp.data.retrofit.ApiConfig
import com.lisapriliant.storyapp.data.retrofit.ApiService
import com.lisapriliant.storyapp.ui.detail.DetailActivity
import com.lisapriliant.storyapp.ui.maps.MapsActivity
import kotlinx.coroutines.flow.first

suspend fun UserPreference.getApiServiceWithToken(): ApiService? {
    val user = this.getSession().first()
    return if (user.isLogin && user.token.isNotEmpty()) {
        ApiConfig.getApiService(user.token)
    } else {
        null
    }
}

inline fun <reified T : Activity> Activity.startActivity() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

fun DetailActivity.showToastFromLiveData(toastLiveData: LiveData<Event<String>>) {
    toastLiveData.observe(this) {
        it.getContentIfNotHandled()?.let { toast ->
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
        }
    }
}

fun MapsActivity.showToastFromLiveData(toastLiveData: LiveData<Event<String>>) {
    toastLiveData.observe(this) {
        it.getContentIfNotHandled()?.let { toast ->
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
        }
    }
}
