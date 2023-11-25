package com.lisapriliant.storyapp.ui.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lisapriliant.storyapp.R
import com.lisapriliant.storyapp.databinding.ActivityAddStoryBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.getImageUri
import com.lisapriliant.storyapp.ui.main.MainActivity
import com.lisapriliant.storyapp.ui.reduceFileImage
import com.lisapriliant.storyapp.ui.uriToFile
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val addStoryViewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var currentImageUri: Uri? = null
    private var currentLocation: Location? = null
    private var location: Boolean = true
    private var image: Boolean = false

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getMyLastLocation()
        setupButton()
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLocation = location
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            else -> {
                // No Location access granted
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        addStoryViewModel.saveInstanceState(currentImageUri, binding.edAddDescription.text.toString(), currentLocation)
    }

    private fun setupButton() {
        binding.cameraButton.setOnClickListener {
            startCamera()
        }
        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.locationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            location = isChecked
        }
        binding.buttonAdd.setOnClickListener {
            addStory()
        }
    }

    private fun addStory() {
        showLoading(false)
        val description = binding.edAddDescription.text.toString()
        if (!image) {
            Toast.makeText(this, getString(R.string.image_required), Toast.LENGTH_SHORT).show()
        } else if (description == "") {
            Toast.makeText(this, getString(R.string.desc_required), Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                currentImageUri?.let { uri ->
                    val imageFile = uriToFile(uri, this@AddStoryActivity).reduceFileImage()
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                    val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
                    Log.e("Image File", "showImage: ${imageFile.path}")
                    postData(multipartBody, description, currentLocation)
                } ?: showToast()
            }
        }
    }

    private fun postData(file: MultipartBody.Part, description: String, currentLocation: Location? = null) {
        val descRequestBody = description.toRequestBody(MultipartBody.FORM)
        lifecycleScope.launch {
            val apiService = addStoryViewModel.getApiServiceWithToken()
            if (apiService != null) {
                if (!location) {
                    addStoryViewModel.addStory(apiService, file, descRequestBody)
                } else {
                    addStoryViewModel.addStory(apiService, file, descRequestBody, currentLocation)
                }
            }
        }
        addStoryViewModel.addNewStory.observe(this) { addNewStory ->
            if (!addNewStory.error) {
                moveToMainActivity()
            }
        }
        showToast()
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
            image = true
            binding.previewImageView.setImageURI(it)
        }
    }
}