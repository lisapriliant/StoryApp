package com.lisapriliant.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.lisapriliant.storyapp.R
import com.lisapriliant.storyapp.databinding.ActivityRegisterBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.login.LoginActivity
import com.lisapriliant.storyapp.ui.startActivity
import com.lisapriliant.storyapp.ui.welcome.WelcomeActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val registerViewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModelFactory = ViewModelFactory.getInstance(this)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.apply {
            registerButton.setOnClickListener {
                when {
                    edRegisterName.length() == 0 -> {
                        edRegisterName.error = getString(R.string.name_required)
                    }
                    edRegisterEmail.length() == 0 -> {
                        edRegisterEmail.error = getString(R.string.email_required)
                    }
                    edRegisterPassword.length() == 0 -> {
                        edRegisterPassword.error = getString(R.string.password_required)
                    }
                    else -> {
                        edRegisterName.error = null
                        edRegisterEmail.error = null
                        edRegisterPassword.error = null

                        sendData()
                        goToActivity()
                        showLoading(true)
                        showToastFromLiveData()
                    }
                }
            }
        }
        goToLogin()
    }

    private fun goToLogin() {
        val toLogin: TextView = findViewById(R.id.tv_goToLogin)
        toLogin.setOnClickListener {
            startActivity<LoginActivity>()
        }
    }

    private fun sendData() {
        binding.apply {
            registerViewModel.isRegister(
                edRegisterName.text.toString(),
                edRegisterEmail.text.toString(),
                edRegisterPassword.text.toString()
            )
        }
    }

    private fun goToActivity() {
        registerViewModel.register.observe(this) {
            if (!it.error) {
                startActivity<LoginActivity>()
                finish()
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivRegister, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(500)
        val tvName = ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 1f).setDuration(500)
        val tilName = ObjectAnimator.ofFloat(binding.tilName, View.ALPHA, 1f).setDuration(500)
        val tvEmail = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(500)
        val tilEmail = ObjectAnimator.ofFloat(binding.tilEmail, View.ALPHA, 1f).setDuration(500)
        val tvPassword = ObjectAnimator.ofFloat(binding.tvPassword, View.ALPHA, 1f).setDuration(500)
        val tilPassword = ObjectAnimator.ofFloat(binding.tilPassword, View.ALPHA, 1f).setDuration(500)
        val register = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(500)
        val tvLogin = ObjectAnimator.ofFloat(binding.tvMustLogin, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvGoToLogin, View.ALPHA, 1f).setDuration(500)

        val nameTogether = AnimatorSet().apply {
            playTogether(tvName, tilName)
        }

        val emailTogether = AnimatorSet().apply {
            playTogether(tvEmail, tilEmail)
        }

        val passwordTogether = AnimatorSet().apply {
            playTogether(tvPassword, tilPassword)
        }

        val loginTogether = AnimatorSet().apply {
            playTogether(tvLogin, login)
        }

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTogether,
                emailTogether,
                passwordTogether,
                register,
                loginTogether
            )
            start()
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        goToWelcome()
    }

    private fun goToWelcome() {
        startActivity<WelcomeActivity>()
    }

    private fun showToastFromLiveData() {
        registerViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { showToast ->
                Toast.makeText(this, showToast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
}