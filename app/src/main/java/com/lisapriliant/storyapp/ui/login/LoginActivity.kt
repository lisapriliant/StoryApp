package com.lisapriliant.storyapp.ui.login

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
import com.lisapriliant.storyapp.data.pref.UserModel
import com.lisapriliant.storyapp.databinding.ActivityLoginBinding
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.main.MainActivity
import com.lisapriliant.storyapp.ui.register.RegisterActivity
import com.lisapriliant.storyapp.ui.startActivity
import com.lisapriliant.storyapp.ui.welcome.WelcomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel.isLoading.observe(this) {
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
            window.insetsController?.hide(
                WindowInsets.Type.statusBars()
            )
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
            loginButton.setOnClickListener {
                when {
                    edLoginEmail.length() == 0 -> {
                        edLoginEmail.error = getString(R.string.email_required)
                    }
                    edLoginPassword.length() == 0 -> {
                        edLoginPassword.error = getString(R.string.password_required)
                    }
                    else -> {
                        edLoginEmail.error = null
                        edLoginPassword.error = null

                        sendData()
                        goToActivity()
                        showLoading(true)
                        showToastFromLiveData()
                    }
                }
            }
        }
        goToRegister()
    }

    private fun goToRegister() {
        val toRegister: TextView = findViewById(R.id.tv_goToRegister)
        toRegister.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }

    private fun goToActivity() {
        loginViewModel.login.observe(this) {
            if (it != null) {
                if (!it.error) {
                    startActivity<MainActivity>()
                    finish()
                }
            }
        }
    }

    private fun sendData() {
        binding.apply {
            loginViewModel.isLogin(
                edLoginEmail.text.toString(),
                edLoginPassword.text.toString()
            )
        }
        loginViewModel.login.observe(this) {
            if (it != null) {
                if (!it.error) {
                    saveSession(
                        UserModel(
                            it.loginResult?.name ?: "",
                            it.loginResult?.token ?: "",
                            it.loginResult != null
                        )
                    )
                }
            }
        }
    }

    private fun saveSession(user: UserModel) {
        loginViewModel.saveSession(user)
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

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvLoginMsg, View.ALPHA, 1f).setDuration(500)
        val subTitle = ObjectAnimator.ofFloat(binding.tvLoginMsg2, View.ALPHA, 1f).setDuration(500)
        val tvEmail = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(500)
        val tilEmail = ObjectAnimator.ofFloat(binding.tilEmail, View.ALPHA, 1f).setDuration(500)
        val tvPassword = ObjectAnimator.ofFloat(binding.tvPassword, View.ALPHA, 1f).setDuration(500)
        val tilPassword = ObjectAnimator.ofFloat(binding.tilPassword, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(500)
        val tvRegister = ObjectAnimator.ofFloat(binding.tvMustRegister, View.ALPHA, 1f).setDuration(500)
        val register = ObjectAnimator.ofFloat(binding.tvGoToRegister, View.ALPHA, 1f).setDuration(500)

        val emailTogether = AnimatorSet().apply {
            playTogether(tvEmail, tilEmail)
        }

        val passwordTogether = AnimatorSet().apply {
            playTogether(tvPassword, tilPassword)
        }

        val registerTogether = AnimatorSet().apply {
            playTogether(tvRegister, register)
        }

        AnimatorSet().apply {
            playSequentially(
                title,
                subTitle,
                emailTogether,
                passwordTogether,
                login,
                registerTogether
            )
            start()
        }
    }

    private fun showToastFromLiveData() {
        loginViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { showToast ->
                Toast.makeText(this, showToast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
}