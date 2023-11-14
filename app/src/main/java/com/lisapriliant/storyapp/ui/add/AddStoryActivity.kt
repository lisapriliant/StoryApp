package com.lisapriliant.storyapp.ui.add

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.lisapriliant.storyapp.R
import com.lisapriliant.storyapp.databinding.ActivityAddStoryBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.getImageUri
import com.lisapriliant.storyapp.ui.main.MainActivity
import com.lisapriliant.storyapp.ui.reduceFileImage
import com.lisapriliant.storyapp.ui.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val addStoryViewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)

        addStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        if (savedInstanceState != null) {
            currentImageUri = addStoryViewModel.currentImageUri
            binding.edAddDescription.setText(addStoryViewModel.description)
            showImage()
            showLoading(true)
        }
        setupButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        addStoryViewModel.saveInstanceState(currentImageUri, binding.edAddDescription.text.toString())
    }

    private fun setupButton() {
        binding.cameraButton.setOnClickListener {
            startCamera()
        }
        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.buttonAdd.setOnClickListener {
            addStory()
        }
    }

    private fun addStory() {
        showLoading(true)
        val description = binding.edAddDescription.text.toString()
        addStoryViewModel.getSession().observe(this) {
            currentImageUri?.let { uri ->
                val imageFile = uriToFile(uri, this).reduceFileImage()
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
                Log.e("Image File", "showImage: ${imageFile.path}")
                postData(multipartBody, description)
            } ?: showToast()
        }
    }

    private fun postData(file: MultipartBody.Part, description: String) {
        val descRequestBody = description.toRequestBody(MultipartBody.FORM)
        addStoryViewModel.getApiServiceWithToken().observe(this) { apiService ->
            if (apiService != null) {
                addStoryViewModel.addNewStory(apiService, file, descRequestBody)
            }
            addStoryViewModel.addNewStory.observe(this) { addNewStory ->
                if (!addNewStory.error) {
                    moveToMainActivity()
                }
            }
            showToast()
        }
    }

    private fun moveToMainActivity() {
        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun startGallery() {
        launcherIntentGallery.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showToast()
            showImage()
        } else {
            Log.e("Photo Picker", "No media selected")
            showToast()
        }
    }

    private fun showToast() {
        addStoryViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { toast ->
                Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.e("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }
}