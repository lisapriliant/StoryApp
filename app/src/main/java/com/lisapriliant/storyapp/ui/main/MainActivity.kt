package com.lisapriliant.storyapp.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lisapriliant.storyapp.databinding.ActivityMainBinding
import com.lisapriliant.storyapp.R
import com.lisapriliant.storyapp.data.response.ListStoryItem
import com.lisapriliant.storyapp.databinding.ItemStoryBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.add.AddStoryActivity
import com.lisapriliant.storyapp.ui.detail.DetailActivity
import com.lisapriliant.storyapp.ui.maps.MapsActivity
import com.lisapriliant.storyapp.ui.startActivity
import com.lisapriliant.storyapp.ui.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoryAdapter
    private lateinit var viewModelFactory: ViewModelFactory
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val _token = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.app_name)

        mainViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModelFactory = ViewModelFactory.getInstance(this)

        setupAdapter()
        setupSession()
        goToAddStory()
    }

    private fun setupAdapter() {
        adapter = StoryAdapter()
        val layoutManager = LinearLayoutManager(this)
        binding.apply {
            rvListStory.layoutManager = layoutManager
            rvListStory.setHasFixedSize(true)
            rvListStory.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )
        }

        adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem, binding: ItemStoryBinding) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.DETAIL_ID, data.id)
                val optionCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        Pair(binding.ivItemPhoto, "photo"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.tvItemDescription, "description")
                    )
                startActivity(intent, optionCompat.toBundle())
            }
        })
    }

    private fun setupSession() {
        showLoading(false)
        mainViewModel.getSession().observe(this) {
            Log.e(TAG, "isLogin: ${it.isLogin}")
            _token.value = it.token
            if (it.isLogin) {
                Log.e(TOKEN, "token: $_token")
                setupData()
            } else {
                goToWelcome()
            }
        }
    }

    private fun setupData() {
        mainViewModel.storyItem.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun goToWelcome() {
        startActivity<WelcomeActivity>()
        finish()
    }

    private fun goToAddStory() {
        binding.fabAddStory.setOnClickListener {
            startActivity<AddStoryActivity>()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.maps -> {
                goToMaps()
                true
            }
            R.id.setting -> {
                setupSetting()
                true
            }
            R.id.logout -> {
                lifecycleScope.launch {
                    mainViewModel.logout()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToMaps() {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun setupSetting() {
        startActivity(
            Intent(
                Settings.ACTION_LOCALE_SETTINGS
            )
        )
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    companion object {
        const val TAG = "MainActivity"
        const val TOKEN = "token"
    }
}