package com.lisapriliant.storyapp.ui.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.lisapriliant.storyapp.databinding.ActivityDetailBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.showToastFromLiveData

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        detailViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        setupDetailStory()
    }

    private fun setupDetailStory() {
        val detailId = intent.getStringExtra(DETAIL_ID) ?: ""
        detailViewModel.getApiServiceWithToken().observe(this) { apiService ->
            if (apiService != null) {
                detailViewModel.getDetailStoryById(apiService, detailId)
            } else {
                showToastFromLiveData(detailViewModel.toast)
            }
        }
        detailViewModel.detailStory.observe(this) { detailStory ->
            if (detailStory != null) {
                binding.apply {
                    Glide.with(this@DetailActivity)
                        .load(detailStory.story.photoUrl)
                        .fitCenter()
                        .into(ivDetailPhoto)
                    tvDetailName.text = detailStory.story.name
                    tvDetailDescription.text = detailStory.story.description
                }
            } else {
                showToastFromLiveData(detailViewModel.toast)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    companion object {
        const val DETAIL_ID = "detail_id"
    }
}